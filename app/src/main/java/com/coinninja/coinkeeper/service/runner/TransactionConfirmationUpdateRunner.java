package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.List;

import javax.inject.Inject;

public class TransactionConfirmationUpdateRunner implements Runnable {
    private final static String TAG = TransactionConfirmationUpdateRunner.class.getSimpleName();
    private final TransactionHelper transactionHelper;
    private final WalletHelper walletHelper;
    private final Analytics analytics;

    @Inject
    public TransactionConfirmationUpdateRunner(Analytics analytics, TransactionHelper transactionHelper,
                                               WalletHelper walletHelper) {
        this.analytics = analytics;
        this.transactionHelper = transactionHelper;
        this.walletHelper = walletHelper;
    }

    @Override
    public void run() {
        Log.d(TAG, "|--------- Calculating Confirmations For Unconfirmed Transactions --");

        List<TransactionSummary> transactions = transactionHelper.getPendingMindedTransactions();
        int currentBlockHeight = walletHelper.getBlockTip();

        for (TransactionSummary transaction : transactions) {
            String blockHash = transaction.getBlockhash();
            if (blockHash != null && !blockHash.isEmpty()) {
                transaction.setNumConfirmations(
                        CNWalletManager.Companion.calcConfirmations(currentBlockHeight, transaction.getBlockheight()));
                transaction.update();
            }
        }

    }
}
