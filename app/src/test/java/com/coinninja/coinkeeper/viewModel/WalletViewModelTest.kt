package com.coinninja.coinkeeper.viewModel

import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletViewModelTest {

    private fun createViewModel() = WalletViewModel(mock(), mock(), mock(), mock())
    private fun createScenario(): ActivityScenario<TestableActivity> = ActivityScenario.launch(TestableActivity::class.java)

    @Test
    fun invalidates_chain_holdings_when_notified_sync_is_completed() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)

        val observer = mock<Observer<in Boolean>>()
        scenario.onActivity { activity ->
            viewModel.syncInProgress.observe(activity, observer)
        }

        viewModel.syncChangeObserver.onSyncStatusChanged()
        verify(observer).onChanged(true)
        assertThat(viewModel.syncInProgress.value).isEqualTo(true)

        viewModel.syncChangeObserver.onSyncStatusChanged()
        verify(observer).onChanged(false)
        assertThat(viewModel.syncInProgress.value).isEqualTo(false)

        // registers observer
        verify(viewModel.syncManagerViewNotifier).observeSyncManagerChange(viewModel.syncChangeObserver)
    }

    @Test
    fun refreshes_wallets_balances_once_sync_reports_completion() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(100.00))
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)

        val holdingsObserver = mock<Observer<in CryptoCurrency>>()
        val holdingsWorthObserver = mock<Observer<in USDCurrency>>()
        scenario.onActivity { activity ->
            viewModel.chainHoldings.observe(activity, holdingsObserver)
            viewModel.chainHoldingsWorth.observe(activity, holdingsWorthObserver)
        }

        viewModel.syncChangeObserver.onSyncStatusChanged()
        viewModel.syncChangeObserver.onSyncStatusChanged()
        verify(holdingsObserver, atLeastOnce()).onChanged(btcBalance)
        assertThat(viewModel.chainHoldings.value?.toLong()).isEqualTo(btcBalance.toSatoshis())
        assertThat(viewModel.chainHoldingsWorth.value?.toFormattedCurrency()).isEqualTo("$100.00")
    }

    @Test
    fun provides_means_to_load_initial_holdings() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(10000000)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(USDCurrency(10000.0))
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(1000.0))

        val holdingsObserver = mock<Observer<in CryptoCurrency>>()
        val holdingsWorthObserver = mock<Observer<in USDCurrency>>()
        scenario.onActivity { activity ->
            viewModel.chainHoldings.observe(activity, holdingsObserver)
            viewModel.chainHoldingsWorth.observe(activity, holdingsWorthObserver)
        }

        viewModel.loadHoldingBalances()

        verify(viewModel.syncWalletManager, never()).syncNow()
        assertThat(viewModel.chainHoldings.value?.toLong()).isEqualTo(btcBalance.toSatoshis())
        assertThat(viewModel.chainHoldingsWorth.value?.toFormattedCurrency()).isEqualTo("$1,000.00")
    }

    @Test
    fun triggers_sync_when_value_of_latest_price_is_zero_dollars() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(USDCurrency(0.0))
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(0.0))

        val holdingsObserver = mock<Observer<in CryptoCurrency>>()
        val holdingsWorthObserver = mock<Observer<in USDCurrency>>()
        scenario.onActivity { activity ->
            viewModel.chainHoldings.observe(activity, holdingsObserver)
            viewModel.chainHoldingsWorth.observe(activity, holdingsWorthObserver)
        }

        viewModel.loadHoldingBalances()

        verify(viewModel.syncWalletManager).syncNow()
        assertThat(viewModel.chainHoldings.value?.toLong()).isEqualTo(btcBalance.toSatoshis())
        assertThat(viewModel.chainHoldingsWorth.value?.toFormattedCurrency()).isEqualTo("$0.00")
    }
}