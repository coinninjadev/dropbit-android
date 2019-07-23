package com.coinninja.coinkeeper.service.runner

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.analytics.Analytics
import javax.inject.Inject

@Mockable
class ReceivedInvitesStatusRunner @Inject constructor(
        internal val client: SignedCoinKeeperApiClient,
        internal val transactionHelper: TransactionHelper,
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val analytics: Analytics,
        internal val logger: CNLogger
) : Runnable {

    @Suppress("UNCHECKED_CAST")
    override fun run() {
        val response = client.receivedInvites
        if (response.isSuccessful) {
            val invites: List<ReceivedInvite>? = response.body() as List<ReceivedInvite>?
            invites?.let {
                saveCompletedInvites(it)
            }
        } else {
            logger.logError(TAG, RECEIVED_INVITE_FAILED, response)
        }

        cleanInviteJoinTable()
    }

    private fun saveCompletedInvites(invites: List<ReceivedInvite>) {
        invites.forEach {
            processInvite(it)
        }
    }

    private fun processInvite(cnInvite: ReceivedInvite) {
        val txid = cnInvite.txid ?: ""
        val cnId = cnInvite.id
        if (BTCState.from(cnInvite.status) != BTCState.FULFILLED || txid.isEmpty()) return

        inviteTransactionSummaryHelper.updateFulfilledInviteByCnId(cnId, txid)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, true)
    }

    //TODO Remove HACK
    private fun cleanInviteJoinTable() {
        val invites = transactionHelper.invitesWithTxID
        for (invite in invites) {
            val txID = invite.btcTransactionId
            val transaction = transactionHelper.getTransactionWithTxID(txID) ?: continue

            transactionHelper.joinInviteToTx(invite, transaction)
        }
    }

    companion object {
        internal val TAG = ReceivedInvitesStatusRunner::class.java.simpleName
        internal val RECEIVED_INVITE_FAILED = "|---- Received Invite failed"
    }
}