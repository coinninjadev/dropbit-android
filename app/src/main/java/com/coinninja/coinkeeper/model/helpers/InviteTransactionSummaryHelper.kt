package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
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
    val unfulfilledLightningSentInvites: List<InviteTransactionSummary> get() = inviteSummaryQueryManager.unfulfilledSentLightningInvites

    fun getInviteSummaryByCnId(id: String): InviteTransactionSummary? {
        return inviteSummaryQueryManager.getInviteSummaryByCnId(id)
    }

    internal fun getOrCreateInviteSummaryWithServerId(cnId: String): TransactionsInvitesSummary {
        val inviteTransactionSummary = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId)
                ?: return createInviteTransactionSummaryWithParent(cnId)
        return inviteTransactionSummary.transactionsInvitesSummary
    }

    val getInvitesWithTxID: List<InviteTransactionSummary> get() = inviteSummaryQueryManager.invitesWithTxid

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
        val invite = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId)
        if (invite == null) {
            val inviteTransactionSummary = daoSessionManager.newInviteTransactionSummary()
            inviteTransactionSummary.serverId = cnId
            daoSessionManager.insert(inviteTransactionSummary)

            return inviteTransactionSummary
        } else {
            return invite
        }
    }

    fun saveTemporaryInvite(toUser: Identity, inviteValue: Long, feeValue: Long, totalUsd: Long,
                            requestId: String, inviteType: Type): InviteTransactionSummary {

        val invite = createInviteTransactionSummary(requestId)
        val dropbitMeIdentity = dropbitAccountHelper.identityForType(toUser.identityType)
        dropbitMeIdentity?.let {
            val fromUser = userIdentityHelper.updateFrom(dropbitMeIdentity)
            invite.fromUser = fromUser
        }

        invite.toUser = userIdentityHelper.updateFrom(toUser)
        invite.historicValue = totalUsd
        invite.valueSatoshis = inviteValue
        invite.valueFeesSatoshis = feeValue
        invite.wallet = walletHelper.primaryWallet
        invite.btcState = BTCState.UNACKNOWLEDGED
        invite.type = inviteType
        invite.update()

        return invite
    }

    fun saveTemporaryInvite(pendingInviteDTO: PendingInviteDTO): InviteTransactionSummary {
        val conversionCurrency = USDCurrency(pendingInviteDTO.bitcoinPrice)
        val btcCurrency = BTCCurrency(pendingInviteDTO.inviteAmount + pendingInviteDTO.inviteFee)
        val totalUsdSpending = btcCurrency.toUSD(conversionCurrency).toLong()
        return saveTemporaryInvite(
                pendingInviteDTO.identity,
                pendingInviteDTO.inviteAmount,
                pendingInviteDTO.inviteFee,
                totalUsdSpending,
                pendingInviteDTO.requestId,
                Type.BLOCKCHAIN_SENT
        )

    }

    fun saveTemporaryInvite(paymentHolder: PaymentHolder): InviteTransactionSummary? {
        return paymentHolder.toUser?.let { user ->
            return saveTemporaryInvite(
                    user,
                    paymentHolder.cryptoCurrency.toLong(),
                    paymentHolder.transactionData.feeAmount,
                    paymentHolder.fiat.toLong(),
                    paymentHolder.requestId,
                    if (paymentHolder.accountMode == AccountMode.LIGHTNING)
                        Type.LIGHTNING_SENT
                    else
                        Type.BLOCKCHAIN_SENT
            )
        }
    }

    fun acknowledgeSentInvite(invite: InviteTransactionSummary, cnId: String): InviteTransactionSummary {
        val cnInvite = inviteSummaryQueryManager.getInviteSummaryByCnId(cnId)
        if (cnInvite == null) {
            invite.serverId = cnId
            invite.sentDate = dateUtil.getCurrentTimeInMillis()
            invite.btcState = BTCState.UNFULFILLED
            invite.update()
            if (invite.type == Type.BLOCKCHAIN_SENT)
                transactionInviteSummaryHelper.getOrCreateParentSettlementFor(invite)
            return invite
        } else {
            cnInvite.sentDate = dateUtil.getCurrentTimeInMillis()
            cnInvite.btcState = BTCState.UNFULFILLED
            cnInvite.update()
            if (cnInvite.type == Type.BLOCKCHAIN_SENT)
                transactionInviteSummaryHelper.getOrCreateParentSettlementFor(invite)
            return cnInvite
        }
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

    private fun updateInviteAsFulfilled(txid: String, invite: InviteTransactionSummary) {
        invite.btcTransactionId = txid
        invite.btcState = BTCState.FULFILLED
        invite.update()
        if (invite.type == Type.BLOCKCHAIN_SENT || invite.type == Type.BLOCKCHAIN_RECEIVED)
            transactionInviteSummaryHelper.populateWith(invite.transactionsInvitesSummary, invite)
    }

    fun updateFulfilledInvite(invite: InviteTransactionSummary, txid: String) {
        updateInviteAsFulfilled(txid, invite)
    }

    fun updateFulfilledInviteByCnId(cnId: String, txid: String) {
        getInviteSummaryByCnId(cnId)?.let {
            updateInviteAsFulfilled(txid, it)
        }
    }

    fun cancelInvite(invite: InviteTransactionSummary) {
        invite.btcState = BTCState.CANCELED
        invite.btcTransactionId = ""
        invite.update()
    }

    fun cancelInviteByCnId(cnId: String) {
        getInviteSummaryByCnId(cnId)?.let {
            cancelInvite(it)
        }
    }

    fun cancelPendingSentInvites() {
        unfulfilledSentInvites.forEach {
            cancelInvite(it)
        }
    }

    fun saveReceivedInviteTransaction(receivedInvite: ReceivedInvite): InviteTransactionSummary? {
        val btcState = BTCState.from(receivedInvite.status)
        if (btcState == BTCState.FULFILLED || btcState == BTCState.UNFULFILLED) {
            return inviteSummaryQueryManager.getOrCreate(receivedInvite.id).also {
                val type = Type.receivedFrom(receivedInvite.address_type)
                it.btcState = btcState
                it.historicValue = receivedInvite.metadata.amount.usd
                it.toUser = userIdentityHelper.updateFrom(receivedInvite.metadata.receiver)
                it.fromUser = userIdentityHelper.updateFrom(receivedInvite.metadata.sender)
                it.sentDate = receivedInvite.created_at_millis
                it.valueSatoshis = receivedInvite.metadata.amount.btc
                it.valueFeesSatoshis = 0L
                it.wallet = walletHelper.primaryWallet
                it.address = receivedInvite.address
                it.btcTransactionId = receivedInvite.txid
                it.type = type
                it.update()

                if (type == Type.BLOCKCHAIN_RECEIVED)
                    transactionInviteSummaryHelper.getOrCreateParentSettlementFor(it)
            }
        }
        return null
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
            it.pubkey = sentInvite.addressPubKey
            it.address = sentInvite.address
            it.btcTransactionId = sentInvite.txid

            if (it.type == Type.BLOCKCHAIN_SENT || it.type == Type.BLOCKCHAIN_RECEIVED)
                transactionInviteSummaryHelper.updateSentTimeFrom(it)
            it.update()
        }
        return invite
    }
}
