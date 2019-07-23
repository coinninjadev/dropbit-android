package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.query.TransactionInviteSummaryQueryManager
import com.coinninja.coinkeeper.model.query.TransactionQueryManager
import javax.inject.Inject

@Mockable
class TransactionInviteSummaryHelper @Inject internal constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val transactionQueryManager: TransactionQueryManager,
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

    fun getOrCreateParentSettlementFor(invite: InviteTransactionSummary): TransactionsInvitesSummary =
            invite.transactionsInvitesSummary

                    ?: transactionQueryManager.transactionByTxid(invite.btcTransactionId)?.transactionsInvitesSummary?.also {
                        populateWith(it, invite)
                        it.update()
                    }

                    ?: daoSessionManager.newTransactionInviteSummary().also {
                        populateWith(it, invite)
                        daoSessionManager.insert(it)
                    }


    fun populateWith(settlement: TransactionsInvitesSummary, invite: InviteTransactionSummary) {
        settlement.inviteTransactionSummary = invite
        settlement.fromUser = invite.fromUser
        settlement.toUser = invite.toUser

        when (invite.btcState) {
            BTCState.CANCELED,
            BTCState.FULFILLED,
            BTCState.EXPIRED -> settlement.btcTxTime = invite.sentDate
            else -> settlement.inviteTime = invite.sentDate
        }
    }
}
