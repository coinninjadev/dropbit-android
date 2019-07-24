package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.model.db.FundingStat
import com.coinninja.coinkeeper.model.db.TargetStat
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.Wallet
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import com.coinninja.coinkeeper.service.client.model.TransactionDetail
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class TransactionHelperTest {

    private fun createHelper(): TransactionHelper = TransactionHelper(mock(), mock(), mock(),
            mock(), mock(), mock(), mock())

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
        val detail1 = TransactionDetail().also {
            it.transactionId = "--txid-1--"
        }
        val detail2 = TransactionDetail().also {
            it.transactionId = "--txid-2--"

        }
        val helper = createHelper()
        whenever(helper.transactionQueryManager.transactionByTxid(detail1.transactionId)).thenReturn(tx1)
        whenever(helper.transactionQueryManager.transactionByTxid(detail2.transactionId)).thenReturn(tx2)
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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun updates_transaction_inputs_from_transaction_details() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Test
    fun updates_transaction_ouptuts_from_transaction_details() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /*

    @Test
    fun merging_invite_to_transaction_invite_time_when_txtime_null_when_available() {
        val transactionSummary = mock(TransactionSummary::class.java)
        val inviteTransactionSummary = mock(InviteTransactionSummary::class.java)
        val transactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)
        val joinTableDao = mock(TransactionsInvitesSummaryDao::class.java)
        val joinQuery = mock(QueryBuilder<*>::class.java)
        whenever(joinTableDao.queryBuilder()).thenReturn(joinQuery)
        whenever<QueryBuilder<TransactionsInvitesSummary>>(joinQuery.where(ArgumentMatchers.any())).thenReturn(joinQuery)
        whenever<QueryBuilder<TransactionsInvitesSummary>>(joinQuery.limit(1)).thenReturn(joinQuery)
        whenever<TransactionsInvitesSummary>(joinQuery.unique()).thenReturn(transactionsInvitesSummary).thenReturn(mock(TransactionsInvitesSummary::class.java))
        whenever(daoSessionManager.transactionsInvitesSummaryDao).thenReturn(joinTableDao)
        whenever(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary)
        whenever(inviteTransactionSummary.sentDate).thenReturn(100000L)
        whenever(transactionSummary.txTime).thenReturn(0L)

        helper.joinInviteToTx(inviteTransactionSummary, transactionSummary)

        verify(transactionsInvitesSummary, times(0)).btcTxTime = ArgumentMatchers.anyLong()
        verify(transactionsInvitesSummary).inviteTime = 100000L
    }

    @Test
    fun merging_invite_to_transaction_keeps_tx_time_when_available() {
        val transactionSummary = mock(TransactionSummary::class.java)
        val inviteTransactionSummary = mock(InviteTransactionSummary::class.java)
        val transactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)
        val joinTableDao = mock(TransactionsInvitesSummaryDao::class.java)
        val joinQuery = mock(QueryBuilder<*>::class.java)
        whenever(joinTableDao.queryBuilder()).thenReturn(joinQuery)
        whenever<QueryBuilder<TransactionsInvitesSummary>>(joinQuery.where(ArgumentMatchers.any())).thenReturn(joinQuery)
        whenever<QueryBuilder<TransactionsInvitesSummary>>(joinQuery.limit(1)).thenReturn(joinQuery)
        whenever<TransactionsInvitesSummary>(joinQuery.unique()).thenReturn(transactionsInvitesSummary).thenReturn(mock(TransactionsInvitesSummary::class.java))
        whenever(daoSessionManager.transactionsInvitesSummaryDao).thenReturn(joinTableDao)
        whenever(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary)
        whenever(inviteTransactionSummary.sentDate).thenReturn(100000L)
        whenever(transactionSummary.txTime).thenReturn(200000L)

        helper.joinInviteToTx(inviteTransactionSummary, transactionSummary)

        verify(transactionsInvitesSummary).btcTxTime = 200000L
        verify(transactionsInvitesSummary).inviteTime = 0
    }

    @Test
    fun saves_block_time_on_transaction() {
        val rTime: Long = 1
        val txTime: Long = 2
        val blockTime: Long = 3
        val td = mock(TransactionDetail::class.java)
        whenever(td.receivedTimeMillis).thenReturn(rTime)
        whenever(td.timeMillis).thenReturn(txTime)
        whenever(td.blocktimeMillis).thenReturn(blockTime)
        val ts = mock(TransactionSummary::class.java)

        helper.updateTransaction(ts, td)

        verify(ts).txTime = blockTime
    }

    @Test
    fun saves_tx_time_on_transaction_when_block_time_absent() {
        val rTime: Long = 1
        val txTime: Long = 2
        val td = mock(TransactionDetail::class.java)
        whenever(td.receivedTimeMillis).thenReturn(rTime)
        whenever(td.timeMillis).thenReturn(txTime)
        val ts = mock(TransactionSummary::class.java)

        helper.updateTransaction(ts, td)

        verify(ts).txTime = txTime
    }

    @Test
    fun saves_recieve_time_on_transaction_when_tx_time_and_block_time_absent() {
        val txTime = 1L
        val td = mock(TransactionDetail::class.java)
        whenever(td.receivedTimeMillis).thenReturn(txTime)
        val ts = mock(TransactionSummary::class.java)

        helper.updateTransaction(ts, td)

        verify(ts).txTime = txTime
    }

    @Test
    fun set_transaction_to_ACKNOWLEDGE_if_seen_by_coin_ninja_server_test() {
        val sampleTransaction = mock(TransactionSummary::class.java)
        val sampleTransactionID = "some tx id"
        setupTransactionSummaryDao(sampleTransaction)
        val transactionDetail = buildMockTransactionDetails(sampleTransactionID)
        val fetchedTransactions = ArrayList<TransactionDetail>()
        fetchedTransactions.add(transactionDetail)


        helper.updateTransactions(fetchedTransactions, 562372)

        verify(sampleTransaction).memPoolState = MemPoolState.ACKNOWLEDGE
        verify(sampleTransaction, atLeast(1)).refresh()
        verify(sampleTransaction, atLeast(1)).update()
    }

    @Test
    fun does_not_set_blockheight_when_not_mined() {
        val transactionSummary = mock(TransactionSummary::class.java)
        val sampleTransactionID = "some tx id"
        setupTransactionSummaryDao(transactionSummary)
        val transactionDetail = buildMockTransactionDetails(sampleTransactionID)
        whenever(transactionDetail.blockheight).thenReturn(0)
        val fetchedTransactions = ArrayList<TransactionDetail>()
        fetchedTransactions.add(transactionDetail)

        helper.updateTransactions(fetchedTransactions, 562372)

        verify(transactionSummary, times(0)).numConfirmations = ArgumentMatchers.anyInt()
    }

    @Test
    fun sets_confirmations_when_transaction_mined() {
        val transactionSummary = mock(TransactionSummary::class.java)
        val sampleTransactionID = "some tx id"
        setupTransactionSummaryDao(transactionSummary)
        val transactionDetail = buildMockTransactionDetails(sampleTransactionID)
        whenever(transactionDetail.blockheight).thenReturn(562302)
        val fetchedTransactions = ArrayList<TransactionDetail>()
        fetchedTransactions.add(transactionDetail)

        helper.updateTransactions(fetchedTransactions, 562372)

        verify(transactionSummary).numConfirmations = 71
    }

    @Test
    fun set_target_stat_state_toACKNOWLEDGE_base_on_TransactionMemPoolState_of_ACKNOWLEDGE_test() {
        val sampleMemPoolState = MemPoolState.ACKNOWLEDGE
        val sampleTargetStat = mock(TargetStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveOut(sampleTransaction, out)

        verify(sampleTargetStat).state = TargetStat.State.ACKNOWLEDGE
    }

    @Test
    fun set_target_stat_state_toACKNOWLEDGE_base_on_TransactionMemPoolState_of_MINED_test() {
        val sampleMemPoolState = MemPoolState.MINED
        val sampleTargetStat = mock(TargetStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveOut(sampleTransaction, out)

        verify(sampleTargetStat).state = TargetStat.State.ACKNOWLEDGE
    }

    @Test
    fun set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_FAILED_TO_BROADCAST_test() {
        val sampleMemPoolState = MemPoolState.FAILED_TO_BROADCAST
        val sampleTargetStat = mock(TargetStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveOut(sampleTransaction, out)

        verify(sampleTargetStat).state = TargetStat.State.CANCELED
    }

    @Test
    fun set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_DOUBLE_SPEND_test() {
        val sampleMemPoolState = MemPoolState.DOUBLE_SPEND
        val sampleTargetStat = mock(TargetStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveOut(sampleTransaction, out)

        verify(sampleTargetStat).state = TargetStat.State.CANCELED
    }

    @Test
    fun set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_ORPHANED_test() {
        val sampleMemPoolState = MemPoolState.ORPHANED
        val sampleTargetStat = mock(TargetStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveOut(sampleTransaction, out)

        verify(sampleTargetStat).state = TargetStat.State.CANCELED
    }

    @Test
    fun set_target_stat_state_toPENDING_base_on_TransactionMemPoolState_of_PENDING_test() {
        val sampleMemPoolState = MemPoolState.PENDING
        val sampleTargetStat = mock(TargetStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val out = setupTargetStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveOut(sampleTransaction, out)

        verify(sampleTargetStat).state = TargetStat.State.PENDING
    }

    @Test
    fun set_funding_stat_state_toPENDING_base_on_TransactionMemPoolState_of_PENDING_test() {
        val sampleMemPoolState = MemPoolState.PENDING
        val sampleTargetStat = mock(FundingStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val `in` = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveIn(sampleTransaction, `in`)

        verify(sampleTargetStat).state = FundingStat.State.PENDING
    }

    @Test
    fun set_funding_stat_state_to_ACKNOWLEDGE_base_on_TransactionMemPoolState_of_ACKNOWLEDGE_test() {
        val sampleMemPoolState = MemPoolState.ACKNOWLEDGE
        val sampleTargetStat = mock(FundingStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val `in` = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveIn(sampleTransaction, `in`)

        verify(sampleTargetStat).state = FundingStat.State.ACKNOWLEDGE
    }

    @Test
    fun set_funding_stat_state_to_ACKNOWLEDGE_base_on_TransactionMemPoolState_of_MINED_test() {
        val sampleMemPoolState = MemPoolState.MINED
        val sampleTargetStat = mock(FundingStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val `in` = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveIn(sampleTransaction, `in`)

        verify(sampleTargetStat).state = FundingStat.State.ACKNOWLEDGE
    }

    @Test
    fun set_funding_stat_state_to_CANCELED_base_on_TransactionMemPoolState_of_DOUBLE_SPEND_test() {
        val sampleMemPoolState = MemPoolState.DOUBLE_SPEND
        val sampleTargetStat = mock(FundingStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val `in` = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveIn(sampleTransaction, `in`)

        verify(sampleTargetStat).state = FundingStat.State.CANCELED
    }

    @Test
    fun set_funding_stat_state_to_CANCELED_base_on_TransactionMemPoolState_of_FAILED_TO_BROADCAST_test() {
        val sampleMemPoolState = MemPoolState.FAILED_TO_BROADCAST
        val sampleTargetStat = mock(FundingStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val `in` = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveIn(sampleTransaction, `in`)

        verify(sampleTargetStat).state = FundingStat.State.CANCELED
    }

    @Test
    fun set_funding_stat_state_to_CANCELED_base_on_TransactionMemPoolState_of_ORPHANED_test() {
        val sampleMemPoolState = MemPoolState.ORPHANED
        val sampleTargetStat = mock(FundingStat::class.java)
        val sampleTransaction = buildSampleTransactionSummary()
        val `in` = setupFundingStatMempoolTestSuit(sampleTransaction, sampleMemPoolState, sampleTargetStat)

        helper.saveIn(sampleTransaction, `in`)

        verify(sampleTargetStat).state = FundingStat.State.CANCELED
    }

    @Test
    fun if_transaction_is_in_a_block_set_its_TransactionMemPoolState_to_MINED_test() {
        val detail = mock(TransactionDetail::class.java)
        whenever(detail.blockhash).thenReturn("Some block hash")
        whenever(detail.isInBlock).thenCallRealMethod()

        helper.updateTransaction(transaction, detail)

        verify(transaction).memPoolState = MemPoolState.MINED
    }

    @Test
    fun if_transaction_is_NOT_in_a_block_empty_do_not_touch_the_TransactionMemPoolState_test() {
        val detail = mock(TransactionDetail::class.java)
        whenever(detail.blockhash).thenReturn("")

        helper.updateTransaction(transaction, detail)

        verify(transaction, times(0)).memPoolState = ArgumentMatchers.any()
    }

    @Test
    fun if_transaction_is_NOT_in_a_block_null_do_not_touch_the_TransactionMemPoolState_test() {
        val detail = mock(TransactionDetail::class.java)
        whenever(detail.blockhash).thenReturn(null)

        helper.updateTransaction(transaction, detail)

        verify(transaction, times(0)).memPoolState = ArgumentMatchers.any()
    }

    @Test
    fun adds_transaction_to_parent_settlement_when_creating_new_transaction_record() {
        val detail = mock(TransactionDetail::class.java)
        val transaction = mock(TransactionSummary::class.java)

        helper.updateTransaction(transaction, detail)

        verify(transactionInviteSummaryHelper).getOrCreateParentSettlementFor(transaction)
    }

    @Test
    fun get_incomplete_transactions_test() {
        val sampleTransactionSummary = mock(TransactionSummary::class.java)
        val sampleList = ArrayList<TransactionSummary>()
        sampleList.add(sampleTransactionSummary)
        whenever(transactionQueryManager.incompleteTransactions).thenReturn(sampleList)

        val transactions = helper.incompleteTransactions

        assertThat(transactions, equalTo(sampleList))
    }

    @Test
    fun rename_txid_when_marked_as_failed() {
        val exceptedNewTxId = "failedToBroadcast" + "_" + java.lang.Long.toString(654654) + "_" + "some - txid"

        val transaction = mock(TransactionSummary::class.java)
        val invite = mock(InviteTransactionSummary::class.java)
        val transactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)

        whenever(daoSessionManager.transactionSummaryDao).thenReturn(transactionSummaryDao)
        whenever(daoSessionManager.transactionsInvitesSummaryDao).thenReturn(transactionInviteSummaryDao)
        whenever(daoSessionManager.inviteTransactionSummaryDao).thenReturn(inviteDao)

        whenever(transactionSummaryDao.queryBuilder()).thenReturn(tsQuery)
        whenever(transactionInviteSummaryDao.queryBuilder()).thenReturn(tsInviteQuery)
        whenever(inviteDao.queryBuilder()).thenReturn(inviteQuery)

        whenever(tsQuery.where(ArgumentMatchers.any())).thenReturn(tsQuery)
        whenever(inviteQuery.where(ArgumentMatchers.any())).thenReturn(inviteQuery)
        whenever(tsInviteQuery.where(ArgumentMatchers.any())).thenReturn(tsInviteQuery)

        whenever(tsQuery.limit(1)).thenReturn(tsQuery)
        whenever(inviteQuery.limit(1)).thenReturn(inviteQuery)
        whenever(tsInviteQuery.limit(1)).thenReturn(tsInviteQuery)

        whenever(tsQuery.unique()).thenReturn(transaction)
        whenever(inviteQuery.unique()).thenReturn(invite)
        whenever(tsInviteQuery.unique()).thenReturn(transactionsInvitesSummary)


        val newTxid = helper.markTransactionSummaryAsFailedToBroadcast("some - txid")

        assertThat(newTxid, equalTo("failedToBroadcast_654654_some - txid"))
        verify(transaction).txid = exceptedNewTxId
        verify(invite).btcTransactionId = exceptedNewTxId
        verify(transactionsInvitesSummary).inviteTxID = exceptedNewTxId
    }

    @Test
    fun records_contact_name_and_number_to_give_transaction() {
        whenever(transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transaction)).thenReturn(transactionsInvitesSummary)
        val transactionData = mock(TransactionData::class.java)
        val toName = "Joe Smoe"
        val identity = Identity(IdentityType.PHONE, RECEIVER_PHONE_STRING, "--hash--", toName, "", false, null)
        val txid = "--txid--"
        val completedBroadcastDTO = CompletedBroadcastDTO(transactionData, txid, identity)
        val wallet = mock(Wallet::class.java)
        whenever(walletHelper.wallet).thenReturn(wallet)
        whenever(daoSessionManager.newTransactionSummary()).thenReturn(transaction)
        whenever(daoSessionManager.insert(transaction)).thenReturn(1L)

        val myIdentity = mock(DropbitMeIdentity::class.java)
        whenever(dropbitAccountHelper.identityForType(identity.identityType)).thenReturn(myIdentity)
        val toUser = mock(UserIdentity::class.java)
        whenever(userIdentityHelper.updateFrom(identity)).thenReturn(toUser)
        val fromUser = mock(UserIdentity::class.java)
        whenever(userIdentityHelper.updateFrom(myIdentity)).thenReturn(fromUser)

        val summary = helper.createInitialTransactionForCompletedBroadcast(completedBroadcastDTO)

        whenever(summary.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        verify(transaction).wallet = wallet
        verify(transaction).txid = txid
        verify(transaction).memPoolState = MemPoolState.PENDING
        verify(transaction).numConfirmations = 0
        verify(transaction).txTime = currentTimeInMillsec
        verify(daoSessionManager).insert(transaction)
        verify(transactionsInvitesSummary).update()
        verify(transactionsInvitesSummary).toUser = toUser
        verify(transactionsInvitesSummary).fromUser = fromUser

        assertThat(summary, equalTo(transaction))

        helper.createInitialTransactionForCompletedBroadcast(completedBroadcastDTO)
    }

    private fun setupFundingStatMempoolTestSuit(sampleTransaction: TransactionSummary, sampleMemPoolState: MemPoolState, sampleFundingStat: FundingStat): VIn {
        val sampleAddress = "some address"
        val sampleSatoshiValue = 500L
        val index = 7
        val `in` = buildSampleVIn(sampleAddress, sampleSatoshiValue, index)
        setupFundingStatDao(sampleFundingStat)
        setupAddressDao(null)
        whenever(sampleTransaction.memPoolState).thenReturn(sampleMemPoolState)

        return `in`
    }

    private fun setupTargetStatMempoolTestSuit(sampleTransaction: TransactionSummary, sampleMemPoolState: MemPoolState, sampleTargetStat: TargetStat): VOut {
        val sampleAddress = "some address"
        val sampleSatoshiValue = 500L
        val index = 7
        val out = buildSampleVOut(sampleAddress, sampleSatoshiValue, index)
        setupTargetStatDao(sampleTargetStat)
        setupAddressDao(null)
        whenever(sampleTransaction.memPoolState).thenReturn(sampleMemPoolState)

        return out
    }

    private fun buildSampleTransactionSummary(): TransactionSummary {
        val sampleTxTime = 654654654654L
        val transaction = mock(TransactionSummary::class.java)
        whenever(transaction.txTime).thenReturn(sampleTxTime)
        return transaction
    }

    private fun buildSampleVIn(sampleAddress: String, sampleSatoshiValue: Long, index: Int): VIn {

        val `in` = mock(VIn::class.java)
        val previous = buildSampleVOut(sampleAddress, sampleSatoshiValue, index)
        whenever(`in`.previousOutput).thenReturn(previous)

        return `in`
    }

    private fun buildSampleVOut(sampleAddress: String, sampleSatoshiValue: Long, index: Int): VOut {
        val out = mock(VOut::class.java)
        val pubKey = mock(ScriptPubKey::class.java)

        val addresses = arrayOf(sampleAddress)
        whenever(pubKey.addresses).thenReturn(addresses)

        whenever(out.scriptPubKey).thenReturn(pubKey)
        whenever(out.value).thenReturn(sampleSatoshiValue)
        whenever(out.index).thenReturn(index)

        return out
    }

    private fun setupAddressDao(sampledbAddress: Address?) {
        val dao = mock(AddressDao::class.java)

        val query = mock(QueryBuilder<*>::class.java)
        whenever(daoSessionManager.addressDao).thenReturn(dao)
        whenever(dao.queryBuilder()).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any())).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any(), *ArgumentMatchers.any())).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(query)
        whenever(query.limit(1)).thenReturn(query)
        whenever(query.unique()).thenReturn(sampledbAddress)
    }

    private fun setupFundingStatDao(sampleFundingStat: FundingStat) {
        val dao = mock(FundingStatDao::class.java)

        val query = mock(QueryBuilder<*>::class.java)
        whenever(daoSessionManager.fundingStatDao).thenReturn(dao)
        whenever(dao.queryBuilder()).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(query)
        whenever(query.limit(1)).thenReturn(query)
        whenever(query.unique()).thenReturn(sampleFundingStat)
    }

    private fun setupTargetStatDao(sampleTargetStat: TargetStat) {
        val dao = mock(TargetStatDao::class.java)

        val query = mock(QueryBuilder<*>::class.java)
        whenever(daoSessionManager.targetStatDao).thenReturn(dao)
        whenever(dao.queryBuilder()).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any())).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any(), *ArgumentMatchers.any())).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(query)
        whenever(query.limit(1)).thenReturn(query)
        whenever(query.unique()).thenReturn(sampleTargetStat)
    }

    private fun setupTransactionSummaryDao(sampleTransaction: TransactionSummary?): TransactionSummaryDao {
        val dao = mock(TransactionSummaryDao::class.java)

        val query = mock(QueryBuilder<*>::class.java)
        whenever(daoSessionManager.transactionSummaryDao).thenReturn(dao)
        whenever(dao.queryBuilder()).thenReturn(query)
        whenever(query.where(ArgumentMatchers.any())).thenReturn(query)
        whenever(query.limit(1)).thenReturn(query)
        whenever(query.unique()).thenReturn(sampleTransaction)
        return dao
    }

    private fun buildMockTransactionDetails(txID: String): TransactionDetail {
        val transactionDetail = mock(TransactionDetail::class.java)
        whenever(transactionDetail.transactionId).thenReturn(txID)
        return transactionDetail
    }
     */

    companion object {

        private val RECEIVER_PHONE_STRING = "13305551111"
    }

}