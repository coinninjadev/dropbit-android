package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class SaveTransactionRunner implements Runnable {
    private final TransactionHelper transactionHelper;
    private final CNWalletManager cnWalletManager;
    private final TransactionNotificationManager transactionNotificationManager;
    private final Analytics analytics;
    private final SyncWalletManager syncWalletManager;
    private CompletedBroadcastDTO completedBroadcastActivityDTO;

    @Inject
    SaveTransactionRunner(TransactionHelper transactionHelper,
                          CNWalletManager cnWalletManager,
                          TransactionNotificationManager transactionNotificationManager,
                          Analytics analytics, SyncWalletManager syncWalletManager) {
        this.transactionHelper = transactionHelper;
        this.cnWalletManager = cnWalletManager;
        this.transactionNotificationManager = transactionNotificationManager;
        this.analytics = analytics;
        this.syncWalletManager = syncWalletManager;
    }

    @Override
    public void run() {
        TransactionSummary transactionSummary = transactionHelper.createInitialTransactionForCompletedBroadcast(completedBroadcastActivityDTO);
        sendTransactionNotificationIfNecessary(transactionSummary);
        updateWalletBalance();
        syncWalletManager.syncNow();
    }

    public void setCompletedBroadcastActivityDTO(CompletedBroadcastDTO completedBroadcastActivityDTO) {
        this.completedBroadcastActivityDTO = completedBroadcastActivityDTO;
    }

    private void sendTransactionNotificationIfNecessary(TransactionSummary transactionSummary) {
        trackSharedPayloadEvent();
        createTransactionNotification(transactionSummary);
        sendTransactionNotification();
    }

    private void createTransactionNotification(TransactionSummary transactionSummary) {
        transactionNotificationManager.saveTransactionNotificationLocally(transactionSummary, completedBroadcastActivityDTO);
    }

    private void updateWalletBalance() {
        cnWalletManager.updateBalances();
    }

    private void sendTransactionNotification() {
        if (completedBroadcastActivityDTO.hasPublicKey())
            transactionNotificationManager.sendTransactionNotificationToReceiver(completedBroadcastActivityDTO);
    }

    private void trackSharedPayloadEvent() {
        String didShareMemo = "false";

        if (completedBroadcastActivityDTO.shouldShareMemo()) {
            didShareMemo = "true";
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Analytics.Companion.EVENT_MEMO_JSON_KEY_DID_SHARE, didShareMemo);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        analytics.trackEvent(Analytics.Companion.EVENT_SENT_SHARED_PAYLOAD, jsonObject);
    }
}
