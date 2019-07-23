package com.coinninja.coinkeeper.service.runner


import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import javax.inject.Inject

@Mockable
class FulfillSentInvitesRunner @Inject
internal constructor(internal val transactionHelper: TransactionHelper,
                     internal val sentInvitesStatusGetter: SentInvitesStatusGetter,
                     internal val sentInvitesStatusSender: SentInvitesStatusSender,
                     internal val broadcastBtcInviteRunner: BroadcastBtcInviteRunner) : Runnable {

    override fun run() {

        //Step 1. grab all sent invites from server, save/update the ones that now have an address
        sentInvitesStatusGetter.run()

        //Step 2. addressForPubKey any sent invites that do not have a tx id but have an address
        val unfulfilledTransactions = transactionHelper.gatherUnfulfilledInviteTrans()
        broadcastRealTxForInvites(unfulfilledTransactions)

        //Step 3. report to coinninja server of any invites that have been newly fulfilled (with TX ID)
        sentInvitesStatusSender.run()
    }

    private fun broadcastRealTxForInvites(unfulfilledTransactions: List<InviteTransactionSummary>) {
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
