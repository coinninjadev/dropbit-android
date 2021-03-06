package com.coinninja.coinkeeper.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.*
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeChangeObserver
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Mockable
class WalletViewModel : ViewModel() {
    internal lateinit var syncWalletManager: SyncWalletManager
    internal lateinit var syncManagerViewNotifier: SyncManagerViewNotifier
    internal lateinit var walletHelper: WalletHelper
    internal lateinit var currencyPreference: CurrencyPreference
    internal lateinit var thunderDomeRepository: ThunderDomeRepository
    internal lateinit var accountModeManager: AccountModeManager

    val isLightningLocked: MutableLiveData<Boolean> = MutableLiveData()
    val accountMode: MutableLiveData<AccountMode> = MutableLiveData()
    val currentPrice: MutableLiveData<FiatCurrency> = MutableLiveData()
    val holdings: MutableLiveData<CryptoCurrency> = MutableLiveData()
    val holdingsWorth: MutableLiveData<FiatCurrency> = MutableLiveData()
    val syncInProgress: MutableLiveData<Boolean> = MutableLiveData()
    val defaultCurrencyPreference: MutableLiveData<DefaultCurrencies> = MutableLiveData()
    val lightningHoldings: MutableLiveData<CryptoCurrency> = MutableLiveData()
    val lightningHoldingsWorth: MutableLiveData<FiatCurrency> = MutableLiveData()

    internal val syncChangeObserver: SyncManagerChangeObserver = object : SyncManagerChangeObserver {
        override fun onSyncStatusChanged() {
            syncInProgress.postValue(syncManagerViewNotifier.isSyncing)
            if (!syncManagerViewNotifier.isSyncing) {
                invalidateBalances(accountModeManager.balanceAccountMode)
                checkLightningLock()
            }
        }
    }

    fun fetchLightningBalance(): LiveData<CryptoCurrency> {
        GlobalScope.launch(Dispatchers.Main) {
            lightningHoldings.value = withContext(Dispatchers.IO) {
                thunderDomeRepository.lightningAccount?.balance ?: BTCCurrency(0)
            }
        }
        return lightningHoldings
    }

    fun setupObservers() {
        syncManagerViewNotifier.observeSyncManagerChange(syncChangeObserver)
        accountMode.value = accountModeManager.balanceAccountMode
    }

    fun setMode(accountMode: AccountMode) {
        accountModeManager.changeMode(accountMode)
        this.accountMode.value = accountMode
        invalidateBalances(accountMode)
    }

    fun loadHoldingBalances() {
        if (walletHelper.latestPrice.toLong() == 0L) {
            syncWalletManager.syncNow()
        }

        invalidateBalances(accountModeManager.balanceAccountMode)
    }

    fun loadCurrencyDefaults() {
        defaultCurrencyPreference.value = currencyPreference.currenciesPreference
    }

    fun toggleDefaultCurrencyPreference() {
        defaultCurrencyPreference.value = currencyPreference.toggleDefault()
    }

    fun fetchBtcLatestPrice(): LiveData<FiatCurrency> {
        GlobalScope.launch(Dispatchers.Main) {
            currentPrice.value = withContext(Dispatchers.IO) {
                walletHelper.latestPrice
            }
        }
        return currentPrice
    }

    fun checkLightningLock() {
        viewModelScope.launch {
            val isLocked = withContext(Dispatchers.IO) {
                thunderDomeRepository.isLocked
            }

            withContext(Dispatchers.Main) {
                isLightningLocked.value = isLocked
            }
        }
    }

    fun currentMode() {
        accountMode.value = accountModeManager.accountMode
    }

    private fun invalidateBalances(accountMode: AccountMode) {
        if (accountMode == AccountMode.BLOCKCHAIN) {
            invalidateBlockchain()
        } else {
            invalidateLightning()
        }
    }

    private fun invalidateLightning() {
        GlobalScope.launch(Dispatchers.Main) {
            val balance = BTCCurrency(withContext(Dispatchers.IO) { thunderDomeRepository.availableBalance })
            holdings.value = balance
            holdingsWorth.value = balance.toUSD(walletHelper.latestPrice) ?: USDCurrency(0.00)
            lightningHoldings.value = balance
            lightningHoldingsWorth.value = holdingsWorth.value
            currentPrice.value = walletHelper.latestPrice
        }
    }

    private fun invalidateBlockchain() {
        holdings.value = walletHelper.balance
        holdingsWorth.value = walletHelper.btcChainWorth()
        currentPrice.value = walletHelper.latestPrice
    }

}