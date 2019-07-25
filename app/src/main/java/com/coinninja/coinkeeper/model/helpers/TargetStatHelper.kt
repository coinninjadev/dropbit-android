package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.HDWallet
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference
import com.coinninja.coinkeeper.model.db.TargetStat
import com.coinninja.coinkeeper.model.db.TargetStatDao
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.service.client.model.VOut
import java.util.*
import javax.inject.Inject

@Mockable
class TargetStatHelper @Inject constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val walletHelper: WalletHelper,
        internal val dustProtectionPreference: DustProtectionPreference
) {

    fun targetStatFor(transactionId: Long, output: VOut): TargetStat? =
            daoSessionManager.targetStatDao.queryBuilder()
                    .where(
                            TargetStatDao.Properties.Tsid.eq(transactionId),
                            TargetStatDao.Properties.Value.eq(output.value),
                            TargetStatDao.Properties.Position.eq(output.index),
                            TargetStatDao.Properties.Addr.eq(output.scriptPubKey.addresses[0])
                    )
                    .unique()

    fun getOrCreateTargetStat(transaction: TransactionSummary, output: VOut): TargetStat =
            (targetStatFor(transaction.id, output) ?: daoSessionManager.newTargetStat()).also {
                it.addr = output.scriptPubKey.addresses[0]
                it.position = output.index
                it.transaction = transaction
                it.value = output.value
                it.txTime = transaction.txTime
                if (it.id > 0) {
                    it.update()
                } else {
                    daoSessionManager.insert(it)
                }
            }


    val spendableTargets: List<TargetStat>
        get() {
            val wallet = walletHelper.wallet
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
