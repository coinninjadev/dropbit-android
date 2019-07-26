package com.coinninja.coinkeeper.model.helpers

import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionData
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.service.client.model.ScriptPubKey
import com.coinninja.coinkeeper.service.client.model.VOut
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.greenrobot.greendao.query.Join
import org.greenrobot.greendao.query.QueryBuilder
import org.greenrobot.greendao.query.WhereCondition
import org.junit.Test
import org.mockito.ArgumentCaptor

class TargetStatHelperTest {

    private fun createHelper(): TargetStatHelper {
        val statHelper = TargetStatHelper(mock(), mock(), mock(), mock())
        whenever(statHelper.walletHelper.wallet).thenReturn(mock())
        whenever(statHelper.walletHelper.wallet.id).thenReturn(1L)
        return statHelper
    }

    @Test
    fun updates_existing_output_with_current_details() {
        val helper = createHelper()
        val transaction = mock<TransactionSummary>()
        val output = VOut(
                value = 1000,
                index = 1,
                scriptPubKey = ScriptPubKey(
                        addresses = arrayOf("--address-1--")
                )
        )
        val targetStat = mock<TargetStat>()
        whenever(targetStat.id).thenReturn(1)
        whenever(transaction.txTime).thenReturn(System.currentTimeMillis())
        val queryBuilder: QueryBuilder<TargetStat> = mock()
        whenever(helper.daoSessionManager.targetStatDao).thenReturn(mock())
        whenever(helper.daoSessionManager.targetStatDao.queryBuilder()).thenReturn(queryBuilder)
        whenever(queryBuilder.where(any(), any(), any(), any())).thenReturn(queryBuilder)
        whenever(queryBuilder.limit(1)).thenReturn(queryBuilder)
        whenever(queryBuilder.unique()).thenReturn(targetStat)

        helper.getOrCreateTargetStat(transaction, output)

        val ordered = inOrder(targetStat)
        ordered.verify(targetStat).addr = "--address-1--"
        ordered.verify(targetStat).position = 1
        ordered.verify(targetStat).transaction = transaction
        ordered.verify(targetStat).value = 1000
        ordered.verify(targetStat).txTime = transaction.txTime
        ordered.verify(targetStat).update()
    }

    @Test
    fun inserts_new_output_with_details() {
        val helper = createHelper()
        val transaction = mock<TransactionSummary>()
        whenever(transaction.txTime).thenReturn(System.currentTimeMillis())
        val output = VOut(
                value = 1000,
                index = 1,
                scriptPubKey = ScriptPubKey(
                        addresses = arrayOf("--address-1--")
                )
        )
        val targetStat = mock<TargetStat>()
        val queryBuilder: QueryBuilder<TargetStat> = mock()
        whenever(helper.daoSessionManager.targetStatDao).thenReturn(mock())
        whenever(helper.daoSessionManager.targetStatDao.queryBuilder()).thenReturn(queryBuilder)
        whenever(queryBuilder.where(any(), any(), any(), any())).thenReturn(queryBuilder)
        whenever(queryBuilder.limit(1)).thenReturn(queryBuilder)
        whenever(queryBuilder.unique()).thenReturn(null)
        whenever(targetStat.id).thenReturn(null)

        whenever(helper.daoSessionManager.newTargetStat()).thenReturn(targetStat)

        helper.getOrCreateTargetStat(transaction, output)

        val ordered = inOrder(targetStat, helper.daoSessionManager)
        ordered.verify(helper.daoSessionManager).newTargetStat()
        ordered.verify(targetStat).addr = "--address-1--"
        ordered.verify(targetStat).position = 1
        ordered.verify(targetStat).transaction = transaction
        ordered.verify(targetStat).value = 1000
        ordered.verify(targetStat).txTime = transaction.txTime
        ordered.verify(helper.daoSessionManager).insert(targetStat)
    }

