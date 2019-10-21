package com.coinninja.coinkeeper.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.persistance.model.LightningAccount
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.CryptoCurrency
import app.dropbit.commons.currency.FiatCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun createViewModel(): WalletViewModel {
        val viewModel = WalletViewModel()
        viewModel.syncManagerViewNotifier = mock()
        viewModel.syncWalletManager = mock()
        viewModel.walletHelper = mock()
        viewModel.currencyPreference = mock()
        viewModel.thunderDomeRepository = mock()
        viewModel.accountModeManager = mock()
        viewModel.setupObservers()
        return viewModel
    }

    private fun createScenario(): ActivityScenario<TestableActivity> = ActivityScenario.launch(TestableActivity::class.java)

    @Test
    fun invalidates_chain_holdings_when_notified_sync_is_completed__mode_BLOCKCHAIN() {
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
    fun refreshes_wallets_balances_once_sync_reports_completion__mode_is_BLOCKCHAIN() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        val chainWorth = USDCurrency(100.00)
        val currentPrice = USDCurrency(10000.00)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(chainWorth)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(currentPrice)
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.BLOCKCHAIN)

        val holdingsObserver = mock<Observer<in CryptoCurrency>>()
        val holdingsWorthObserver = mock<Observer<in FiatCurrency>>()
        val currentPriceObserver = mock<Observer<in FiatCurrency>>()
        scenario.onActivity { activity ->
            viewModel.holdings.observe(activity, holdingsObserver)
            viewModel.holdingsWorth.observe(activity, holdingsWorthObserver)
            viewModel.currentPrice.observe(activity, currentPriceObserver)
        }

        viewModel.syncChangeObserver.onSyncStatusChanged()
        viewModel.syncChangeObserver.onSyncStatusChanged()
        verify(holdingsObserver, atLeastOnce()).onChanged(btcBalance)
        verify(holdingsWorthObserver, atLeastOnce()).onChanged(chainWorth)
        verify(currentPriceObserver, atLeastOnce()).onChanged(currentPrice)
        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(btcBalance.toLong())
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$100.00")
        assertThat(viewModel.currentPrice.value?.toFormattedCurrency()).isEqualTo("$10,000.00")
    }

    @Test
    fun provides_means_to_load_initial_holdings__mode_is_BLOCKCHAIN() {
        createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(10000000)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(USDCurrency(10000.0))
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(1000.0))
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.BLOCKCHAIN)

        viewModel.loadHoldingBalances()

        verify(viewModel.syncWalletManager, never()).syncNow()
        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(btcBalance.toLong())
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$1,000.00")
        assertThat(viewModel.currentPrice.value?.toFormattedCurrency()).isEqualTo("$10,000.00")
    }

    @Test
    fun triggers_sync_when_value_of_latest_price_is_zero_dollars__mode_BLOCKCHAIN() {
        createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(USDCurrency(0.0))
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(0.0))
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.BLOCKCHAIN)


        viewModel.loadHoldingBalances()

        verify(viewModel.syncWalletManager).syncNow()
        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(btcBalance.toLong())
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$0.00")
    }

    @Test
    fun notifies_of_default_currency_preference_changes() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        val defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        whenever(viewModel.currencyPreference.currenciesPreference).thenReturn(defaultCurrencies)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(0.0))
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.BLOCKCHAIN)

        val defaultCurrencyChangeObserver = mock<Observer<in DefaultCurrencies>>()
        scenario.onActivity { activity ->
            viewModel.defaultCurrencyPreference.observe(activity, defaultCurrencyChangeObserver)
        }

        viewModel.loadCurrencyDefaults()

        verify(defaultCurrencyChangeObserver).onChanged(defaultCurrencies)
        assertThat(viewModel.defaultCurrencyPreference.value).isEqualTo(defaultCurrencies)
    }

    @Test
    fun toggles_currency_preference__mode_BLOCKCHAIN() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        val defaultCurrenciesInitial = DefaultCurrencies(USDCurrency(), BTCCurrency())
        val defaultCurrenciesToggled = DefaultCurrencies(BTCCurrency(), USDCurrency())
        whenever(viewModel.currencyPreference.currenciesPreference).thenReturn(defaultCurrenciesInitial)
        whenever(viewModel.currencyPreference.toggleDefault()).thenReturn(defaultCurrenciesToggled)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(USDCurrency(0.0))
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.BLOCKCHAIN)

        val defaultCurrencyChangeObserver = mock<Observer<in DefaultCurrencies>>()
        scenario.onActivity { activity ->
            viewModel.defaultCurrencyPreference.observe(activity, defaultCurrencyChangeObserver)
        }

        viewModel.loadCurrencyDefaults()
        viewModel.toggleDefaultCurrencyPreference()

        verify(defaultCurrencyChangeObserver, atLeastOnce()).onChanged(any())
        assertThat(viewModel.defaultCurrencyPreference.value!!.primaryCurrency).isInstanceOf(BTCCurrency::class.java)
    }

    @Test
    fun observers_are_notified_of_change() {
        createScenario().onActivity { activity: TestableActivity ->
            val viewModel = createViewModel()

            val observer = mock<Observer<in AccountMode>>()
            viewModel.accountMode.observe(activity, observer)

            viewModel.setMode(AccountMode.LIGHTNING)

            verify(observer, atLeastOnce()).onChanged(AccountMode.LIGHTNING)
        }
    }

    @Ignore
    @Test
    fun setting_mode_to_LIGHTNING_pushes_balances_for_lightning() {
        val viewModel = createViewModel()

        val btcBalance = BTCCurrency(1000000)
        val chainWorth = USDCurrency(100.00)
        val currentPrice = USDCurrency(10000.00)
        val lightningAccount = LightningAccount(balance = BTCCurrency(2000000))

        whenever(viewModel.thunderDomeRepository.lightningAccount).thenReturn(lightningAccount)
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(chainWorth)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(currentPrice)
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)

        runBlocking {
            viewModel.setMode(AccountMode.LIGHTNING)
        }

        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(2000000)
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$200.00")
        assertThat(viewModel.currentPrice.value?.toFormattedCurrency()).isEqualTo("$10,000.00")

    }

    @Test
    fun setting_mode_to_BLOCKCHAIN_pushes_balances_for_BLOCKCHAIN() {
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        val chainWorth = USDCurrency(100.00)
        val currentPrice = USDCurrency(10000.00)
        val lightningAccount = LightningAccount(balance = BTCCurrency(2000000))
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(chainWorth)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(currentPrice)
        whenever(viewModel.thunderDomeRepository.lightningAccount).thenReturn(lightningAccount)
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)

        runBlocking {
            viewModel.setMode(AccountMode.BLOCKCHAIN)
        }

        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(1000000)
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$100.00")
        assertThat(viewModel.currentPrice.value?.toFormattedCurrency()).isEqualTo("$10,000.00")
    }

    @Ignore
    @Test
    fun pushes_updates_for_lightning_balances_when_mode_is_LIGHTNING() {
        createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        val chainWorth = USDCurrency(100.00)
        val currentPrice = USDCurrency(10000.00)
        val lightningAccount = LightningAccount(balance = BTCCurrency(2000000))
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(chainWorth)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(currentPrice)
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.LIGHTNING)
        whenever(viewModel.thunderDomeRepository.lightningAccount).thenReturn(lightningAccount)

        runBlocking {
            viewModel.setMode(AccountMode.LIGHTNING)
        }

        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(2000000)
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$200.00")
        assertThat(viewModel.currentPrice.value?.toFormattedCurrency()).isEqualTo("$10,000.00")
    }

    @Ignore
    @Test
    fun pushes_updates_for_no_balance_lightning_balances_when_mode_is_LIGHTNING() {
        createScenario()
        val viewModel = createViewModel()
        val btcBalance = BTCCurrency(1000000)
        val chainWorth = USDCurrency(100.00)
        val currentPrice = USDCurrency(10000.00)
        val lightningAccount = LightningAccount(balance = BTCCurrency(0))
        whenever(viewModel.walletHelper.balance).thenReturn(btcBalance)
        whenever(viewModel.walletHelper.btcChainWorth()).thenReturn(chainWorth)
        whenever(viewModel.walletHelper.latestPrice).thenReturn(currentPrice)
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)
        whenever(viewModel.accountModeManager.balanceAccountMode).thenReturn(AccountMode.LIGHTNING)
        whenever(viewModel.thunderDomeRepository.lightningAccount).thenReturn(lightningAccount)

        runBlocking {
            viewModel.loadHoldingBalances()
        }

        assertThat(viewModel.holdings.value?.toLong()).isEqualTo(0)
        assertThat(viewModel.holdingsWorth.value?.toFormattedCurrency()).isEqualTo("$0.00")
        assertThat(viewModel.currentPrice.value?.toFormattedCurrency()).isEqualTo("$10,000.00")
    }

    @Ignore
    @Test
    fun fetches_latest_price_of_btc() {
        createScenario()
        val viewModel = createViewModel()
        whenever(viewModel.walletHelper.latestPrice).thenReturn(USDCurrency(10_500_00))

        val currentPrice = runBlocking {
            viewModel.fetchBtcLatestPrice()
        }

        assertThat(currentPrice.value!!.toLong()).isEqualTo(10_000_00)
    }
}