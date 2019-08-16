package com.coinninja.coinkeeper.model.helpers

import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionData
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.service.client.model.*
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class TransactionHelperTest {

    private fun createHelper(): TransactionHelper = TransactionHelper(mock(), mock(), mock(),
            mock(), mock(), mock(), mock(), mock(), mock(), mock())

    @Test
    fun initializes_transaction_with_pending_mempool_state() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val gsonAddress = GsonAddress(txid = "--txid--")
        val wallet: Wallet = mock()
        whenever(helper.walletHelper.wallet).thenReturn(wallet)

        helper.initializeTransaction(transaction, gsonAddress)

        val ordered = inOrder(transaction, helper.daoSessionManager)
        ordered.verify(transaction).wallet = wallet
        ordered.verify(transaction).txid = "--txid--"
        ordered.verify(transaction).memPoolState = MemPoolState.PENDING
        ordered.verify(helper.daoSessionManager).insert(transaction)
    }

    @Test
    fun initializing_transactions_process_transactions_already_consumed() {
        val helper = createHelper()
        val addresses: List<GsonAddress> = listOf(
                GsonAddress(address = "--address-1", txid = "--txid-1--")
        )
        whenever(helper.daoSessionManager.newTransactionSummary()).thenReturn(mock())
        whenever(helper.transactionQueryManager.transactionByTxid("--txid-1--")).thenReturn(mock())

        helper.initTransactions(addresses)

        verify(helper.daoSessionManager, times(0)).newTransactionSummary()
        verify(helper.daoSessionManager, times(0)).insert(any<TransactionSummary>())
    }

    @Test
    fun initializing_transactions_will_not_produce_duplicates() {
        val helper = createHelper()
        val addresses: List<GsonAddress> = listOf(
                GsonAddress(address = "--address-1", txid = "--txid-1-"),
                GsonAddress(address = "--address-1", txid = "--txid-2-"),
                GsonAddress(address = "--address-2", txid = "--txid-3-"),
                GsonAddress(address = "--address-3", txid = "--txid-1-")
        )
        whenever(helper.daoSessionManager.newTransactionSummary()).thenReturn(mock())

        helper.initTransactions(addresses)

        verify(helper.daoSessionManager, times(3)).newTransactionSummary()
        verify(helper.daoSessionManager, times(3)).insert(any<TransactionSummary>())
    }

    @Test
    fun updates_transaction_as_failed_to_broadcast() {
        val helper = createHelper()
        val txid = "--txid--"
        val transaction: TransactionSummary = mock()
        whenever(helper.transactionQueryManager.transactionByTxid(txid)).thenReturn(transaction)

        helper.markTransactionSummaryAsFailedToBroadcast(txid)

        val ordered = inOrder(transaction)
        ordered.verify(transaction).memPoolState = MemPoolState.FAILED_TO_BROADCAST
        ordered.verify(transaction).update()
    }

    @Test
    fun cancels_inputs_when_transaction_fails_to_broadcast() {
        val helper = createHelper()
        val txid = "--txid--"
        val transaction: TransactionSummary = mock()
        val input1 = mock<FundingStat>()
        val input2 = mock<FundingStat>()
        val output1 = mock<TargetStat>()
        val output2 = mock<TargetStat>()
        val inputs: List<FundingStat> = listOf(input1, input2)
        whenever(helper.transactionQueryManager.transactionByTxid(txid)).thenReturn(transaction)
        whenever(transaction.funder).thenReturn(inputs)
        whenever(input1.id).thenReturn(1)
        whenever(input2.id).thenReturn(2)
        whenever(helper.transactionQueryManager.targetStatFromFundingId(1)).thenReturn(output1)
        whenever(helper.transactionQueryManager.targetStatFromFundingId(2)).thenReturn(output2)

        helper.markTransactionSummaryAsFailedToBroadcast(txid)

        verify(input1).state = FundingStat.State.CANCELED
        verify(input1).update()
        verify(output1).fundingStat = null
        verify(output1).update()
        verify(input2).state = FundingStat.State.CANCELED
        verify(input2).update()
        verify(output2).fundingStat = null
        verify(output2).update()
    }

    @Test
    fun cancels_outputs_for_transactions_that_have_failed_to_broadcast() {
        val helper = createHelper()
        val txid = "--txid--"
        val transaction: TransactionSummary = mock()
        val input1 = mock<FundingStat>()
        val input2 = mock<FundingStat>()
        val output1 = mock<TargetStat>()
        val output2 = mock<TargetStat>()
        val outputs: List<TargetStat> = listOf(output1, output2)
        whenever(helper.transactionQueryManager.transactionByTxid(txid)).thenReturn(transaction)
        whenever(transaction.receiver).thenReturn(outputs)
        whenever(output1.id).thenReturn(1)
        whenever(output2.id).thenReturn(2)
        whenever(helper.transactionQueryManager.fundingStatFromTargetId(1)).thenReturn(input1)
        whenever(helper.transactionQueryManager.fundingStatFromTargetId(2)).thenReturn(input2)

        helper.markTransactionSummaryAsFailedToBroadcast(txid)

        verify(output1).state = TargetStat.State.CANCELED
        verify(output1).update()
        verify(input1).targetStat = null
        verify(input1).update()
        verify(output2).state = TargetStat.State.CANCELED
        verify(output2).update()
        verify(input2).targetStat = null
        verify(input2).update()
    }

    @Test
    fun updates_transaction_as_acknowledged() {
        val helper = createHelper()
        val txid = "--txid--"
        val transaction: TransactionSummary = mock()
        whenever(helper.transactionQueryManager.transactionByTxid(txid)).thenReturn(transaction)

        helper.markTransactionSummaryAsAcknowledged(txid)

        val ordered = inOrder(transaction)
        ordered.verify(transaction).memPoolState = MemPoolState.ACKNOWLEDGE
        ordered.verify(transaction).update()
    }

    @Test
    fun updates_many_transactions_from_list_of_details() {
        val tx1 = mock<TransactionSummary>()
        val tx2 = mock<TransactionSummary>()
        val detail1 = TransactionDetail(txid = "--txid-1--")
        val detail2 = TransactionDetail(txid = "--txid-2--")
        val helper = createHelper()
        whenever(helper.transactionQueryManager.transactionByTxid(detail1.txid)).thenReturn(tx1)
        whenever(helper.transactionQueryManager.transactionByTxid(detail2.txid)).thenReturn(tx2)
        val transactions: List<TransactionDetail> = listOf(detail1, detail2)

        helper.updateTransactions(transactions, 100)

        val ordered = inOrder(tx1, tx2, helper.transactionInviteSummaryHelper)
        ordered.verify(tx1).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(tx1)
        ordered.verify(tx2).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(tx2)
    }

    @Test
    fun updates_transaction_from_transaction_details() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val time = System.currentTimeMillis() / 1000
        val detail = TransactionDetail(
                txid = "--txid--",
                blockhash = "--block-hash--",
                blocktime = time,
                blockheight = 2,
                vInList = listOf(),
                vOutList = listOf()
        )
        whenever(helper.walletHelper.wallet).thenReturn(mock())

        helper.updateTransaction(transaction, detail, 100)

        val ordered = inOrder(transaction, helper.transactionInviteSummaryHelper)
        ordered.verify(transaction).blockheight = 2
        ordered.verify(transaction).numConfirmations = 99
        ordered.verify(transaction).txTime = time * 1000
        ordered.verify(transaction).blockhash = "--block-hash--"
        ordered.verify(transaction).memPoolState = MemPoolState.MINED
        ordered.verify(transaction).numInputs = 0
        ordered.verify(transaction).numOutputs = 0
        ordered.verify(transaction).wallet = helper.walletHelper.wallet
        ordered.verify(transaction).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
    }

    @Test
    fun updates_transaction_inputs_from_transaction_details() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        whenever(transaction.id).thenReturn(1)
        whenever(transaction.memPoolState).thenReturn(MemPoolState.ACKNOWLEDGE)
        val time = System.currentTimeMillis() / 1000
        val detail = TransactionDetail(
                txid = "--txid--",
                blockhash = "--block-hash--",
                blocktime = time,
                blockheight = 2,
                vInList = listOf(VIn(
                        txid = "--funding-txid--",
                        previousOutput = VOut(
                                value = 1000,
                                index = 1,
                                scriptPubKey = ScriptPubKey(
                                        addresses = arrayOf("--address-1--")
                                )

                        )
                )),
                vOutList = listOf()
        )
        val fundingStat: FundingStat = mock()
        whenever(helper.fundingStatHelper.getOrCreateFundingStat(transaction, detail.vInList[0])).thenReturn(fundingStat)

        helper.updateTransaction(transaction, detail, 100)

        val ordered = inOrder(transaction, fundingStat, helper.transactionInviteSummaryHelper)
        ordered.verify(transaction).numInputs = 1
        ordered.verify(transaction).numOutputs = 0
        ordered.verify(fundingStat).state = FundingStat.State.ACKNOWLEDGE
        ordered.verify(fundingStat).update()
        ordered.verify(transaction).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
    }

    @Test
    fun updates_transaction_inputs_from_transaction_details__sent_from_our_address() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        whenever(transaction.id).thenReturn(1)
        whenever(transaction.memPoolState).thenReturn(MemPoolState.ACKNOWLEDGE)
        whenever(transaction.wallet).thenReturn(mock())
        val time = System.currentTimeMillis() / 1000
        val detail = TransactionDetail(
                txid = "--txid--",
                blockhash = "--block-hash--",
                blocktime = time,
                blockheight = 2,
                vInList = listOf(VIn(
                        txid = "--funding-txid--",
                        previousOutput = VOut(
                                value = 1000,
                                index = 1,
                                scriptPubKey = ScriptPubKey(
                                        addresses = arrayOf("--address-1--")
                                )

                        )
                )),
                vOutList = listOf()
        )
        val fundingStat: FundingStat = mock()
        val address: Address = mock()
        whenever(helper.fundingStatHelper.getOrCreateFundingStat(transaction, detail.vInList[0])).thenReturn(fundingStat)
        whenever(helper.addressHelper.addressForPubKey("--address-1--")).thenReturn(address)

        helper.updateTransaction(transaction, detail, 100)

        val ordered = inOrder(transaction, fundingStat, helper.transactionInviteSummaryHelper)
        ordered.verify(transaction).numInputs = 1
        ordered.verify(transaction).numOutputs = 0
        ordered.verify(fundingStat).address = address
        ordered.verify(fundingStat).wallet = transaction.wallet
        ordered.verify(fundingStat).state = FundingStat.State.ACKNOWLEDGE
        ordered.verify(fundingStat).update()
        ordered.verify(transaction).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
    }

    @Test
    fun updates_transaction_ouptuts_from_transaction_details() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        whenever(transaction.id).thenReturn(1)
        whenever(transaction.memPoolState).thenReturn(MemPoolState.ACKNOWLEDGE)
        whenever(transaction.wallet).thenReturn(mock())
        val time = System.currentTimeMillis() / 1000
        val detail = TransactionDetail(
                txid = "--txid--",
                blockhash = "--block-hash--",
                blocktime = time,
                blockheight = 2,
                vOutList = listOf(VOut(
                        value = 1000,
                        index = 1,
                        scriptPubKey = ScriptPubKey(
                                addresses = arrayOf("--address-1--")
                        )
                ))
        )
        val targetStat: TargetStat = mock()
        whenever(helper.targetStatHelper.getOrCreateTargetStat(transaction, detail.vOutList[0])).thenReturn(targetStat)

        helper.updateTransaction(transaction, detail, 100)

        val ordered = inOrder(targetStat, transaction, helper.transactionInviteSummaryHelper)
        ordered.verify(transaction).numInputs = 0
        ordered.verify(transaction).numOutputs = 1
        ordered.verify(targetStat).state = TargetStat.State.ACKNOWLEDGE
        ordered.verify(targetStat).update()
        ordered.verify(transaction).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
    }

    @Test
    fun can_save_coinbase_transaction_outputs() {
        val helper = createHelper()
        val detail: TransactionDetail = Gson().fromJson(coinbaseTX, TransactionDetail::class.java)
        val transaction: TransactionSummary = mock()
        val targetStat: TargetStat = mock()
        whenever(transaction.memPoolState).thenReturn(MemPoolState.ACKNOWLEDGE)
        whenever(helper.targetStatHelper.getOrCreateTargetStat(transaction, detail.vOutList[1], "Coinbase")).thenReturn(targetStat)

        helper.saveOut(detail, transaction, detail.vOutList[1])

        verify(helper.targetStatHelper).getOrCreateTargetStat(transaction, detail.vOutList[1], "Coinbase")
        verify(targetStat).state = TargetStat.State.ACKNOWLEDGE
        verify(targetStat).update()
    }

    @Test
    fun can_save_coinbase_transaction_inputs() {
        val helper = createHelper()
        val detail: TransactionDetail = Gson().fromJson(coinbaseTX, TransactionDetail::class.java)
        val transaction: TransactionSummary = mock()
        val fundingStat: FundingStat = mock()
        whenever(transaction.memPoolState).thenReturn(MemPoolState.ACKNOWLEDGE)
        whenever(helper.fundingStatHelper.getOrCreateFundingStat(transaction, detail.vInList[0], "Coinbase")).thenReturn(fundingStat)

        helper.saveIn(detail, transaction, detail.vInList[0])

        verify(helper.fundingStatHelper).getOrCreateFundingStat(transaction, detail.vInList[0], "Coinbase")
        verify(fundingStat).state = FundingStat.State.ACKNOWLEDGE
        verify(fundingStat).update()
    }

    @Test
    fun updates_transaction_ouptuts_from_transaction_details__saves_reference_to_our_address() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        whenever(transaction.id).thenReturn(1)
        whenever(transaction.memPoolState).thenReturn(MemPoolState.ACKNOWLEDGE)
        whenever(transaction.wallet).thenReturn(mock())
        val time = System.currentTimeMillis() / 1000
        val detail = TransactionDetail(
                txid = "--txid--",
                blockhash = "--block-hash--",
                blocktime = time,
                blockheight = 2,
                vOutList = listOf(VOut(
                        value = 1000,
                        index = 1,
                        scriptPubKey = ScriptPubKey(
                                addresses = arrayOf("--address-1--")
                        )
                ))
        )
        val targetStat: TargetStat = mock()
        val address: Address = mock()
        whenever(helper.targetStatHelper.getOrCreateTargetStat(transaction, detail.vOutList[0])).thenReturn(targetStat)
        whenever(helper.addressHelper.addressForPubKey("--address-1--")).thenReturn(address)

        helper.updateTransaction(transaction, detail, 100)

        val ordered = inOrder(targetStat, transaction, helper.transactionInviteSummaryHelper)
        ordered.verify(transaction).numInputs = 0
        ordered.verify(transaction).numOutputs = 1
        ordered.verify(targetStat).address = address
        ordered.verify(targetStat).wallet = transaction.wallet
        ordered.verify(targetStat).state = TargetStat.State.ACKNOWLEDGE
        ordered.verify(targetStat).update()
        ordered.verify(transaction).update()
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
    }

    @Test
    fun updates_settlement_with_identity_when_transaction_sent() {
        val helper = createHelper()
        val identity = mock<Identity>()
        val dropbitMeIdentity: DropbitMeIdentity = mock()
        val toUser = mock<UserIdentity>()
        val fromUser = mock<UserIdentity>()
        val settlement: TransactionsInvitesSummary = mock()

        whenever(identity.identityType).thenReturn(IdentityType.TWITTER)
        whenever(helper.userIdentityHelper.updateFrom(identity)).thenReturn(toUser)
        whenever(helper.userIdentityHelper.updateFrom(dropbitMeIdentity)).thenReturn(fromUser)
        whenever(helper.dropbitAccountHelper.identityForType(identity.identityType)).thenReturn(dropbitMeIdentity)

        helper.addUserIdentitiesToTransaction(identity, settlement)

        val ordered = inOrder(settlement)
        ordered.verify(settlement).fromUser = fromUser
        ordered.verify(settlement).toUser = toUser
        ordered.verify(settlement).update()
    }

    @Test
    fun creates_initial_transaction_at_time_of_broadcast() {
        val helper = createHelper()
        val identity = mock<Identity>()
        val dropbitMeIdentity: DropbitMeIdentity = mock()
        val toUser = mock<UserIdentity>()
        val fromUser = mock<UserIdentity>()
        val settlement: TransactionsInvitesSummary = mock()
        val completedBroadcastDTO = CompletedBroadcastDTO(
                transactionId = "--txid--",
                identity = identity,
                transactionData = TransactionData(
                        utxos = arrayOf(
                                UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                                        DerivationPath(49, 0, 0, 0, 1))),
                        amount = 1000, feeAmount = 10, changeAmount = 100000,
                        changePath = DerivationPath(49, 0, 0, 1, 0),
                        paymentAddress = "--pay-to-address--"
                )
        )
        val transaction: TransactionSummary = mock()
        whenever(helper.daoSessionManager.newTransactionSummary()).thenReturn(transaction)
        whenever(helper.walletHelper.wallet).thenReturn(mock())
        whenever(helper.dateUtil.getCurrentTimeInMillis()).thenReturn(System.currentTimeMillis())
        whenever(helper.dateUtil.getCurrentTimeInMillis()).thenReturn(System.currentTimeMillis())
        whenever(identity.identityType).thenReturn(IdentityType.TWITTER)
        whenever(helper.userIdentityHelper.updateFrom(identity)).thenReturn(toUser)
        whenever(helper.userIdentityHelper.updateFrom(dropbitMeIdentity)).thenReturn(fromUser)
        whenever(helper.dropbitAccountHelper.identityForType(identity.identityType)).thenReturn(dropbitMeIdentity)
        whenever(helper.transactionInviteSummaryHelper.getOrCreateParentSettlementFor(transaction)).thenReturn(settlement)

        helper.createInitialTransactionForCompletedBroadcast(completedBroadcastDTO)

        val ordered = inOrder(transaction, helper.daoSessionManager, helper.transactionInviteSummaryHelper, settlement, helper.fundingStatHelper, helper.targetStatHelper)
        ordered.verify(transaction).txid = completedBroadcastDTO.transactionId
        ordered.verify(transaction).wallet = helper.walletHelper.wallet
        ordered.verify(transaction).memPoolState = MemPoolState.PENDING
        ordered.verify(transaction).txTime = helper.dateUtil.getCurrentTimeInMillis()
        ordered.verify(helper.daoSessionManager).insert(transaction)
        ordered.verify(helper.fundingStatHelper).createInputsFor(transaction, completedBroadcastDTO.transactionData)
        ordered.verify(helper.targetStatHelper).createOutputsFor(transaction, completedBroadcastDTO.transactionData)
        ordered.verify(helper.transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
        ordered.verify(settlement).fromUser = fromUser
        ordered.verify(settlement).toUser = toUser
        ordered.verify(settlement).update()
    }
}