package com.coinninja.coinkeeper.cn.dropbit;

import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class DropBitAddMemoManager {
    private TransactionNotificationManager transactionNotificationManager;
    private WalletHelper walletHelper;

    @Inject
    DropBitAddMemoManager(TransactionNotificationManager transactionNotificationManager, WalletHelper walletHelper) {
        this.transactionNotificationManager = transactionNotificationManager;
        this.walletHelper = walletHelper;
    }

    public void createMemo(String txid, String memo) {
        TransactionNotification notification = transactionNotificationManager.createTransactionNotification(memo, false);
        TransactionSummary transaction = walletHelper.getTransactionByTxid(txid);

        if (transaction != null) {
            transaction.setSoughtNotification(true);
            transaction.setTransactionNotification(notification);
        }

        transaction.update();
    }
}
