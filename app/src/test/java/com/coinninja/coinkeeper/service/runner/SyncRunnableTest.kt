package com.coinninja.coinkeeper.service.runner

import app.coinninja.cn.libbitcoin.HDWallet
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.service.client.model.GsonAddress
import com.coinninja.coinkeeper.service.client.model.TransactionDetail
import com.coinninja.coinkeeper.service.client.model.TransactionStats
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.wallet.data.TestData
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.*

class SyncRunnableTest {

    private val gson: Gson get() = Gson()

    private fun createRunner(): SyncRunnable = SyncRunnable(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock()).also {
        whenever(it.addressAPIUtil.fetchAddresses(any(), any(), any(), any())).thenReturn(mutableListOf())
        whenever(it.addressHelper.addAddresses(any(), any(), any())).thenReturn(mutableListOf())
        whenever(it.cnWalletManager.hasWallet).thenReturn(true)
        whenever(it.walletHelper.primaryWallet).thenReturn(mock())

    }

    private fun mockPartialTransactions(runner: SyncRunnable): List<TransactionSummary> {
        val transactions = ArrayList<TransactionSummary>()
        whenever(runner.transactionHelper.incompleteTransactions).thenReturn(transactions)
        val t1 = TransactionSummary()
        t1.txid = "1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24"
        transactions.add(t1)

        val t2 = TransactionSummary()
        t2.txid = "9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57"
        transactions.add(t2)
        return transactions
    }

    @Test
    fun returns_when_no_there_is_no_wallet() {
        val runner = createRunner()
        whenever(runner.cnWalletManager.hasWallet).thenReturn(false)

        runner.run()

        verify(runner.cnWalletManager, times(0)).syncCompleted()
    }

    @Test
    fun updateSpentStatusForAddressesTargetStats() {
        val runner = createRunner()
        runner.run()

        verify(runner.addressHelper).updateSpentTransactions()
    }

    @Test
    fun invokes_shared_memo_retrieval_runner() {
        val runner = createRunner()
        runner.run()

        verify(runner.sharedMemoRetrievalRunner).run()
    }

    @Test
    fun sets_look_ahead_to_20_on_first_sync() {
        val runner = createRunner()
        whenever(runner.cnWalletManager.isFirstSync).thenReturn(true)

        runner.run()

        verify(runner.addressAPIUtil).setLookAhead(20)
    }

    @Test
    fun sets_look_ahead_to_5_on_subsequent_sync() {
        val runner = createRunner()
        whenever(runner.cnWalletManager.isFirstSync).thenReturn(false)

        runner.run()

        verify(runner.addressAPIUtil).setLookAhead(5)
    }

    @Test
    fun normalSyncUsesSmallerGap() {
        val runner = createRunner()
        runner.run()

        verify(runner.addressAPIUtil).setLookAhead(AddressAPIUtil.LOOK_AHEAD)
    }

    @Test
    fun updatesWalletBalance() {
        val runner = createRunner()
        runner.run()

        verify(runner.walletHelper).updateBalances(runner.walletHelper.primaryWallet)
    }

    @Test
    fun savesFeeData() {
        val runner = createRunner()
        val ts = TransactionStats()
        ts.fees = 1905L
        ts.feesRate = 5107L
        ts.isCoinBase = false

        val transactions = ArrayList<TransactionSummary>()
        val t1 = mock(TransactionSummary::class.java)
        transactions.add(t1)
        whenever(runner.transactionHelper.transactionsWithoutFees).thenReturn(transactions)
        whenever(runner.transactionAPIUtil.fetchFeesFor(t1)).thenReturn(ts)

        runner.run()

        verify(t1).fee = 1905L
        verify(t1).update()
    }

    @Test
    fun fetchesHistoricPriceForTransactions() {
        val runner = createRunner()
        val transactions = ArrayList<TransactionSummary>()

        runner.run()

        verify(runner.transactionAPIUtil).updateHistoricPricingIfNecessary(transactions)
    }

    @Test
    fun updatesPartialTransactions() {
        val runner = createRunner()
        whenever(runner.walletHelper.blockTip).thenReturn(562372)
        val t1: TransactionDetail = gson.fromJson(TestData.TRANSACTIONS_ONE, TransactionDetail::class.java)
        val t2: TransactionDetail = gson.fromJson(TestData.TRANSACTIONS_TWO, TransactionDetail::class.java)
        val fetchedTransactions = listOf(t1, t2)

        val transactions = mockPartialTransactions(runner)
        whenever(runner.transactionAPIUtil.fetchPartialTransactions(transactions)).thenReturn(fetchedTransactions)

        runner.run()

        verify(runner.transactionHelper).updateTransactions(fetchedTransactions, 562372)
    }

    @Test
    fun fetchesIncompleteTransactions() {
        val runner = createRunner()
        val transactions = mockPartialTransactions(runner)

        runner.run()

        verify(runner.transactionAPIUtil).fetchPartialTransactions(transactions)
    }

    @Test
    fun itSavesReferencesToTransactions() {
        val runner = createRunner()
        val gson = Gson()
        val responseAddresses = gson.fromJson<List<GsonAddress>>(TestData.EXTERNAL_ADDRESS_RESPONSE_BLOCK_ONE, object : TypeToken<List<GsonAddress>>() {

        }.type)
        whenever(runner.addressAPIUtil.fetchAddresses(runner.hdWallet, HDWallet.EXTERNAL, 0, 1)).thenReturn(responseAddresses)

        runner.run()

        verify(runner.transactionHelper).initTransactions(runner.walletHelper.primaryWallet, responseAddresses)
    }

