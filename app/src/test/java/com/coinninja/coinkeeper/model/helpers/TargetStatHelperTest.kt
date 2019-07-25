package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.service.client.model.ScriptPubKey
import com.coinninja.coinkeeper.service.client.model.VOut
import com.nhaarman.mockitokotlin2.*
import org.greenrobot.greendao.query.Join
import org.greenrobot.greendao.query.QueryBuilder
import org.greenrobot.greendao.query.WhereCondition
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.ArgumentCaptor

class TargetStatHelperTest {

    private fun createHelper(): TargetStatHelper {
        val statHelper = TargetStatHelper(mock(), mock(), mock())
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

        assertThat(unspentTransactionOutput.amount, equalTo(1000L))
        assertThat(unspentTransactionOutput.index, equalTo(1))
        assertThat(unspentTransactionOutput.isReplaceable, equalTo(true))
        assertThat(unspentTransactionOutput.txId, equalTo("--txid--"))
        assertThat(unspentTransactionOutput.path.purpose, equalTo(49))
        assertThat(unspentTransactionOutput.path.coinType, equalTo(0))
        assertThat(unspentTransactionOutput.path.account, equalTo(0))
        assertThat(unspentTransactionOutput.path.change, equalTo(1))
        assertThat(unspentTransactionOutput.path.index, equalTo(50))
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
        assertThat((joinValues[0] as WhereCondition.PropertyCondition).op,
                equalTo((TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.id,
                        MemPoolState.DOUBLE_SPEND.id,
                        MemPoolState.ORPHANED.id
                ) as WhereCondition.PropertyCondition).op))

        val whereValues = whereConditionArgumentCaptor.allValues
        assertThat((whereValues[0] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.id) as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[1] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.WalletId.eq(1L) as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[2] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.FundingId.isNull as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[3] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.Value.gt(9999L) as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[4] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.AddressId.isNotNull as WhereCondition.PropertyCondition).op))
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
        assertThat((joinValues[0] as WhereCondition.PropertyCondition).op,
                equalTo((TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.id,
                        MemPoolState.DOUBLE_SPEND.id,
                        MemPoolState.ORPHANED.id
                ) as WhereCondition.PropertyCondition).op))

        val whereValues = whereConditionArgumentCaptor.allValues
        assertThat((whereValues[0] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.id) as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[1] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.WalletId.eq(1L) as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[2] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.FundingId.isNull as WhereCondition.PropertyCondition).op))
        assertThat((whereValues[3] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.Value.gt(10000) as WhereCondition.PropertyCondition).op))


        assertThat((whereValues[4] as WhereCondition.PropertyCondition).op,
                equalTo((TargetStatDao.Properties.AddressId.isNotNull as WhereCondition.PropertyCondition).op))
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

        assertThat(targets.size, equalTo(1))
        assertThat(targets[0], equalTo(target))
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

        assertThat(targets.size, equalTo(0))
    }
}