    @Test
    fun convertsToUnspentTransactionOutput() {
        val targetStat: TargetStat = mock()
        whenever(targetStat.toUnspentTransactionOutput()).thenCallRealMethod()
        whenever(targetStat.value).thenReturn(1000L)
        whenever(targetStat.position).thenReturn(1)
        whenever(targetStat.transaction).thenReturn(mock())
        whenever(targetStat.transaction.txid).thenReturn("--txid--")
        whenever(targetStat.transaction.isReplaceable).thenReturn(true)
        val address = Address()
        address.changeIndex = 1
        address.index = 50
        whenever(targetStat.address).thenReturn(address)

        val unspentTransactionOutput = targetStat.toUnspentTransactionOutput()

        assertThat(unspentTransactionOutput.amount).isEqualTo(1000L)
        assertThat(unspentTransactionOutput.index).isEqualTo(1)
        assertThat(unspentTransactionOutput.isReplaceable).isEqualTo(true)
        assertThat(unspentTransactionOutput.txId).isEqualTo("--txid--")
        assertThat(unspentTransactionOutput.path.purpose).isEqualTo(49)
        assertThat(unspentTransactionOutput.path.coinType).isEqualTo(0)
        assertThat(unspentTransactionOutput.path.account).isEqualTo(0)
        assertThat(unspentTransactionOutput.path.change).isEqualTo(1)
        assertThat(unspentTransactionOutput.path.index).isEqualTo(50)
    }

    @Test
    fun inits_query_rightly_with_dust_protection() {
        val targetStatHelper = createHelper()
        val join: Join<TargetStat, TransactionSummary> = mock()
        val target: TargetStat = mock()
        val address: Address = mock()
        val transactionSummary: TransactionSummary = mock()

        whenever(address.changeIndex).thenReturn(HDWallet.EXTERNAL)
        whenever(transactionSummary.numConfirmations).thenReturn(3)
        whenever(target.address).thenReturn(address)
        whenever(target.transaction).thenReturn(transactionSummary)
        val targets = mutableListOf(target)

        val qb: QueryBuilder<TargetStat> = mock()
        val targetStatDao: TargetStatDao = mock()

        whenever(targetStatHelper.dustProtectionPreference.isDustProtectionEnabled).thenReturn(true)
        whenever(targetStatHelper.daoSessionManager.targetStatDao).thenReturn(targetStatDao)
        whenever(qb.join(eq(TargetStatDao.Properties.Tsid), eq(TransactionSummary::class.java))).thenReturn(join)
        whenever(targetStatDao.queryBuilder()).thenReturn(qb)
        whenever(qb.where(any(), any(), any(), any(), any())).thenReturn(qb)
        whenever(qb.orderAsc(any())).thenReturn(qb)
        whenever(qb.list()).thenReturn(targets)


        targetStatHelper.spendableTargets


        val whereConditionArgumentCaptor: ArgumentCaptor<WhereCondition> = ArgumentCaptor.forClass(WhereCondition::class.java)
        val joinCaptor: ArgumentCaptor<WhereCondition> = ArgumentCaptor.forClass(WhereCondition::class.java)

        verify(qb).where(whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture())
        verify(join).where(joinCaptor.capture())

        val joinValues = joinCaptor.allValues
        assertThat((joinValues[0] as WhereCondition.PropertyCondition).op).isEqualTo(
                (TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.id,
                        MemPoolState.DOUBLE_SPEND.id,
                        MemPoolState.ORPHANED.id
                ) as WhereCondition.PropertyCondition).op)

