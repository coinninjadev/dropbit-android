package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.model.TransactionNotificationMapper;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionNotificationDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor;
import com.google.gson.Gson;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class SharedMemoRetrievalRunner implements Runnable {
    private final static String TAG = SharedMemoRetrievalRunner.class.getSimpleName();
    private final TransactionHelper transactionHelper;
    private final Analytics analytics;
    private SignedCoinKeeperApiClient signedCoinKeeperApiClient;
    private TransactionNotificationMapper transactionNotificationMapper;
    private MessageEncryptor messageEncryptor;
    private DaoSessionManager daoSessionManager;
    private WalletHelper walletHelper;

    @Inject
    public SharedMemoRetrievalRunner(Analytics analytics, TransactionHelper transactionHelper,
                                     SignedCoinKeeperApiClient signedCoinKeeperApiClient,
                                     MessageEncryptor messageEncryptor, TransactionNotificationMapper transactionNotificationMapper,
                                     DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        this.messageEncryptor = messageEncryptor;
        this.analytics = analytics;
        this.transactionHelper = transactionHelper;
        this.signedCoinKeeperApiClient = signedCoinKeeperApiClient;
        this.transactionNotificationMapper = transactionNotificationMapper;
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
    }

    @Override
    public void run() {
        Log.d(TAG, "|--------- Retrieving Shared Memos for Transactions --");
        if(!walletHelper.hasVerifiedAccount()){return;}
        TransactionNotificationDao transactionNotificationDao = daoSessionManager.getTransactionNotificationDao();
        List<TransactionSummary> transactionSummaries = transactionHelper.getRequiringNotificationCheck();
        for (TransactionSummary transaction : transactionSummaries) {
            Response response = signedCoinKeeperApiClient.getTransactionNotification(transaction.getTxid());
            if (response.isSuccessful() && ((List<CNSharedMemo>) response.body()).size() > 0) {
                handleMemoResponse(transactionNotificationDao, transaction, response);
            }

            transaction.setSoughtNotification(true);
            transaction.update();
        }
    }

    private void handleMemoResponse(TransactionNotificationDao transactionNotificationDao, TransactionSummary transaction, Response response) {
        try {
            CNSharedMemo memo = ((List<CNSharedMemo>) response.body()).get(0);
            String decrypted = messageEncryptor.decrypt(memo.getAddress(), memo.getEncrypted_payload());

            TransactionNotificationV1 v1 = new Gson().fromJson(decrypted, TransactionNotificationV1.class);

            if (v1 == null || v1.getInfo() == null || v1.getInfo().getMemo() == null || v1.getInfo().getMemo().equals("")) { return; }

            TransactionNotification transactionNotification = transactionNotificationMapper.fromV1(v1);
            long id = transactionNotificationDao.insert(transactionNotification);
            transaction.setTransactionNotificationId(id);
        } catch (Exception ex) { }
    }
}
