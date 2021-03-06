package com.coinninja.coinkeeper.model.helpers

import app.coinninja.cn.libbitcoin.HDWallet
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.service.client.model.VOut
import java.util.*
import javax.inject.Inject

@Mockable
class TargetStatHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val walletHelper: WalletHelper,
        internal val addressHelper: AddressHelper,
        internal val dustProtectionPreference: DustProtectionPreference
) {

    fun targetStatFor(transactionId: Long, output: VOut, address: String? = null): TargetStat? =
            daoSessionManager.targetStatDao.queryBuilder()
                    .where(
                            TargetStatDao.Properties.Tsid.eq(transactionId),
                            TargetStatDao.Properties.Value.eq(output.value),
                            TargetStatDao.Properties.Position.eq(output.index),
                            TargetStatDao.Properties.Addr.eq(address
                                    ?: output.scriptPubKey.addresses[0])
                    )
                    .unique()

    fun getOrCreateTargetStat(transaction: TransactionSummary, output: VOut, address: String? = null): TargetStat =
            (targetStatFor(transaction.id, output, address)
                    ?: daoSessionManager.newTargetStat()).also {
                it.addr = address ?: output.scriptPubKey.addresses[0]
                it.position = output.index
                it.transaction = transaction
                it.value = output.value
                it.txTime = transaction.txTime
                if (it.id == null) {
                    daoSessionManager.insert(it)
                } else {
                    it.update()
                }
            }

    internal fun createChangeOutput(wallet: Wallet, transactionData: TransactionData): TargetStat? {
        transactionData.changePath?.let { changePath ->
            addressHelper.addressForPath(wallet, changePath)?.let { changeAddress ->
                if (transactionData.changeAmount > 0) {
                    return daoSessionManager.newTargetStat().apply {
                        address = changeAddress
                        addr = changeAddress.address
                        position = 1
                        value = transactionData.changeAmount
                    }
                }
            }
        }
        return null
    }

    internal fun createOutputForReceiver(transactionData: TransactionData): TargetStat = daoSessionManager.newTargetStat().apply {
        addr = transactionData.paymentAddress
        position = 0
        value = transactionData.amount
    }

    fun createOutputsFor(transaction: TransactionSummary, transactionData: TransactionData) {
        createOutputForReceiver(transactionData).apply {
            state = TargetStat.State.PENDING
            this.transaction = transaction
            daoSessionManager.insert(this)
        }
        createChangeOutput(transaction.wallet, transactionData)?.apply {
            state = TargetStat.State.PENDING
            this.transaction = transaction
            wallet = transaction.wallet
            daoSessionManager.insert(this)
        }
    }


    val allUnspentOutputs: List<TargetStat>
        get() {
            val wallet = walletHelper.primaryWallet
            val dao = daoSessionManager.targetStatDao
            val queryBuilder = dao.queryBuilder()
            val transactionSummaryJoin = queryBuilder.join(TargetStatDao.Properties.Tsid, TransactionSummary::class.java)
            transactionSummaryJoin.where(
                    TransactionSummaryDao.Properties.MemPoolState.notIn(
                            MemPoolState.FAILED_TO_BROADCAST.id,
                            MemPoolState.DOUBLE_SPEND.id,
                            MemPoolState.ORPHANED
                    )
            )

            queryBuilder.where(
                    TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.id),
                    TargetStatDao.Properties.WalletId.eq(wallet.id),
                    TargetStatDao.Properties.FundingId.isNull,
                    TargetStatDao.Properties.Value.gt(spendableMinimum),
                    TargetStatDao.Properties.AddressId.isNotNull
            )


            queryBuilder.orderAsc(TargetStatDao.Properties.TxTime)
            return queryBuilder.list()
        }


    val spendableTargets: List<TargetStat>
        get() {
            val wallet = walletHelper.primaryWallet
            val dao = daoSessionManager.targetStatDao
            val queryBuilder = dao.queryBuilder()
            val transactionSummaryJoin = queryBuilder.join(TargetStatDao.Properties.Tsid, TransactionSummary::class.java)
            transactionSummaryJoin.where(
                    TransactionSummaryDao.Properties.MemPoolState.notIn(
                            MemPoolState.FAILED_TO_BROADCAST.id,
                            MemPoolState.DOUBLE_SPEND.id,
                            MemPoolState.ORPHANED
                    )
            )

            queryBuilder.where(
                    TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.id),
                    TargetStatDao.Properties.WalletId.eq(wallet.id),
                    TargetStatDao.Properties.FundingId.isNull,
                    TargetStatDao.Properties.Value.gt(spendableMinimum),
                    TargetStatDao.Properties.AddressId.isNotNull
            )


            queryBuilder.orderAsc(TargetStatDao.Properties.TxTime)
            val targetStats = queryBuilder.list()

            val toRemove = ArrayList<TargetStat>()

            targetStats.forEach { it ->
                if (it.address.changeIndex == HDWallet.EXTERNAL && it.transaction.numConfirmations < 1) {
                    toRemove.add(it)
                }
            }

            toRemove.forEach { targetStats.remove(it) }
            toRemove.clear()
            return targetStats
        }

    private val spendableMinimum: Long
        get() {
            var minimum = 0L

            if (dustProtectionPreference.isDustProtectionEnabled) {
                minimum = 999L
            }

            return minimum
        }
}
