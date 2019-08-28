package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperClient;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.coinninja.coinkeeper.util.CNLogger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class TransactionAPIUtil {
    private static final String TAG = TransactionAPIUtil.class.getSimpleName();
    private final CNLogger logger;
    private CoinKeeperApiClient apiClient;

    @Inject
    TransactionAPIUtil(CoinKeeperApiClient apiClient, CNLogger logger) {
        this.apiClient = apiClient;
        this.logger = logger;
    }

    public List<TransactionDetail> fetchPartialTransactions(List<TransactionSummary> transactions) {
        List<TransactionDetail> results = new ArrayList<>();
        int numTransactions = transactions.size();

        if (numTransactions <= 0) {
            return results;
        }

        List<List<String>> hunks = new ArrayList<>();
        List<String> transactionIds = new ArrayList<>();
        for (int i = 0; i < numTransactions; i++) {
            if (i != 0 && (i % CoinKeeperClient.TRANSACTIONS_TO_QUERY_AT_A_TIME) == 0) {
                hunks.add(transactionIds);
                transactionIds = new ArrayList<>();
            }
            transactionIds.add(transactions.get(i).getTxid());

        }
        if (transactionIds.size() > 0) {
            hunks.add(transactionIds);
        }

        for (List<String> chunk : hunks) {
            results.addAll(getTransactionDetails(chunk.toArray(new String[0])));
        }

        return results;
    }

    void updateHistoricPricingIfNecessary(List<TransactionSummary> transactions) {
        if (transactions.isEmpty()) return;

        for (TransactionSummary transaction : transactions) {
            CNPricing pricing = getTransactionHistoricalPricing(transaction.getTxid());
            if (pricing == null || pricing.getAverage() < 100) {
                continue;
            }
            transaction.setHistoricPrice(pricing.getAverage());
            transaction.update();
        }
    }

    TransactionStats fetchFeesFor(TransactionSummary transaction) {
        Response transactionStats;

        transactionStats = apiClient.getTransactionStats(transaction.getTxid());
        if (transactionStats.isSuccessful()) {
            return (TransactionStats) transactionStats.body();
        } else {
            return null;
        }
    }

    private CNPricing getTransactionHistoricalPricing(String txid) {
        if (txid == null || "".equals(txid)) return null;

        CNPricing results = null;
        Response response = apiClient.getHistoricPrice(txid);
        if (response.isSuccessful()) {
            results = (CNPricing) response.body();
        } else {
            logger.logError(TAG, "|---- Get Historic pricing", response);
        }

        return results;
    }

    private List<TransactionDetail> getTransactionDetails(String[] txIds) {
        List<TransactionDetail> results = new ArrayList<>();

        if (txIds.length < 1) return results;

        Response response = apiClient.getTransactions(txIds);
        if (response.isSuccessful()) {
            List<TransactionDetail> details = (List<TransactionDetail>) response.body();
            if (null != details) results.addAll(details);

        } else {
            logger.logError(TAG, "|---- Get Batch Transactions", response);
        }
        return results;
    }
}
