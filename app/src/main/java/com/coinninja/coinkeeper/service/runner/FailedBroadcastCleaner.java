package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.BlockchainTX;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class FailedBroadcastCleaner implements Runnable {

    private final CoinKeeperApplication application;
    private final TransactionHelper transactionHelper;
    private final ExternalNotificationHelper externalNotificationHelper;
    private final CoinKeeperApiClient coinKeeperApiClient;
    private final BlockchainClient blockChainInfoClient;
    private Analytics analytics;

    @Inject
    public FailedBroadcastCleaner(CoinKeeperApplication application, ExternalNotificationHelper externalNotificationHelper,
                                  TransactionHelper transactionHelper, CoinKeeperApiClient coinKeeperApiClient,
                                  Analytics analytics, BlockchainClient blockchainClient) {
        this.application = application;
        this.externalNotificationHelper = externalNotificationHelper;
        this.transactionHelper = transactionHelper;
        blockChainInfoClient = blockchainClient;
        this.analytics = analytics;
        this.coinKeeperApiClient = coinKeeperApiClient;
    }

    @Override
    public void run() {
        List<TransactionSummary> oldPendingTransactions = transactionHelper.getPendingTransactionsOlderThan(DropbitIntents.PENDING_TRANSITION_LIFE_LIMIT_SECONDS);
        if (isListEmpty(oldPendingTransactions))
            return;


        List<TransactionSummary> unAcknowledgedByCoinNinja = checkCoinNinjaMarkAsAcknowledged(oldPendingTransactions);
        if (isListEmpty(unAcknowledgedByCoinNinja))
            return;


        List<TransactionSummary> unAcknowledgedByBlockchainInfo = checkBlockchainInfoMarkAsAcknowledged(unAcknowledgedByCoinNinja);
        if (isListEmpty(unAcknowledgedByBlockchainInfo))
            return;

        String[] newlyFailedTXIDs = markAsFailedToBroadcast(unAcknowledgedByBlockchainInfo);

        notifyUserOfBroadcastFail(newlyFailedTXIDs);
    }

    List<TransactionSummary> checkCoinNinjaMarkAsAcknowledged(List<TransactionSummary> oldPendingTransactions) {

        HashMap<String, TransactionSummary> transactionsMap = transListToTxIDMap(oldPendingTransactions);
        Response response = coinKeeperApiClient.getTransactions(transactionsMap.keySet().toArray(new String[transactionsMap.size()]));

        if (!response.isSuccessful()) {//stop everything, we might not have a internet connection !
            return null;
        }

        if (response.body() != null) {//if coinninja gives us null, that means NONE of the txIds were found
            List<TransactionDetail> details = (List<TransactionDetail>) response.body();
            for (TransactionDetail detail : details) {
                String foundTxID = detail.getTransactionId();
                transactionsMap.remove(foundTxID);
                markAsAcknowledged(foundTxID);
            }
        }


        return flatMapOfValuesInHashMap(transactionsMap);
    }

    List<TransactionSummary> checkBlockchainInfoMarkAsAcknowledged(List<TransactionSummary> oldPendingTransactions) {
        HashMap<String, TransactionSummary> transactionsMap = transListToTxIDMap(oldPendingTransactions);

        List<TransactionDetail> details = blockChainInfoLoop(transactionsMap.keySet().toArray(new String[transactionsMap.size()]));
        if (details == null) {//stop everything, we might not have a internet connection !
            return null;
        }

        for (TransactionDetail detail : details) {
            String foundTxID = detail.getTransactionId();
            transactionsMap.remove(foundTxID);
            markAsAcknowledged(foundTxID);
        }

        return flatMapOfValuesInHashMap(transactionsMap);
    }

    String[] markAsFailedToBroadcast(List<TransactionSummary> unAcknowledged) {
        ArrayList<String> newTxIds = new ArrayList<>();
        for (TransactionSummary transactionSummary : unAcknowledged) {
            String newTXID = transactionHelper.markTransactionSummaryAsFailedToBroadcast(transactionSummary.getTxid());
            reportFailure(transactionSummary);
            if (newTXID == null) continue;

            newTxIds.add(newTXID);
        }

        return newTxIds.toArray(new String[newTxIds.size()]);
    }

    void notifyUserOfBroadcastFail(String[] newlyFailedTXIDs) {
        for (String newlyFailedTXID : newlyFailedTXIDs) {
            String message = application.getString(R.string.notification_transaction_failed_to_broadcast, newlyFailedTXID);

            externalNotificationHelper.saveNotification(message, newlyFailedTXID);
        }
    }

    private List<TransactionDetail> blockChainInfoLoop(String[] txids) {
        List<TransactionDetail> transactionDetails = new ArrayList<>();
        for (String txId : txids) {
            Response response = blockChainInfoClient.getTransactionFor(txId);

            if (response == null) {
                continue;
            }

            if (!response.isSuccessful()) {
                continue;
            }

            BlockchainTX blockchainTX = (BlockchainTX) response.body();
            TransactionDetail transactionDetail = new TransactionDetail();
            transactionDetail.setTransactionId(blockchainTX.getHash());
            transactionDetails.add(transactionDetail);
        }

        return transactionDetails;
    }

    private void reportFailure(TransactionSummary transactionSummary) {
        if (transactionSummary.getTransactionsInvitesSummary().getInviteTransactionSummary() == null) {
            analytics.trackEvent(Analytics.EVENT_PENDING_TRANSACTION_FAILED);
        } else {
            analytics.trackEvent(Analytics.EVENT_PENDING_DROPBIT_SEND_FAILED);
        }
    }

    private void markAsAcknowledged(String acknowledgedTxID) {
        transactionHelper.markTransactionSummaryAsAcknowledged(acknowledgedTxID);
    }

    private HashMap<String, TransactionSummary> transListToTxIDMap(List<TransactionSummary> list) {
        HashMap<String, TransactionSummary> map = new HashMap<>();

        for (TransactionSummary transactionSummary : list) {
            String txID = transactionSummary.getTxid();
            map.put(txID, transactionSummary);
        }

        return map;
    }

    private List<TransactionSummary> flatMapOfValuesInHashMap(HashMap<String, TransactionSummary> transactionsHasMap) {
        return new ArrayList<>(transactionsHasMap.values());
    }

    private boolean isListEmpty(List list) {
        return list == null || list.size() == 0;
    }
}
