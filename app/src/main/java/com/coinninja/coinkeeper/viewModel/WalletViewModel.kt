package com.coinninja.coinkeeper.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.currency.CryptoCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import javax.inject.Inject

@Mockable
class WalletViewModel @Inject constructor(
        internal val syncManagerViewNotifier: SyncManagerViewNotifier,
        internal val walletHelper: WalletHelper,
        internal val currencyPreference: CurrencyPreference
) : ViewModel() {


    val chainHoldings: MutableLiveData<CryptoCurrency> = MutableLiveData()
    val chainHolidngsWorth: MutableLiveData<USDCurrency> = MutableLiveData()

    val syncInProgress: MutableLiveData<Boolean> = MutableLiveData()

    internal val syncChangeObserver: SyncManagerChangeObserver = object : SyncManagerChangeObserver {
        override fun onSyncStatusChanged() {
            syncInProgress.postValue(syncManagerViewNotifier.isSyncing)
        }
    }.also {
        syncManagerViewNotifier.observeSyncManagerChange(it)
    }

}