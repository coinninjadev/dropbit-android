package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.bindings.TransactionBroadcastResult
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.coinkeeper.util.DateUtil
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import javax.inject.Inject

@Mockable
class InviteTransactionSummaryHelper @Inject internal
constructor(internal val inviteSummaryQueryManager: InviteSummaryQueryManager,
            internal val transactionInviteSummaryHelper: TransactionInviteSummaryHelper,
            internal val daoSessionManager: DaoSessionManager,
            internal val transactionHelper: TransactionHelper,
            internal val dropbitAccountHelper: DropbitAccountHelper,
            internal val userIdentityHelper: UserIdentityHelper,
            internal val walletHelper: WalletHelper,
            internal val dateUtil: DateUtil
) {

    val allUnacknowledgedInvitations: List<InviteTransactionSummary> get() = inviteSummaryQueryManager.allUnacknowledgedInvitations
    val unfulfilledSentInvites: List<InviteTransactionSummary> get() = inviteSummaryQueryManager.unfulfilledSentInvites

    fun getInviteSummaryById(id: String): InviteTransactionSummary? {
        return inviteSummaryQueryManager.getInviteSummaryByCnId(id)
    }

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
        invite.btcState = BTCState.UNACKNOWLEDGED
        invite.type = Type.SENT
        invite.update()

        return invite
    }

    private fun acknowledgeSentInvite(invite: InviteTransactionSummary, cnId: String) {
        invite.serverId = cnId
        invite.sentDate = dateUtil.getCurrentTimeInMillis()
        invite.btcState = BTCState.UNFULFILLED
        invite.update()
        transactionInviteSummaryHelper.getOrCreateParentSettlementFor(invite)
    }

    fun acknowledgeInviteTransactionSummary(completedInviteDTO: CompletedInviteDTO): InviteTransactionSummary? {
        val serverId = completedInviteDTO.cnId ?: ""
        if (serverId.isEmpty()) return null

        val invite = inviteSummaryQueryManager.getInviteSummaryByCnId(completedInviteDTO.requestId)
        invite?.let {
            acknowledgeSentInvite(it, serverId)
        }
        return invite
    }


    fun acknowledgeInviteTransactionSummary(sentInvite: SentInvite) {
        val invite = inviteSummaryQueryManager.getInviteSummaryByCnId(sentInvite.metadata.request_id)
        invite?.let {
            acknowledgeSentInvite(it, sentInvite.id)
        }
    }

    fun updateFulfilledInvite(invite: InviteTransactionSummary, transactionBroadcastResult: TransactionBroadcastResult) {
        val txid = transactionBroadcastResult.txId
        invite.apply {
            btcTransactionId = txid
            btcState = BTCState.FULFILLED
            update()
        }
        transactionInviteSummaryHelper.populateWith(invite.transactionsInvitesSummary, invite)
    }

    fun cancelInvite(invite: InviteTransactionSummary) {
        invite.btcState = BTCState.CANCELED
        invite.update()
    }

    fun cancelPendingSentInvites() {
        unfulfilledSentInvites.forEach {
            cancelInvite(it)
        }
    }

    fun saveReceivedInviteTransaction(receivedInvite: ReceivedInvite) =
            inviteSummaryQueryManager.getOrCreate(receivedInvite.id).also {
                it.btcState = BTCState.from(receivedInvite.status)
                it.historicValue = receivedInvite.metadata.amount.usd
                it.toUser = userIdentityHelper.updateFrom(receivedInvite.metadata.receiver)
                it.fromUser = userIdentityHelper.updateFrom(receivedInvite.metadata.sender)
                it.sentDate = receivedInvite.created_at_millis
                it.valueSatoshis = receivedInvite.metadata.amount.btc
                it.valueFeesSatoshis = 0L
                it.wallet = walletHelper.wallet
                it.address = receivedInvite.address
                it.btcTransactionId = receivedInvite.txid
                it.type = Type.RECEIVED
                it.update()
                transactionInviteSummaryHelper.getOrCreateParentSettlementFor(it)
            }

    fun updateInviteAddressTransaction(cnId: String, address: String) {
        inviteSummaryQueryManager.getInviteSummaryByCnId(cnId)?.let {
            it.address = address
            it.update()
        }

    }

    fun updateInviteAddressTransaction(sentInvite: SentInvite): InviteTransactionSummary? {
        val invite: InviteTransactionSummary? = inviteSummaryQueryManager.getInviteSummaryByCnId(sentInvite.id)
        invite?.let {
            it.btcState = BTCState.from(sentInvite.status)
            it.pubkey = sentInvite.addressPubKey
            it.address = sentInvite.address
            transactionInviteSummaryHelper.updateSentTimeFrom(it)
            it.update()
        }
        return invite
    }
}
