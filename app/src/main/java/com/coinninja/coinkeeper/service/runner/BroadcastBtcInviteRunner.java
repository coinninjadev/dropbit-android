package com.coinninja.coinkeeper.service.runner;

import android.content.Context;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import javax.inject.Inject;


public class BroadcastBtcInviteRunner implements Runnable {
    private final BroadcastTransactionHelper broadcastHelper;
    private final TransactionFundingManager transactionFundingManager;
    private final TransactionNotificationManager transactionNotificationManager;
    private final InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    private final BroadcastBtcInviteHelper broadcastBtcInviteHelper;
    private final SyncWalletManager syncWalletManager;
    private final ExternalNotificationHelper externalNotificationHelper;
    private final Analytics analytics;
    private final WalletHelper walletHelper;
    private final Context context;

    private TransactionHelper transactionHelper;
    private InviteTransactionSummary invite;

    @Inject
    BroadcastBtcInviteRunner(@ApplicationContext Context context, WalletHelper walletHelper,
                             TransactionFundingManager transactionFundingManager,
                             TransactionNotificationManager transactionNotificationManager,
                             InviteTransactionSummaryHelper inviteTransactionSummaryHelper,
                             TransactionHelper transactionHelper, BroadcastBtcInviteHelper broadcastBtcInviteHelper,
                             BroadcastTransactionHelper broadcastHelper,
                             SyncWalletManager syncWalletManager,
                             ExternalNotificationHelper externalNotificationHelper,
                             Analytics analytics) {
        this.context = context;
        this.walletHelper = walletHelper;
        this.transactionFundingManager = transactionFundingManager;
        this.transactionNotificationManager = transactionNotificationManager;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
        this.broadcastBtcInviteHelper = broadcastBtcInviteHelper;
        this.transactionHelper = transactionHelper;
        this.broadcastHelper = broadcastHelper;
        this.syncWalletManager = syncWalletManager;
        this.externalNotificationHelper = externalNotificationHelper;
        this.analytics = analytics;
    }

    @Override
    public void run() {
        TransactionData transactionData = fundInvite();

        TransactionBroadcastResult transactionBroadcastResult = fulfillInvite(transactionData);
        if (transactionBroadcastResult.isSuccess()) {
            updateFulfilledInvite(transactionBroadcastResult);
        }
    }

    public void setInvite(InviteTransactionSummary invite) {
        this.invite = invite;
    }

    private TransactionBroadcastResult broadcastTXToBtcNetwork(TransactionData transactionData) {
        TransactionBroadcastResult result;
        if (checksumPass(transactionData)) {
            result = broadcastHelper.broadcast(transactionData);
            analytics.trackEvent(Analytics.EVENT_DROPBIT_COMPLETED);
        } else {
            result = broadcastHelper.generateFailedBroadcast(context.getString(R.string.transaction_checksum_error));
        }

        return result;
    }

    private void updateFulfilledInvite(TransactionBroadcastResult transactionBroadcastResult) {
        inviteTransactionSummaryHelper.updateFulfilledInvite(invite.getTransactionsInvitesSummary(), transactionBroadcastResult);
        saveToBroadcastBtcDatabaseMarkAsFunded(transactionBroadcastResult);
        saveToExternalNotificationsDatabase(transactionBroadcastResult, invite);
        transactionNotificationManager.notifyCnOfFundedInvite(invite);
        syncWalletManager.syncNow();
    }

    private TransactionBroadcastResult fulfillInvite(TransactionData transactionData) {
        TransactionBroadcastResult transactionBroadcastResult = broadcastTXToBtcNetwork(transactionData);
        if (!transactionBroadcastResult.isSuccess()) {
            onBroadcastTxError(transactionBroadcastResult);
        }
        return transactionBroadcastResult;
    }

    private TransactionData fundInvite() {
        TransactionData transactionData = transactionFundingManager.
                buildFundedTransactionDataForDropBit(invite.getValueSatoshis(), invite.getValueFeesSatoshis());

        if (transactionData.getUtxos().length == 0) {
            cancelInvite();
        }

        transactionData.setPaymentAddress(invite.getAddress());
        return transactionData;
    }

    private void cancelInvite() {
        saveCancellationToBroadcastBtcDatabase(invite);
        saveInviteCanceledToExternalNotificationsDatabase(invite);
        updateWalletBalance();
    }

    private void saveCancellationToBroadcastBtcDatabase(InviteTransactionSummary invite) {
        transactionHelper.updateInviteAsCanceled(invite.getServerId());
        broadcastBtcInviteHelper.saveBroadcastInviteAsCanceled(invite);
    }

    private void saveToExternalNotificationsDatabase(TransactionBroadcastResult result,
                                                     InviteTransactionSummary invite) {
        PhoneNumber phoneNumber = invite.getReceiverPhoneNumber();

        BTCCurrency btcSpent = new BTCCurrency(invite.getValueSatoshis());
        String messageAmount = btcSpent.toFormattedCurrency();
        String messageReceiver = phoneNumber.displayTextForLocale();
        String message = context.getString(R.string.invite_broadcast_real_btc_message, messageAmount, messageReceiver);
        String txID = result.getTxId();

        externalNotificationHelper.saveNotification(message, txID);
    }

    private void saveInviteCanceledToExternalNotificationsDatabase(InviteTransactionSummary invite) {

        String messageReceiver = invite.getReceiverPhoneNumber().displayTextForLocale();
        String message = context.getString(R.string.invite_broadcast_canceled_message, messageReceiver);
        String inviteID = invite.getServerId();

        externalNotificationHelper.saveNotification(message, inviteID);
    }

    private void updateWalletBalance() {
        walletHelper.updateBalances();
        walletHelper.updateSpendableBalances();
    }

    private void saveToBroadcastBtcDatabaseMarkAsFunded(TransactionBroadcastResult result) {
        InviteTransactionSummary inviteTransactionSummary = transactionHelper.getInviteTransactionSummary(invite.getServerId());
        broadcastBtcInviteHelper.saveBroadcastBtcInvite(inviteTransactionSummary, invite.getServerId(), result.getTxId(), invite.getAddress(), BTCState.FULFILLED);
    }

    private void onBroadcastTxError(TransactionBroadcastResult result) {
        result.getMessage();
    }

    private boolean checksumPass(TransactionData transactionData) {
        return transactionData != null &&
                transactionData.getPaymentAddress() != null &&
                !transactionData.getPaymentAddress().isEmpty();
    }

}
