package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.query.TransactionInviteSummaryQueryManager
import javax.inject.Inject

@Mockable
class TransactionInviteSummaryHelper @Inject internal constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val transactionInviteSummaryQueryManager: TransactionInviteSummaryQueryManager
) {


    fun getOrCreateTransactionInviteSummaryFor(transaction: TransactionSummary): TransactionsInvitesSummary {
        return transactionInviteSummaryQueryManager
                        .getTransactionInviteSummaryByTransactionSummary(transaction)
                        ?: daoSessionManager.newTransactionInviteSummary().also {
                            it.transactionSummary = transaction
                            it.btcTxTime = transaction.txTime
                            daoSessionManager.insert(it)
                        }

    }
}
