package com.coinninja.coinkeeper.ui.market

import com.coinninja.coinkeeper.viewModel.MarketDataViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
class TestMarketChartModule {
    @Provides
    fun provideMarketDataViewModel(): MarketDataViewModel = mock<MarketDataViewModel>().also{
        whenever(it.currentGranularity).thenReturn(mock())
        whenever(it.periodData).thenReturn(mock())
        whenever(it.currentBtcPrice).thenReturn(mock())
    }

}
