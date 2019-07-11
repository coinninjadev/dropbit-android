package com.coinninja.coinkeeper.viewModel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.dropbit.annotations.Mockable
import app.dropbit.commons.util.CoroutineContextProvider
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.HistoricalPriceRecord
import com.coinninja.coinkeeper.ui.market.Granularity
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.currency.USDCurrency
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

@Mockable
class MarketDataViewModel(internal val contextProvider: CoroutineContextProvider,
                          internal val walletHelper: WalletHelper,
                          internal val apiClient: SignedCoinKeeperApiClient,
                          internal val localBroadCastUtil: LocalBroadCastUtil) : ViewModel() {

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updateLatestBtcPrice(intent)
            }
        }
    }
    val intentFilter = IntentFilter(DropbitIntents.ACTION_BTC_PRICE_UPDATE)
    val currentGranularity: MutableLiveData<Granularity> = MutableLiveData()
    val periodData: MutableLiveData<List<HistoricalPriceRecord>> = MutableLiveData()
    val currentBtcPrice: MutableLiveData<USDCurrency> = MutableLiveData<USDCurrency>().also {
        localBroadCastUtil.registerReceiver(receiver, intentFilter)
    }


    @Suppress("UNCHECKED_CAST")
    fun loadGranularity(granularity: Granularity) {
        currentGranularity.value = granularity
        GlobalScope.launch(contextProvider.Main) {
            val response: Response<List<HistoricalPriceRecord>> = withContext(contextProvider.IO) {
                apiClient.loadHistoricPricing(granularity) as Response<List<HistoricalPriceRecord>>
            }

            if (response.isSuccessful) {
                periodData.postValue(response.body())
            }

        }
    }

    fun loadCurrentPrice() {
        currentBtcPrice.value = walletHelper.latestPrice
    }

    public override fun onCleared() {
        localBroadCastUtil.unregisterReceiver(receiver)
        super.onCleared()
    }

    private fun updateLatestBtcPrice(intent: Intent) {
        if (intent.action == DropbitIntents.ACTION_BTC_PRICE_UPDATE) {
            val price =
                    if (intent.hasExtra(DropbitIntents.EXTRA_BITCOIN_PRICE)) {
                        USDCurrency(intent.getLongExtra(DropbitIntents.EXTRA_BITCOIN_PRICE, 0L))
                    } else {
                        walletHelper.latestPrice
                    }
            currentBtcPrice.postValue(price)
        }
    }
}
