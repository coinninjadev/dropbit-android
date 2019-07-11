package com.coinninja.coinkeeper.viewModel

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.matchers.IntentFilterSubject.Companion.assertThatIntentFilter
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketDataViewModelTest {

    @Test
    fun `updates btc price value when broadcast received`() {
        val marketDataViewModel = MarketDataViewModel(mock(), mock(), mock(), mock())
        val intent = Intent(DropbitIntents.ACTION_BTC_PRICE_UPDATE).also {
            it.putExtra(DropbitIntents.EXTRA_BITCOIN_PRICE, 1245000L)
        }

        marketDataViewModel.receiver.onReceive(ApplicationProvider.getApplicationContext(), intent)

        assertThat(marketDataViewModel.currentBtcPrice.value!!.toFormattedCurrency()).isEqualTo("$12,450.00")
    }

    @Test
    fun `sets up broadcast receiver to observe price updates`() {
        val marketDataViewModel = MarketDataViewModel(mock(), mock(), mock(), mock())

        verify(marketDataViewModel.localBroadCastUtil).registerReceiver(marketDataViewModel.receiver, marketDataViewModel.intentFilter)

        assertThatIntentFilter(marketDataViewModel.intentFilter).containsAction(DropbitIntents.ACTION_BTC_PRICE_UPDATE)
    }

    @Test
    fun `clearing removes observer for broadcasts`() {
        val marketDataViewModel = MarketDataViewModel(mock(), mock(), mock(), mock())

        marketDataViewModel.onCleared()

        verify(marketDataViewModel.localBroadCastUtil).unregisterReceiver(marketDataViewModel.receiver)

    }
}

