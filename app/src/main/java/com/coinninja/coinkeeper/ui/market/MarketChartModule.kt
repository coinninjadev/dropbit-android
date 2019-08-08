package com.coinninja.coinkeeper.ui.market

import app.dropbit.commons.util.CoroutineContextProvider
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.viewModel.MarketDataViewModel
import dagger.Module
import dagger.Provides

@Module
class MarketChartModule {
    @Provides
    fun provideMarketChartDataViewModel(contextProvider: CoroutineContextProvider,
                                        walletHelper: WalletHelper,
                                        signedCoinKeeperApiClient: SignedCoinKeeperApiClient,
                                        localBroadCastUtil: LocalBroadCastUtil): MarketDataViewModel {

        return MarketDataViewModel(contextProvider, walletHelper, signedCoinKeeperApiClient, localBroadCastUtil)
    }

    @Provides
    fun provideMarketChartConfiguration(): MarketChartController = MarketChartController()

    @Provides
    fun provideMarketChartButtonBarController(): MarketChartButtonBarController = MarketChartButtonBarController()

}
