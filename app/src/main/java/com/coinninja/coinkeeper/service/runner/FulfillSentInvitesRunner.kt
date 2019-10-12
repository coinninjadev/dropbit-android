package com.coinninja.coinkeeper.service.runner


import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.isNotNullOrEmpty
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import javax.inject.Inject

@Mockable
class FulfillSentInvitesRunner @Inject internal constructor(
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val sentInvitesStatusGetter: SentInvitesStatusGetter,
        internal val sentInvitesStatusSender: SentInvitesStatusSender,
        internal val broadcastBtcInviteRunner: BroadcastBtcInviteRunner,
        internal val thunderDomeRepository: ThunderDomeRepository,
        internal val cnClient: SignedCoinKeeperApiClient
) : Runnable {

    override fun run() {

        //Step 1. grab all sent invites from server, save/update the ones that now have an address
        sentInvitesStatusGetter.run()

        //Step 2. addressForPubKey any sent invites that do not have a tx id but have an address
        broadcastInvitesOnBlockchain(inviteTransactionSummaryHelper.unfulfilledSentInvites)
        broadcastInvitesOnThunderDome(inviteTransactionSummaryHelper.unfulfilledLightningSentInvites)

        //Step 3. report to coinninja server of any invites that have been newly fulfilled (with TX ID)
        sentInvitesStatusSender.run()
    }

    internal fun broadcastInvitesOnThunderDome(invites: List<InviteTransactionSummary>) {
        invites.forEach { invite ->
            if (invite.address.isNotNullOrEmpty()) {
                thunderDomeRepository.pay(invite.address, invite.valueSatoshis)?.let { invoice ->
                    invoice.id.let { id ->
                        invite.btcState = BTCState.FULFILLED
                        invite.update()
                        cnClient.updateInviteStatusCompleted(invite.serverId, id.split(":")[0])
                    }

                }
            }
        }
    }

    private fun broadcastInvitesOnBlockchain(unfulfilledTransactions: List<InviteTransactionSummary>) {
        for (transaction in unfulfilledTransactions) {
            if (alreadyHasTxId(transaction)) continue

            broadcastBtcInviteRunner.invite = transaction
            broadcastBtcInviteRunner.run()
        }
    }

    fun alreadyHasTxId(transaction: InviteTransactionSummary): Boolean {
        val currentTXID = transaction.btcTransactionId
        return !(currentTXID == null || currentTXID.isEmpty())
    }
}
