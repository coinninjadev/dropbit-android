package com.coinninja.coinkeeper.service.runner

import android.content.Context
import app.dropbit.annotations.Mockable
import com.coinninja.bindings.TransactionBroadcastResult
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import javax.inject.Inject


@Mockable
class BroadcastBtcInviteRunner @Inject
internal constructor(@ApplicationContext internal val context: Context,
                     internal val cnWalletManager: CNWalletManager,
                     internal val transactionFundingManager: TransactionFundingManager,
                     internal val transactionNotificationManager: TransactionNotificationManager,
                     internal val inviteTransactionSummaryHelper: InviteTransactionSummaryHelper,
                     internal val transactionHelper: TransactionHelper,
                     internal val broadcastBtcInviteHelper: BroadcastBtcInviteHelper,
                     internal val broadcastHelper: BroadcastTransactionHelper,
                     internal val syncWalletManager: SyncWalletManager,
                     internal val externalNotificationHelper: ExternalNotificationHelper,
                     internal val analytics: Analytics) : Runnable {

    var invite: InviteTransactionSummary? = null

    override fun run() {
        invite?.let { invite ->
            val transactionData = fundInvite(invite)

            if (transactionData.utxos.size == 0) {
                cancelInvite(invite)
            } else {
                val transactionBroadcastResult = fulfillInvite(transactionData)
                if (transactionBroadcastResult.isSuccess) {
                    updateFulfilledInvite(invite, transactionBroadcastResult)
                }
            }
        }
    }

    private fun broadcastTXToBtcNetwork(transactionData: TransactionData): TransactionBroadcastResult {
        val result: TransactionBroadcastResult
        if (transactionData.paymentAddress.isNotEmpty()) {
            result = broadcastHelper.broadcast(transactionData)
            analytics.trackEvent(Analytics.EVENT_DROPBIT_COMPLETED)
        } else {
            result = broadcastHelper.generateFailedBroadcast(context.getString(R.string.transaction_checksum_error))
        }

        return result
    }

    private fun updateFulfilledInvite(invite: InviteTransactionSummary, transactionBroadcastResult: TransactionBroadcastResult) {
        inviteTransactionSummaryHelper.updateFulfilledInvite(invite.transactionsInvitesSummary, transactionBroadcastResult)
        saveToBroadcastBtcDatabaseMarkAsFunded(invite, transactionBroadcastResult)
        saveToExternalNotificationsDatabase(transactionBroadcastResult, invite)
        transactionNotificationManager.notifyCnOfFundedInvite(invite)
        syncWalletManager.syncNow()
    }

    private fun fulfillInvite(transactionData: TransactionData): TransactionBroadcastResult {
        val transactionBroadcastResult = broadcastTXToBtcNetwork(transactionData)
        if (!transactionBroadcastResult.isSuccess) {
            onBroadcastTxError(transactionBroadcastResult)
        }
        return transactionBroadcastResult
    }

    private fun fundInvite(invite: InviteTransactionSummary): TransactionData {
        val transactionData = transactionFundingManager
                .buildFundedTransactionDataForDropBit(invite.address, invite.valueSatoshis, invite.valueFeesSatoshis)


        transactionData.paymentAddress = invite.address
        return transactionData
    }

    private fun cancelInvite(invite: InviteTransactionSummary) {
        saveCancellationToBroadcastBtcDatabase(invite)
        saveInviteCanceledToExternalNotificationsDatabase(invite)
        cnWalletManager.updateBalances()
    }

    private fun saveCancellationToBroadcastBtcDatabase(invite: InviteTransactionSummary) {
        transactionHelper.updateInviteAsCanceled(invite.serverId)
        broadcastBtcInviteHelper.saveBroadcastInviteAsCanceled(invite)
    }

    private fun saveToExternalNotificationsDatabase(result: TransactionBroadcastResult,
                                                    invite: InviteTransactionSummary) {
        val recipient = invite.localeFriendlyDisplayIdentityForReceiver
        val btcSpent = BTCCurrency(invite.valueSatoshis)
        val messageAmount = btcSpent.toFormattedCurrency()
        val message = context.getString(R.string.invite_broadcast_real_btc_message, messageAmount, recipient)
        val txID = result.txId

        externalNotificationHelper.saveNotification(message, txID)
    }

    private fun saveInviteCanceledToExternalNotificationsDatabase(invite: InviteTransactionSummary) {
        externalNotificationHelper.saveNotification(
                context.getString(R.string.invite_broadcast_canceled_message,
                        invite.localeFriendlyDisplayIdentityForReceiver),
                invite.serverId)
    }

    private fun saveToBroadcastBtcDatabaseMarkAsFunded(invite: InviteTransactionSummary, result: TransactionBroadcastResult) {
        val inviteTransactionSummary = transactionHelper.getInviteTransactionSummary(invite.serverId)
        broadcastBtcInviteHelper.saveBroadcastBtcInvite(inviteTransactionSummary, invite.serverId, result.txId, invite.address, BTCState.FULFILLED)
    }

    private fun onBroadcastTxError(result: TransactionBroadcastResult) {
        result.message
    }

}
