package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.coinninja.coinkeeper.util.CNLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Response;

import static com.coinninja.coinkeeper.service.client.CoinKeeperClient.TRANSACTIONS_TO_QUERY_AT_A_TIME;

public class TransactionAPIUtil {
    private static final String TAG = TransactionAPIUtil.class.getSimpleName();

    private CoinKeeperApiClient apiClient;
    private final CNLogger logger;

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
            if (i != 0 && (i % TRANSACTIONS_TO_QUERY_AT_A_TIME) == 0) {
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

    public void updateHistoricPricingIfNecessary(List<TransactionSummary> transactions) {
        if (transactions.size() < 1) return;

        for(TransactionSummary transaction : transactions) {
            CNPricing pricing = getTransactionHistoricalPricing(transaction.getTxid());
            if (pricing == null) { continue; }
            transaction.setHistoricPrice(pricing.getAverage());
            transaction.update();
        }
    }

    private CNPricing getTransactionHistoricalPricing(String txid) {
        if ("".equals(txid)) return null;

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

    public List<TransactionStats> fetchFeesFor(List<TransactionSummary> transactions) {
        ArrayList<TransactionStats> stats = new ArrayList<>();
        Response transactionStats;

        for (TransactionSummary transaction : transactions) {
            transactionStats = apiClient.getTransactionStats(transaction.getTxid());
            if (transactionStats.isSuccessful()) {
                stats.add((TransactionStats) transactionStats.body());
            }
        }

        return stats;
    }
}
