package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.List;

import javax.inject.Inject;

public class SyncRunnable implements Runnable {


    private final CNWalletManager cnWalletManager;
    private final AccountManager accountManager;
    private final AddressAPIUtil addressAPIUtil;
    private final TransactionAPIUtil transactionAPIUtil;
    private final WalletHelper walletHelper;
    private final AddressHelper addressHelper;
    private final TransactionHelper transactionHelper;
    private final HDWallet hdWallet;
    private final Analytics analytics;
    private SharedMemoRetrievalRunner sharedMemoRetrievalRunner;

    @Inject
    SyncRunnable(CNWalletManager cnWalletManager, AccountManager accountManager,
                 AddressAPIUtil addressAPIUtil, TransactionAPIUtil transactionAPIUtil,
                 WalletHelper walletHelper, TransactionHelper transactionHelper,
                 AddressHelper addressHelper, HDWallet hdWallet, Analytics analytics,
                 SharedMemoRetrievalRunner sharedMemoRetrievalRunner) {
        this.cnWalletManager = cnWalletManager;
        this.accountManager = accountManager;
        this.addressAPIUtil = addressAPIUtil;
        this.transactionAPIUtil = transactionAPIUtil;
        this.walletHelper = walletHelper;
        this.addressHelper = addressHelper;
        this.transactionHelper = transactionHelper;
        this.hdWallet = hdWallet;
        this.analytics = analytics;
        this.sharedMemoRetrievalRunner = sharedMemoRetrievalRunner;
    }

    @Override
    public void run() {
        if (!cnWalletManager.hasWallet())
            return;

        setGapLimit();

        Log.d("SYNC", "fetching addresses");
        fetchAllAddresses();

        Log.d("SYNC", "fetching incomplete transactions");
        fetchIncompleteTransactions();

        Log.d("SYNC", "update outputs as spent");
        updateSpentOutPuts();

        Log.d("SYNC", "fetching fee's for transactions");
        fetchTransactionFees();

        Log.d("SYNC", "attempting to match contact phone number");
        sharedMemoRetrievalRunner.run();

        Log.d("SYNC", "fetching historic prices for transactions");
        fetchHistoricPricesForTransactionsIfNeeded();

        Log.d("SYNC", "calculating wallet balance");
        calculateBalance();

        Log.d("SYNC", "updating last sync time");
        cnWalletManager.syncCompleted();
        accountManager.cacheAddresses();
        walletHelper.linkStatsWithAddressBook();

        if (addressHelper.hasReceivedTransaction()) {
            analytics.setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, true);
        }
    }

    private void updateSpentOutPuts() {
        addressHelper.updateSpentTransactions();
    }

    private void fetchAllAddresses() {
        // Can Optimize address lookup by checking address stats
        // and only refreshing addresses that have new transactions
        // we can then start at our next index seek 5 ahead
        // and then scan AddressStats for any need to update further
        Log.d("SYNC", "fetching external addresses");
        findExternalAddresses();

        Log.d("SYNC", "fetching internal addresses");
        findInternalAddresses();
    }

    private void setGapLimit() {
        if (cnWalletManager.isFirstSync()) {
            Log.d("SYNC", "--- GAP LIMIT 20");
            addressAPIUtil.setLookAhead(AddressAPIUtil.INITIAL_GAP_LIMIT);
        } else {
            Log.d("SYNC", "--- GAP LIMIT 5");
            addressAPIUtil.setLookAhead(AddressAPIUtil.LOOK_AHEAD);
        }
    }

    private List<GsonAddress> findInternalAddresses() {
        List<GsonAddress> addresses = findAddresses(HDWallet.INTERNAL, accountManager.getLargestReportedChangeAddress() + 1);
        saveInternalIndexTo(addressAPIUtil.getLargestIndexConsumed());
        return addresses;
    }

    private List<GsonAddress> findExternalAddresses() {
        List<GsonAddress> addresses = findAddresses(HDWallet.EXTERNAL, accountManager.getLargestReportedReceiveAddress() + 1);
        saveExternalIndexTo(addressAPIUtil.getLargestIndexConsumed());
        return addresses;
    }

    private List<GsonAddress> findAddresses(int change, int currentIndex) {
        // Fetch addresses
        List<GsonAddress> addresses = addressAPIUtil.
                fetchAddresses(hdWallet, change, 0, currentIndex);

        // Save Fetched Addresses
        addressHelper.addAddresses(addresses, change);

        // save reference transactions
        transactionHelper.initTransactions(addresses);

        return addresses;
    }

    private void saveExternalIndexTo(int index) {
        accountManager.reportLargestReceiveIndexConsumed(index);
    }

    private void saveInternalIndexTo(int index) {
        accountManager.reportLargestChangeIndexConsumed(index);
    }

    private void fetchIncompleteTransactions() {
        List<TransactionSummary> incompleteTransactions = transactionHelper.getIncompleteTransactions();
        List<TransactionDetail> fetchedTransactions = transactionAPIUtil.fetchPartialTransactions(incompleteTransactions);
        transactionHelper.updateTransactions(fetchedTransactions, walletHelper.getBlockTip());
    }

    private void fetchHistoricPricesForTransactionsIfNeeded() {
        List<TransactionSummary> transactions = transactionHelper.getTransactionsWithoutHistoricPricing();
        transactionAPIUtil.updateHistoricPricingIfNecessary(transactions);
    }

    private void fetchTransactionFees() {
        List<TransactionSummary> transactionsWithoutFees = transactionHelper.getTransactionsWithoutFees();
        List<TransactionStats> transactionStats = transactionAPIUtil.fetchFeesFor(transactionsWithoutFees);
        transactionHelper.updateFeesFor(transactionStats);
    }

    private void calculateBalance() {
        walletHelper.updateBalances();
        walletHelper.updateSpendableBalances();
    }

}
