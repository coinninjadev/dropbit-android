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
        var summary: TransactionsInvitesSummary? =
                transactionInviteSummaryQueryManager
                        .getTransactionInviteSummaryByTransactionSummary(transaction)

        return if (summary == null) {
            summary = daoSessionManager.newTransactionInviteSummary()
            summary.transactionSummary = transaction
            daoSessionManager.insert(summary)
            summary
        } else {
            summary
        }
    }
}