    @Test
    fun savesExternalAddresses() {
        val runner = createRunner()
        val gson = Gson()
        val responseAddresses = gson.fromJson<List<GsonAddress>>(TestData.EXTERNAL_ADDRESS_RESPONSE_BLOCK_ONE, object : TypeToken<List<GsonAddress>>() {

        }.type)
        whenever(runner.addressAPIUtil.fetchAddresses(runner.hdWallet, HDWallet.EXTERNAL, 0, 1)).thenReturn(responseAddresses)

        runner.run()

        verify(runner.addressHelper).addAddresses(runner.walletHelper.primaryWallet, responseAddresses, HDWallet.EXTERNAL)
    }

    @Test
    fun savesInternalAddresses() {
        val runner = createRunner()
        val gson = Gson()
        val responseAddresses = gson.fromJson<List<GsonAddress>>(TestData.INTERNAL_ADDRESS_RESPONSE_BLOCK_ONE, object : TypeToken<List<GsonAddress>>() {

        }.type)
        whenever(runner.addressAPIUtil.fetchAddresses(runner.hdWallet, HDWallet.INTERNAL, 0, 1)).thenReturn(responseAddresses)

        runner.run()

        verify(runner.addressHelper).addAddresses(runner.walletHelper.primaryWallet, responseAddresses, HDWallet.INTERNAL)
    }

    @Test
    fun setsInternalAddressToCorrectIndex() {
        val runner = createRunner()
        val wallet = runner.walletHelper.primaryWallet
        whenever(runner.addressAPIUtil.largestIndexConsumed).thenReturn(3)

        runner.run()

        verify(runner.accountManager).reportLargestChangeIndexConsumed(wallet, 3)


        whenever(runner.addressAPIUtil.largestIndexConsumed).thenReturn(0)

        runner.run()

        verify(runner.accountManager).reportLargestChangeIndexConsumed(wallet, 0)
    }

    @Test
    fun setsExternalAddressToCorrectPositionWhenAddressesAreFetched() {
        val runner = createRunner()
        val wallet = runner.walletHelper.primaryWallet
        whenever(runner.addressAPIUtil.largestIndexConsumed).thenReturn(5)

        runner.run()

        verify(runner.accountManager).reportLargestReceiveIndexConsumed(wallet, 5)
    }

    @Test
    fun doesNotSaveALowerExternalAddressForWallet() {
        val runner = createRunner()
        whenever(runner.addressAPIUtil.largestIndexConsumed).thenReturn(1)

        runner.run()

        verify(runner.walletHelper.primaryWallet, times(0)).externalIndex = any()
    }

    @Test
    fun fetchesExternalAddresses() {
        val runner = createRunner()
        val wallet = runner.walletHelper.primaryWallet
        whenever(runner.accountManager.largestReportedReceiveAddress(wallet)).thenReturn(5)

        runner.run()

        verify(runner.addressAPIUtil).fetchAddresses(runner.hdWallet, HDWallet.EXTERNAL, 0, 6)
    }

    @Test
    fun fetchesInternalAddresses() {
        val runner = createRunner()
        val wallet = runner.walletHelper.primaryWallet
        whenever(runner.accountManager.largestReportedChangeAddress(wallet)).thenReturn(2)

        runner.run()

        verify(runner.addressAPIUtil).fetchAddresses(runner.hdWallet, HDWallet.INTERNAL, 0, 3)
    }

    @Test
    fun caches_addresses_once_sync_completed() {
        val runner = createRunner()
        val inOrder = inOrder(runner.cnWalletManager, runner.accountManager)
        runner.run()

        inOrder.verify(runner.cnWalletManager).syncCompleted()
        inOrder.verify(runner.accountManager).cacheAddresses(runner.walletHelper.primaryWallet)
    }

    @Test
    fun updatesLastSyncOnWallet() {
        val runner = createRunner()
        runner.run()

        verify(runner.cnWalletManager).syncCompleted()
    }

    @Test
    fun handles_null_fee_response() {
        val runner = createRunner()
        val t1 = mock<TransactionSummary>()
        val t2 = mock<TransactionSummary>()
        val stat = mock<TransactionStats>()
        whenever(stat.fees).thenReturn(1000)
        whenever(runner.transactionHelper.transactionsWithoutFees).thenReturn(listOf(t1, t2))
        whenever(runner.transactionAPIUtil.fetchFeesFor(t1)).thenReturn(null)
        whenever(runner.transactionAPIUtil.fetchFeesFor(t2)).thenReturn(stat)

        runner.fetchTransactionFees()

        verifyZeroInteractions(t1)
        val ordered = inOrder(t2)
        ordered.verify(t2).fee = 1000
        ordered.verify(t2).update()
    }

    @Test
    fun sets_has_received_as_user_property() {
        val runner = createRunner()
        whenever(runner.addressHelper.hasReceivedTransaction()).thenReturn(false).thenReturn(true)

        runner.run()
        runner.run()

        // only runs 1 time because the first time, no receives happened
        verify(runner.analytics).setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_ADDRESS, true)
    }

    @Test
    fun links_address_to_stats() {
        val runner = createRunner()
        runner.run()

        verify(runner.walletHelper).linkStatsWithAddressBook()
    }
}