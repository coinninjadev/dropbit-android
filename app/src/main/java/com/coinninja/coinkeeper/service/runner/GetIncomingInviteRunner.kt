package com.coinninja.coinkeeper.service.runner

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.enums.Type
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite
import com.coinninja.coinkeeper.util.CNLogger
import javax.inject.Inject

@Mockable
class GetIncomingInviteRunner @Inject constructor(
        internal val client: SignedCoinKeeperApiClient,
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val thunderDomeRepository: ThunderDomeRepository,
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

    internal fun writeInvitesToDatabase(receivedInvites: List<ReceivedInvite>) {
        for (invite in receivedInvites) {
            saveInvite(invite)
        }
    }

    internal fun saveInvite(invite: ReceivedInvite) {
        val invite = inviteTransactionSummaryHelper.saveReceivedInviteTransaction(invite)
        if (invite.type == Type.LIGHTNING_RECEIVED) {
            thunderDomeRepository.createSettlementForInvite(invite.id, invite.toUser.id, invite.fromUser.id, invite.sentDate)
        }
    }

}
