package com.coinninja.coinkeeper.service.runner

import android.annotation.SuppressLint
import android.content.Context
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.SentInvite
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import javax.inject.Inject

@Mockable
class SentInvitesStatusGetter @Inject
internal constructor(@ApplicationContext internal val context: Context,
                     internal val internalNotificationHelper: InternalNotificationHelper,
                     internal val client: SignedCoinKeeperApiClient,
                     internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
                     internal val cnLogger: CNLogger
) : Runnable {

    override fun run() {
        val response = client.sentInvites
        if (response.isSuccessful) {
            val sentInvites: MutableList<SentInvite> = response.body() as MutableList<SentInvite>
            updateSentInvitesDatabase(sentInvites = sentInvites)
        } else {
            cnLogger.logError(TAG, "|---- Get Sent Invites failed", response)
        }
    }

    private fun updateSentInvitesDatabase(sentInvites: MutableList<SentInvite>) {
        val serverInvitesWithNoLocalMatch = serverInvitesWithoutLocalMatch(sentInvites)
        for (sentInvite in serverInvitesWithNoLocalMatch) {
            client.updateInviteStatusCanceled(sentInvite.id)
            sentInvites.remove(sentInvite)
        }

        for (sentInvite in sentInvites) {
            if (BTCState.from(sentInvite.status) == BTCState.UNFULFILLED) {
                acknowledgeLocalInvitationIfNecessary(sentInvite)
                inviteTransactionSummaryHelper.updateInviteAddressTransaction(sentInvite)
                continue
            }

            val oldInvite = inviteTransactionSummaryHelper.getInviteSummaryByCnId(sentInvite.id)
            oldInvite?.let {
                val oldInviteBtcState = oldInvite.btcState
                val newInvite = inviteTransactionSummaryHelper.updateInviteAddressTransaction(sentInvite)
                newInvite?.let {
                    if (hasStateChanged(oldInviteBtcState, newInvite)) {
                        notifyUser(newInvite)
                    }
                }

            }
        }

        deleteAnyLocalInvitationsWithoutServerMatches(sentInvites)
    }

    private fun deleteAnyLocalInvitationsWithoutServerMatches(sentInvites: List<SentInvite>) {
        val serverIds = mutableMapOf<String, String>()

        for (invite in sentInvites) {
            serverIds[invite.id] = invite.id
        }

        val allUnacknowledgedInvitations = inviteTransactionSummaryHelper.allUnacknowledgedInvitations

        for (unacknowledgedInvitation in allUnacknowledgedInvitations) {
            if (!serverIds.containsKey(unacknowledgedInvitation.serverId)) {
                unacknowledgedInvitation.transactionNotification?.delete()
                unacknowledgedInvitation.delete()
            }
        }
    }

    fun serverInvitesWithoutLocalMatch(sentInvites: List<SentInvite>): List<SentInvite> {
        val noLocalMatchInvites = mutableListOf<SentInvite>()

        for (sentInvite in sentInvites) {
            if (inviteTransactionSummaryHelper.getInviteSummaryByCnId(sentInvite.id) == null && BTCState.from(sentInvite.status) != BTCState.CANCELED) {
                noLocalMatchInvites.add(sentInvite)
            }
        }

        return noLocalMatchInvites
    }

    private fun acknowledgeLocalInvitationIfNecessary(sentInvite: SentInvite) {
        inviteTransactionSummaryHelper.getInviteSummaryByCnId(sentInvite.metadata.request_id)?.let {
            inviteTransactionSummaryHelper.acknowledgeInviteTransactionSummary(sentInvite)
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun notifyUser(invite: InviteTransactionSummary) {
        if (invite.btcState == BTCState.EXPIRED) {
            internalNotificationHelper.addNotifications(
                    String.format(context.getString(R.string.invite_send_expired_message), getContact(invite)))
        } else if (invite.btcState == BTCState.CANCELED) {
            val btc = BTCCurrency(invite.valueSatoshis)
            btc.currencyFormat = BTCCurrency.ALT_CURRENCY_FORMAT
            internalNotificationHelper.addNotifications(
                    String.format(context.getString(R.string.invite_send_canceled_message),
                            getContact(invite), btc.toFormattedCurrency()))
        }
    }

    private fun getContact(invite: InviteTransactionSummary): String {
        return if (invite.type == Type.SENT) {
            invite.localeFriendlyDisplayIdentityForReceiver
        } else {
            invite.localeFriendlyDisplayIdentityForSender
        }
    }

    private fun hasStateChanged(oldState: BTCState, newInvite: InviteTransactionSummary): Boolean {
        return oldState != newInvite.btcState
    }

    companion object {
        private val TAG = SentInvitesStatusGetter::class.java.simpleName
    }
}