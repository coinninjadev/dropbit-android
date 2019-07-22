package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.TransactionBroadcastResult
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.BTCState.UNACKNOWLEDGED
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager
import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.coinkeeper.util.DateUtil
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import javax.inject.Inject

@Mockable
class InviteTransactionSummaryHelper @Inject internal
constructor(internal val inviteSummaryQueryManager: InviteSummaryQueryManager,
            internal val daoSessionManager: DaoSessionManager,
            internal val transactionHelper: TransactionHelper,
            internal val dropbitAccountHelper: DropbitAccountHelper,
            internal val userIdentityHelper: UserIdentityHelper,
            internal val walletHelper: WalletHelper,
            internal val dateUtil: DateUtil
) {

    val allUnacknowledgedInvitations: List<InviteTransactionSummary> get() = inviteSummaryQueryManager.allUnacknowledgedInvitations
    val unfulfilledSentInvites: List<InviteTransactionSummary> get() = inviteSummaryQueryManager.unfulfilledSentInvites

    internal fun getOrCreateInviteSummaryWithServerId(cnId: String): TransactionsInvitesSummary {
        val inviteTransactionSummary = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId)
                ?: return createInviteTransactionSummaryWithParent(cnId)
        return inviteTransactionSummary.transactionsInvitesSummary
    }

    private fun createInviteTransactionSummaryWithParent(cnId: String): TransactionsInvitesSummary {
        val transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary()
        val inviteTransactionSummary = createInviteTransactionSummary(cnId)

        val joinId = daoSessionManager.insert(transactionsInvitesSummary)
        val inviteId = daoSessionManager.insert(inviteTransactionSummary)
        inviteTransactionSummary.transactionsInvitesSummaryID = joinId
        transactionsInvitesSummary.inviteSummaryID = inviteId
        transactionsInvitesSummary.update()
        inviteTransactionSummary.update()

        return transactionsInvitesSummary
    }

    private fun createInviteTransactionSummary(cnId: String): InviteTransactionSummary {
        val inviteTransactionSummary = daoSessionManager.newInviteTransactionSummary()
        inviteTransactionSummary.serverId = cnId
        daoSessionManager.insert(inviteTransactionSummary)

        return inviteTransactionSummary
    }

    fun saveTemporaryInvite(pendingInviteDTO: PendingInviteDTO): InviteTransactionSummary {
        val invite = createInviteTransactionSummary(pendingInviteDTO.requestId)
        val conversionCurrency = USDCurrency(pendingInviteDTO.bitcoinPrice)
        val btcCurrency = BTCCurrency(pendingInviteDTO.inviteAmount + pendingInviteDTO.inviteFee)
        val totalUsdSpending = btcCurrency.toUSD(conversionCurrency)

        val dropbitMeIdentity = dropbitAccountHelper.identityForType(pendingInviteDTO.identity.identityType)
        dropbitMeIdentity?.let {
            val fromUser = userIdentityHelper.updateFrom(dropbitMeIdentity)
            invite.fromUser = fromUser
        }

        val toUser = userIdentityHelper.updateFrom(pendingInviteDTO.identity)
        invite.toUser = toUser
        invite.historicValue = totalUsdSpending.toLong()
        invite.valueSatoshis = pendingInviteDTO.inviteAmount
        invite.valueFeesSatoshis = pendingInviteDTO.inviteFee
        invite.wallet = walletHelper.wallet
        invite.btcState = UNACKNOWLEDGED
        invite.type = Type.SENT
        invite.update()

        return invite
    }

    fun acknowledgeInviteTransactionSummary(completedInviteDTO: CompletedInviteDTO): InviteTransactionSummary? {
        inviteSummaryQueryManager.getInviteSummaryByCnId(completedInviteDTO.requestId)?.let { invite ->
            val transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary()
            transactionsInvitesSummary.inviteTime = completedInviteDTO.invitedContact!!.createdAt
            daoSessionManager.insert(transactionsInvitesSummary)

            transactionsInvitesSummary.inviteSummaryID = invite.id
            invite.apply {
                this.transactionsInvitesSummary = transactionsInvitesSummary
                serverId = completedInviteDTO.cnId
                sentDate = completedInviteDTO.invitedContact!!.createdAt
                btcState = BTCState.from(completedInviteDTO.invitedContact!!.status)
                update()
            }
            transactionsInvitesSummary.update()
            return invite
        }
        return null
    }

    fun acknowledgeInviteTransactionSummary(sentInvite: SentInvite) {
        val invite = inviteSummaryQueryManager.getInviteSummaryByCnId(sentInvite.metadata.request_id)!!
        if (invite.btcState != UNACKNOWLEDGED) {
            return
        }

        val transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary()
        transactionsInvitesSummary.apply {
            inviteTime = sentInvite.created_at
            toUser = invite.toUser
            fromUser = invite.fromUser
        }
        daoSessionManager.insert(transactionsInvitesSummary)

        invite.apply {
            this.transactionsInvitesSummary = transactionsInvitesSummary
            serverId = sentInvite.id
            sentDate = sentInvite.created_at
            btcState = BTCState.from(sentInvite.status)
            update()
        }
    }

    fun updateFulfilledInvite(transactionsInvitesSummary: TransactionsInvitesSummary,
                              transactionBroadcastResult: TransactionBroadcastResult) {
        val txid = transactionBroadcastResult.txId
        val inviteTransactionSummary = transactionsInvitesSummary.inviteTransactionSummary
        inviteTransactionSummary.apply {
            btcTransactionId = txid
            btcState = BTCState.FULFILLED
            update()
        }
        transactionsInvitesSummary.apply {
            inviteTxID = txid
            transactionTxID = txid
            inviteTime = 0L
            btcTxTime = dateUtil.getCurrentTimeInMillis()
            update()
        }

        val transactionSummary = transactionHelper.createInitialTransaction(txid)
        transactionsInvitesSummary.transactionSummary = transactionSummary
    }

    fun getInviteSummaryById(id: String): InviteTransactionSummary? {
        return inviteSummaryQueryManager.getInviteSummaryByCnId(id)
    }

    fun cancelPendingSentInvites() {
        unfulfilledSentInvites.forEach {
            it.btcState = BTCState.CANCELED
            it.update()
        }
    }
}
