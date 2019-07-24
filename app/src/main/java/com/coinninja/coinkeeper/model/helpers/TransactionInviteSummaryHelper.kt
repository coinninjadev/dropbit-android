package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager
import com.coinninja.coinkeeper.model.query.TransactionInviteSummaryQueryManager
import com.coinninja.coinkeeper.model.query.TransactionQueryManager
import javax.inject.Inject

@Mockable
class TransactionInviteSummaryHelper @Inject internal constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val transactionQueryManager: TransactionQueryManager,
        internal val inviteSummaryQueryManager: InviteSummaryQueryManager,
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

    fun getOrCreateParentSettlementFor(transaction: TransactionSummary): TransactionsInvitesSummary =
            transaction.transactionsInvitesSummary
                    ?: inviteSummaryQueryManager.getInviteSummaryByTxid(transaction.txid)?.transactionsInvitesSummary?.also {
                        transaction.transactionsInvitesSummary = it
                        transaction.update()
                        populateWith(it, transaction)
                    }

                    ?: daoSessionManager.newTransactionInviteSummary().also {
                        daoSessionManager.insert(it)
                        transaction.transactionsInvitesSummary = it
                        transaction.update()
                        populateWith(it, transaction)
                    }

    fun getOrCreateParentSettlementFor(invite: InviteTransactionSummary): TransactionsInvitesSummary =
            invite.transactionsInvitesSummary

                    ?: transactionQueryManager.transactionByTxid(invite.btcTransactionId)?.transactionsInvitesSummary?.also {
                        invite.transactionsInvitesSummary = it
                        invite.update()
                        populateWith(it, invite)
                        it.update()
                    }

                    ?: daoSessionManager.newTransactionInviteSummary().also {
                        daoSessionManager.insert(it)
                        invite.transactionsInvitesSummary = it
                        invite.update()
                        populateWith(it, invite)
                    }


    fun populateWith(settlement: TransactionsInvitesSummary, transaction: TransactionSummary) {
        settlement.transactionSummary = transaction
        settlement.transactionTxID = transaction.txid
        settlement.update()
        updateSentTimeFrom(transaction)
    }

    fun populateWith(settlement: TransactionsInvitesSummary, invite: InviteTransactionSummary) {
        settlement.inviteTransactionSummary = invite
        settlement.fromUser = invite.fromUser
        settlement.toUser = invite.toUser
        settlement.transactionTxID = invite.btcTransactionId
        settlement.update()
        updateSentTimeFrom(invite)
    }

    fun updateSentTimeFrom(transaction: TransactionSummary) {
        if (transaction.txTime > 0 && transaction.transactionsInvitesSummary?.inviteSummaryID == null) {
            val transactionsInvitesSummary = transaction.transactionsInvitesSummary
            transactionsInvitesSummary.inviteTime = 0
            transactionsInvitesSummary.btcTxTime = transaction.txTime
            transactionsInvitesSummary.update()
        }
    }

    fun updateSentTimeFrom(invite: InviteTransactionSummary) {
        val transactionsInvitesSummary = invite.transactionsInvitesSummary
        when (invite.btcState) {
            BTCState.CANCELED,
            BTCState.FULFILLED,
            BTCState.EXPIRED -> {
                transactionsInvitesSummary.btcTxTime = invite.sentDate
                transactionsInvitesSummary.inviteTime = 0
            }
            else -> {
                transactionsInvitesSummary.btcTxTime = 0
                transactionsInvitesSummary.inviteTime = invite.sentDate
            }
        }
        transactionsInvitesSummary.update()
    }
}
