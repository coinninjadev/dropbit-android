package com.coinninja.coinkeeper.cn.dropbit

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import javax.inject.Inject

@Mockable
class DropBitCancellationManager @Inject internal constructor(
        internal val client: SignedCoinKeeperApiClient,
        internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
        internal val cnWalletManager: CNWalletManager
) {

    fun markAsCanceled(summaries: List<InviteTransactionSummary>) {
        for (invite in summaries) {
            markAsCanceled(invite)
        }
    }

    fun markAsCanceled(invite: InviteTransactionSummary) {
        val inviteID = invite.serverId

        val response = client.updateInviteStatusCanceled(inviteID)
        if (response.isSuccessful) {
            inviteTransactionSummaryHelper.cancelInviteByCnId(inviteID)
            cnWalletManager.updateBalances()
        }
    }

    fun markAsCanceled(id: String) {
        inviteTransactionSummaryHelper.getInviteSummaryByCnId(id)?.let {
            markAsCanceled(it)
        }

    }

    fun markUnfulfilledAsCanceled() {
        markAsCanceled(inviteTransactionSummaryHelper.unfulfilledSentInvites)
        cnWalletManager.updateBalances()
    }
}
