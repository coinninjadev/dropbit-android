package com.coinninja.coinkeeper.service.runner

import android.util.Log
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.HDWalletWrapper
import com.coinninja.coinkeeper.model.helpers.AddressHelper
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import com.coinninja.coinkeeper.util.analytics.Analytics
import javax.inject.Inject

@Mockable
class SyncRunnable @Inject internal constructor(
        internal val cnWalletManager: CNWalletManager,
        internal val accountManager: AccountManager,
        internal val addressAPIUtil: AddressAPIUtil,
        internal val transactionAPIUtil: TransactionAPIUtil,
        internal val walletHelper: WalletHelper,
        internal val transactionHelper: TransactionHelper,
        internal val addressHelper: AddressHelper,
        internal val hdWallet: HDWalletWrapper,
        internal val analytics: Analytics,
        internal val sharedMemoRetrievalRunner: SharedMemoRetrievalRunner
) : Runnable {

    override fun run() {
        if (!cnWalletManager.hasWallet)
            return

        addressAPIUtil.setWallet(walletHelper.primaryWallet)
        accountManager.cacheAddresses()
        setGapLimit()

        Log.d("SYNC", "fetching addresses")
        fetchAllAddresses()

        Log.d("SYNC", "fetching incomplete transactions")
        fetchIncompleteTransactions()

        Log.d("SYNC", "update outputs as spent")
        updateSpentOutPuts()

        Log.d("SYNC", "fetching fee's for transactions")
        fetchTransactionFees()

        Log.d("SYNC", "attempting to match contact phone number")
        sharedMemoRetrievalRunner.run()

        Log.d("SYNC", "fetching historic prices for transactions")
        fetchHistoricPricesForTransactionsIfNeeded()

        Log.d("SYNC", "calculating wallet balance")
        calculateBalance()

        Log.d("SYNC", "updating last sync time")
        cnWalletManager.syncCompleted()
        accountManager.cacheAddresses()
        walletHelper.linkStatsWithAddressBook()

        if (addressHelper.hasReceivedTransaction()) {
            analytics.setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, true)
        }
    }

    private fun updateSpentOutPuts() {
        addressHelper.updateSpentTransactions()
    }

    private fun fetchAllAddresses() {
        // Can Optimize address lookup by checking address stats
        // and only refreshing addresses that have new transactions
        // we can then start at our next index seek 5 ahead
        // and then scan AddressStats for any need to update further
        Log.d("SYNC", "fetching external addresses")
        findExternalAddresses()

        Log.d("SYNC", "fetching internal addresses")
        findInternalAddresses()
    }

    private fun setGapLimit() {
        if (cnWalletManager.isFirstSync) {
            Log.d("SYNC", "--- GAP LIMIT 20")
            addressAPIUtil.setLookAhead(AddressAPIUtil.INITIAL_GAP_LIMIT)
        } else {
            Log.d("SYNC", "--- GAP LIMIT 5")
            addressAPIUtil.setLookAhead(AddressAPIUtil.LOOK_AHEAD)
        }
    }

    private fun findInternalAddresses(): List<GsonAddress> {
        val addresses = findAddresses(HDWalletWrapper.INTERNAL, accountManager.largestReportedChangeAddress + 1)
        saveInternalIndexTo(addressAPIUtil.largestIndexConsumed)
        return addresses
    }

    private fun findExternalAddresses(): List<GsonAddress> {
        val addresses = findAddresses(HDWalletWrapper.EXTERNAL, accountManager.largestReportedReceiveAddress + 1)
        saveExternalIndexTo(addressAPIUtil.largestIndexConsumed)
        return addresses
    }

    private fun findAddresses(change: Int, currentIndex: Int): List<GsonAddress> {
        // Fetch addresses
        val addresses = addressAPIUtil.fetchAddresses(hdWallet, change, 0, currentIndex)

        // Save Fetched Addresses
        addressHelper.addAddresses(addresses, change)

        // save reference transactions
        transactionHelper.initTransactions(addresses)

        return addresses
    }

    private fun saveExternalIndexTo(index: Int) {
        accountManager.reportLargestReceiveIndexConsumed(index)
    }

    private fun saveInternalIndexTo(index: Int) {
        accountManager.reportLargestChangeIndexConsumed(index)
    }

    private fun fetchIncompleteTransactions() {
        val incompleteTransactions = transactionHelper.incompleteTransactions
        val fetchedTransactions = transactionAPIUtil.fetchPartialTransactions(incompleteTransactions)
        transactionHelper.updateTransactions(fetchedTransactions, walletHelper.blockTip)
    }

    private fun fetchHistoricPricesForTransactionsIfNeeded() {
        val transactions = transactionHelper.transactionsWithoutHistoricPricing
        transactionAPIUtil.updateHistoricPricingIfNecessary(transactions)
    }

    internal fun fetchTransactionFees() {
        for (summary in transactionHelper.transactionsWithoutFees) {
            transactionAPIUtil.fetchFeesFor(summary)?.let {
                summary.fee = it.fees
                summary.update()
            }
        }
    }

    private fun calculateBalance() {
        walletHelper.updateBalances()
        walletHelper.updateSpendableBalances()
    }

}