        val whereValues = whereConditionArgumentCaptor.allValues
        assertThat((whereValues[0] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.id) as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[1] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.WalletId.eq(1L) as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[2] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.FundingId.isNull as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[3] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.Value.gt(9999L) as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[4] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.AddressId.isNotNull as WhereCondition.PropertyCondition).op)
    }

    @Test
    fun inits_query_rightly_without_dust_protection() {
        val targetStatHelper = createHelper()
        val target: TargetStat = mock()
        val join: Join<TargetStat, TransactionSummary> = mock()
        val address: Address = mock()
        val transactionSummary: TransactionSummary = mock()
        whenever(address.changeIndex).thenReturn(HDWallet.EXTERNAL)
        whenever(transactionSummary.numConfirmations).thenReturn(3)
        whenever(target.address).thenReturn(address)
        whenever(target.transaction).thenReturn(transactionSummary)
        val targets = mutableListOf(target)

        val qb: QueryBuilder<TargetStat> = mock()
        val targetStatDao: TargetStatDao = mock()

        whenever(targetStatHelper.dustProtectionPreference.isDustProtectionEnabled).thenReturn(false)
        whenever(targetStatHelper.daoSessionManager.targetStatDao).thenReturn(targetStatDao)
        whenever(qb.join(eq(TargetStatDao.Properties.Tsid), eq(TransactionSummary::class.java))).thenReturn(join)
        whenever(targetStatDao.queryBuilder()).thenReturn(qb)
        whenever(qb.where(any(), any(), any(), any(), any())).thenReturn(qb)
        whenever(qb.orderAsc(any())).thenReturn(qb)
        whenever(qb.list()).thenReturn(targets)


        targetStatHelper.spendableTargets


        val whereConditionArgumentCaptor = ArgumentCaptor.forClass(WhereCondition::class.java)
        val joinCaptor = ArgumentCaptor.forClass(WhereCondition::class.java)

        verify(qb).where(whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture(), whereConditionArgumentCaptor.capture())
        verify(join).where(joinCaptor.capture())


        val joinValues = joinCaptor.allValues
        assertThat((joinValues[0] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.id,
                        MemPoolState.DOUBLE_SPEND.id,
                        MemPoolState.ORPHANED.id
                ) as WhereCondition.PropertyCondition).op)

        val whereValues = whereConditionArgumentCaptor.allValues
        assertThat((whereValues[0] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.id) as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[1] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.WalletId.eq(1L) as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[2] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.FundingId.isNull as WhereCondition.PropertyCondition).op)
        assertThat((whereValues[3] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.Value.gt(10000) as WhereCondition.PropertyCondition).op)


        assertThat((whereValues[4] as WhereCondition.PropertyCondition).op)
                .isEqualTo((TargetStatDao.Properties.AddressId.isNotNull as WhereCondition.PropertyCondition).op)
    }

    @Test
    fun get_spendable_targets_test() {
        val targetStatHelper = createHelper()
        val target: TargetStat = mock()
        val join: Join<TargetStat, TransactionSummary> = mock()
        val address: Address = mock()
        val transactionSummary: TransactionSummary = mock()
        val qb: QueryBuilder<TargetStat> = mock()

        whenever(address.changeIndex).thenReturn(HDWallet.EXTERNAL)
        whenever(transactionSummary.numConfirmations).thenReturn(3)
        whenever(target.address).thenReturn(address)
        whenever(target.transaction).thenReturn(transactionSummary)
        val mockedTargets = mutableListOf(target)


        val targetStatDao: TargetStatDao = mock()
        whenever(targetStatHelper.daoSessionManager.targetStatDao).thenReturn(targetStatDao)
        whenever(targetStatDao.queryBuilder()).thenReturn(qb)
        whenever(qb.where(any(), any(), any(), any(), any())).thenReturn(qb)
        whenever(qb.join(TargetStatDao.Properties.Tsid, TransactionSummary::class.java)).thenReturn(join)
        whenever(qb.orderAsc(any())).thenReturn(qb)
        whenever(qb.list()).thenReturn(mockedTargets)


        val targets = targetStatHelper.spendableTargets

        assertThat(targets.size).isEqualTo(1)
        assertThat(targets[0]).isEqualTo(target)
    }

    @Test
    fun get_spendable_targets_test_remove_external_without_enough_confirmations() {
        val targetStatHelper = createHelper()
        val target: TargetStat = mock()
        val join: Join<TargetStat, TransactionSummary> = mock()
        val address: Address = mock()
        val transactionSummary: TransactionSummary = mock()
        val qb: QueryBuilder<TargetStat> = mock()
        whenever(address.changeIndex).thenReturn(HDWallet.EXTERNAL)
        whenever(transactionSummary.numConfirmations).thenReturn(0)
        whenever(target.address).thenReturn(address)
        whenever(target.transaction).thenReturn(transactionSummary)
        val mockTargets = mutableListOf(target)


        val targetStatDao: TargetStatDao = mock()
        whenever(targetStatHelper.daoSessionManager.targetStatDao).thenReturn(targetStatDao)
        whenever(targetStatDao.queryBuilder()).thenReturn(qb)
        whenever(qb.where(any(), any(), any(), any(), any())).thenReturn(qb)
        whenever(qb.join(TargetStatDao.Properties.Tsid, TransactionSummary::class.java)).thenReturn(join)
        whenever(qb.orderAsc(any())).thenReturn(qb)
        whenever(qb.list()).thenReturn(mockTargets)

        val targets = targetStatHelper.spendableTargets
        assertThat(targets.size).isEqualTo(0)
    }

    @Test
    fun creates_outputs_for_saved_transactions() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val changePath = DerivationPath(49, 0, 0, 1, 0)
        val transactionData = TransactionData(
                utxos = arrayOf(
                        UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                                DerivationPath(49, 0, 0, 0, 1))),
                amount = 1000, feeAmount = 10, changeAmount = 100000,
                changePath = changePath,
                paymentAddress = "--pay-to-address--"
        )
        val changeOutput = mock<TargetStat>()
        val receiverOutput = mock<TargetStat>()
        val address = mock<Address>()
        whenever(address.address).thenReturn("--change-address--")
        whenever(helper.daoSessionManager.newTargetStat()).thenReturn(receiverOutput).thenReturn(changeOutput)
        whenever(helper.addressHelper.addressForPath(changePath)).thenReturn(address)
        whenever(transaction.wallet).thenReturn(mock())

        helper.createOutputsFor(transaction, transactionData)

        val ordered = inOrder(changeOutput, receiverOutput, helper.daoSessionManager)
        ordered.verify(receiverOutput, times(0)).address = any()
        ordered.verify(receiverOutput).addr = "--pay-to-address--"
        ordered.verify(receiverOutput).position = 0
        ordered.verify(receiverOutput).value = transactionData.amount
        ordered.verify(receiverOutput).state = TargetStat.State.PENDING
        ordered.verify(receiverOutput).transaction = transaction
        ordered.verify(receiverOutput, times(0)).wallet = any()
        ordered.verify(helper.daoSessionManager).insert(receiverOutput)

        ordered.verify(changeOutput).address = address
        ordered.verify(changeOutput).addr = "--change-address--"
        ordered.verify(changeOutput).position = 1
        ordered.verify(changeOutput).value = transactionData.changeAmount
        ordered.verify(changeOutput).state = TargetStat.State.PENDING
        ordered.verify(changeOutput).transaction = transaction
        ordered.verify(changeOutput).wallet = transaction.wallet
        ordered.verify(helper.daoSessionManager).insert(changeOutput)
    }

    @Test
    fun creates_change_output_for_sent_transaction() {
        val helper = createHelper()
        val changePath = DerivationPath(49, 0, 0, 1, 0)
        val transactionData = TransactionData(
                utxos = arrayOf(
                        UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                                DerivationPath(49, 0, 0, 0, 1))),
                amount = 1000, feeAmount = 10, changeAmount = 100000,
                changePath = changePath,
                paymentAddress = "--pay-to-address--"
        )

        val output = mock<TargetStat>()
        val address = mock<Address>()
        whenever(address.address).thenReturn("--change-address--")
        whenever(helper.daoSessionManager.newTargetStat()).thenReturn(output)
        whenever(helper.addressHelper.addressForPath(changePath)).thenReturn(address)

        assertThat(helper.createChangeOutput(transactionData)).isEqualTo(output)

        val ordered = inOrder(output)
        ordered.verify(output).address = address
        ordered.verify(output).addr = "--change-address--"
        ordered.verify(output).position = 1
        ordered.verify(output).value = transactionData.changeAmount
    }

    @Test
    fun only_creates_change_output_when_there_is_change() {
        val helper = createHelper()
        val transactionData = TransactionData(
                utxos = arrayOf(
                        UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                                DerivationPath(49, 0, 0, 0, 1))),
                amount = 1000, feeAmount = 10, changeAmount = 0,
                changePath = DerivationPath(49, 0, 0, 1, 0),
                paymentAddress = "--pay-to-address--"
        )

        assertThat(helper.createChangeOutput(transactionData)).isNull()
    }

    @Test
    fun creates_payment_outputs_for_sent_transaction() {
        val helper = createHelper()
        val changePath = DerivationPath(49, 0, 0, 1, 0)
        val transactionData = TransactionData(
                utxos = arrayOf(
                        UnspentTransactionOutput("--proof-txid-1--", 1, 100000,
                                DerivationPath(49, 0, 0, 0, 1))),
                amount = 1000, feeAmount = 10, changeAmount = 100000,
                changePath = changePath,
                paymentAddress = "--pay-to-address--"
        )

        val output = mock<TargetStat>()
        whenever(helper.daoSessionManager.newTargetStat()).thenReturn(output)

        assertThat(helper.createOutputForReceiver(transactionData)).isEqualTo(output)

        val ordered = inOrder(output)
        ordered.verify(output, times(0)).address = any()
        ordered.verify(output).addr = "--pay-to-address--"
        ordered.verify(output).position = 0
        ordered.verify(output).value = transactionData.amount

    }
}