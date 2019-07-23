package com.coinninja.coinkeeper.model.query

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao.Properties
import com.coinninja.coinkeeper.model.db.enums.MemPoolState
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.util.DateUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Mockable
class TransactionQueryManager @Inject constructor(
        internal val dateUtil: DateUtil,
        internal val daoSessionManager: DaoSessionManager) {

    val incompleteTransactions: List<TransactionSummary>
        get() =
            daoSessionManager.transactionSummaryDao.queryBuilder()
                    .whereOr(Properties.MemPoolState.eq(MemPoolState.INIT.getId()),
                            Properties.MemPoolState.eq(MemPoolState.PENDING.getId()),
                            Properties.MemPoolState.eq(MemPoolState.ACKNOWLEDGE.getId())
                    ).list()

    val requiringNotificationCheck: List<TransactionSummary>
        get() =
            daoSessionManager.transactionSummaryDao.queryBuilder()
                    .where(Properties.SoughtNotification.eq(false))
                    .list()

    val transactionsWithoutHistoricPricing: List<TransactionSummary>
        get() =
            daoSessionManager.transactionSummaryDao.queryBuilder()
                    .where(Properties.HistoricPrice.eq(0L))
                    .list()

    val pendingMindedTransactions: List<TransactionSummary>
        get() =
            daoSessionManager.transactionSummaryDao.queryBuilder()
                    .where(
                            Properties.MemPoolState.eq(MemPoolState.MINED.getId()),
                            Properties.NumConfirmations.lt(6)
                    ).list()

    val transactionsWithoutFees: List<TransactionSummary>
        get() =
            daoSessionManager.transactionSummaryDao.queryBuilder().where(Properties.Fee.eq(0L)).list()

    fun pendingTransactionsOlderThan(olderThanSeconds: Long): List<TransactionSummary> =

            daoSessionManager.transactionSummaryDao.queryBuilder()
                    .whereOr(
                            Properties.MemPoolState.eq(MemPoolState.PENDING.getId()),
                            Properties.MemPoolState.eq(MemPoolState.INIT.getId())
                    ).where(
                            Properties.TxTime.le(calculatePastTimeFromNow(olderThanSeconds))
                    ).list()


    private fun calculatePastTimeFromNow(olderThanSeconds: Long): Long =
            dateUtil.getCurrentTimeInMillis() - TimeUnit.SECONDS.toMillis(olderThanSeconds)
}
