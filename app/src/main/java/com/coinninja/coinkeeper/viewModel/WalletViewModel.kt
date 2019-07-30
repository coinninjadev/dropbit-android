package com.coinninja.coinkeeper.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.FiatCurrency

@Mockable
class WalletViewModel constructor(
        internal val syncWalletManager: SyncWalletManager,
        internal val syncManagerViewNotifier: SyncManagerViewNotifier,
        internal val walletHelper: WalletHelper,
        internal val currencyPreference: CurrencyPreference
) : ViewModel() {


    val currentPrice: MutableLiveData<FiatCurrency> = MutableLiveData()
    val chainHoldings: MutableLiveData<CryptoCurrency> = MutableLiveData()
    val chainHoldingsWorth: MutableLiveData<FiatCurrency> = MutableLiveData()
    val syncInProgress: MutableLiveData<Boolean> = MutableLiveData()
    val defaultCurrencyPreference: MutableLiveData<DefaultCurrencies> = MutableLiveData()

    internal val syncChangeObserver: SyncManagerChangeObserver = object : SyncManagerChangeObserver {
        override fun onSyncStatusChanged() {
            syncInProgress.postValue(syncManagerViewNotifier.isSyncing)
            if (!syncManagerViewNotifier.isSyncing) {
                chainHoldings.value = walletHelper.balance
                chainHoldingsWorth.value = walletHelper.btcChainWorth()
            }
        }
    }.also {
        syncManagerViewNotifier.observeSyncManagerChange(it)
    }

    fun loadHoldingBalances() {
        if (walletHelper.latestPrice.toLong() == 0L) {
            syncWalletManager.syncNow()
        }

        chainHoldings.value = walletHelper.balance
        chainHoldingsWorth.value = walletHelper.btcChainWorth()
        currentPrice.value = walletHelper.latestPrice
    }

    fun loadCurrencyDefaults() {
        defaultCurrencyPreference.value = currencyPreference.currenciesPreference
    }

    fun toggleDefaultCurrencyPreference() {
        defaultCurrencyPreference.value = currencyPreference.toggleDefault()
    }

}