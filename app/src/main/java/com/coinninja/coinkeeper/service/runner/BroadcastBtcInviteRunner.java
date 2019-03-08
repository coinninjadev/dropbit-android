package com.coinninja.coinkeeper.service.runner;

import android.content.Context;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.google.i18n.phonenumbers.Phonenumber;

import javax.inject.Inject;


public class BroadcastBtcInviteRunner implements Runnable {
    private final BroadcastTransactionHelper broadcastHelper;
    private final TransactionNotificationManager transactionNotificationManager;
    private final InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    private final BroadcastBtcInviteHelper broadcastBtcInviteHelper;
    private final HDWallet hdWallet;
    private final SyncWalletManager syncWalletManager;
    private final ExternalNotificationHelper externalNotificationHelper;
    private final AccountManager accountManager;
    private final Analytics analytics;
    private final WalletHelper walletHelper;
    private final Context context;
    private final DaoSessionManager daoSessionManager;
    private FundingRunnable fundingRunnable;

    private TransactionHelper transactionHelper;
    private InviteTransactionSummary invite;
    private PhoneNumberUtil phoneNumberUtil;

    @Inject
    BroadcastBtcInviteRunner(@ApplicationContext Context context, WalletHelper walletHelper,
                             HDWallet hdWallet, DaoSessionManager daoSessionManager,
                             TransactionHelper transactionHelper, FundingRunnable fundingRunnable,
                             TransactionNotificationManager transactionNotificationManager,
                             InviteTransactionSummaryHelper inviteTransactionSummaryHelper,
                             BroadcastBtcInviteHelper broadcastBtcInviteHelper,
                             BroadcastTransactionHelper broadcastHelper,
                             SyncWalletManager syncWalletManager,
                             ExternalNotificationHelper externalNotificationHelper,
                             AccountManager accountManager, Analytics analytics,
                             PhoneNumberUtil phoneNumberUtil) {
        this.context = context;
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
        this.hdWallet = hdWallet;
        this.transactionNotificationManager = transactionNotificationManager;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
        this.broadcastBtcInviteHelper = broadcastBtcInviteHelper;
        this.transactionHelper = transactionHelper;
        this.broadcastHelper = broadcastHelper;
        this.syncWalletManager = syncWalletManager;
        this.externalNotificationHelper = externalNotificationHelper;
        this.accountManager = accountManager;
        this.analytics = analytics;
        this.fundingRunnable = fundingRunnable;
        this.phoneNumberUtil = phoneNumberUtil;
    }

    @Override
    public void run() {
        //Step 1. fund the Invite
        FundingRunnable.FundedHolder fundedHolder = fundInvite();
        if (fundedHolder == null || fundedHolder.getUnspentTransactionHolder() == null) {
            saveCancellationToBroadcastBtcDatabase(invite);
            saveInviteCanceledToExternalNotificationsDatabase(invite);
            updateWalletBalance();
            return;
        }

        //Step 2. broadcast the funded invite to libbitcoin network
        TransactionBroadcastResult transactionBroadcastResult = broadcastTXToBtcNetwork(fundedHolder.getUnspentTransactionHolder());
        if (!transactionBroadcastResult.isSuccess()) {
            onBroadcastTxError(transactionBroadcastResult);
            return;
        }

        //Step 3. save tx as transactionSummary
        inviteTransactionSummaryHelper.updateFulfilledInvite(invite.getTransactionsInvitesSummary(), transactionBroadcastResult);

        //Step 4. save tx to database so later we can update the invite server
        saveToBroadcastBtcDatabaseMarkAsFunded(transactionBroadcastResult);

        //Step 5. save tx to database so later we can show a notifications in on the users phone
        saveToExternalNotificationsDatabase(transactionBroadcastResult, invite, fundedHolder.getUnspentTransactionHolder());

        transactionNotificationManager.notifyCnOfFundedInvite(invite);

        syncWalletManager.syncNow();
    }

    private void saveCancellationToBroadcastBtcDatabase(InviteTransactionSummary invite) {
        transactionHelper.updateInviteAsCanceled(invite.getServerId());
        broadcastBtcInviteHelper.saveBroadcastInviteAsCanceled(invite);
    }

    private FundingRunnable.FundedHolder fundInvite() {
        fundingRunnable.setCurrentChangeAddressIndex(accountManager.getNextChangeIndex());
        fundingRunnable.setPaymentAddress(invite.getAddress());
        FundingUTXOs fundingUTXOs = fundingRunnable.fundRun(invite.getValueSatoshis(), invite.getValueFeesSatoshis(), null);
        FundingRunnable.FundedHolder fundedHolder = fundingRunnable.evaluateFundingUTXOs(fundingUTXOs);
        return fundedHolder;
    }

    public TransactionBroadcastResult broadcastTXToBtcNetwork(UnspentTransactionHolder unspentTransactionHolder) {
        TransactionBroadcastResult result;

        TransactionData transactionData = unspentTransactionHolder.toTransactionData();

        if (checksumPass(transactionData)) {
            result = broadcastHelper.broadcast(transactionData);
            analytics.trackEvent(Analytics.EVENT_DROPBIT_COMPLETED);
        } else {
            result = broadcastHelper.generateFailedBroadcast(context.getString(R.string.transaction_checksum_error));
        }

        return result;
    }

    private void saveToExternalNotificationsDatabase(TransactionBroadcastResult result,
                                                     InviteTransactionSummary invite,
                                                     UnspentTransactionHolder unspentTransactionHolder) {
        PhoneNumber phoneNumber = invite.getReceiverPhoneNumber();
        long spent = unspentTransactionHolder.satoshisRequestingToSpend;

        BTCCurrency btcSpent = new BTCCurrency(spent);

        String messageAmount = btcSpent.toFormattedCurrency();
        String messageReceiver = phoneNumber.toNationalDisplayText();
        String message = context.getString(R.string.invite_broadcast_real_btc_message, messageAmount, messageReceiver);
        String txID = result.getTxId();

        externalNotificationHelper.saveNotification(message, txID);
    }

    private void saveInviteCanceledToExternalNotificationsDatabase(InviteTransactionSummary invite) {

        String messageReceiver = invite.getReceiverPhoneNumber().toNationalDisplayText();
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

    public void setInvite(InviteTransactionSummary invite) {
        this.invite = invite;
    }

}
