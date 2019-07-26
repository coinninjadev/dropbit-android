package com.coinninja.coinkeeper.service.runner

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.coinninja.coinkeeper.util.CNLogger
import javax.inject.Inject

@Mockable
class GetIncomingInviteRunner @Inject constructor(
        internal val client: SignedCoinKeeperApiClient,
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val logger: CNLogger
) : Runnable {

    @Suppress("UNCHECKED_CAST")
    override fun run() {
        val response = client.receivedInvites
        if (response.isSuccessful) {
            (response.body() as List<ReceivedInvite>?)?.let {
                writeInvitesToDatabase(it)
            }
        } else {
            logger.logError(this::class.java.simpleName, "|---- Received Invite failed", response)
        }
    }

    private fun writeInvitesToDatabase(receivedInvites: List<ReceivedInvite>) {
        for (invite in receivedInvites) {
            inviteTransactionSummaryHelper.saveReceivedInviteTransaction(invite)
        }
    }

}
