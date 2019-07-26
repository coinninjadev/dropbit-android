package com.coinninja.coinkeeper.ui.news

import app.dropbit.commons.util.CoroutineContextProvider
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides

@Module
class MarketNewsModule {

    @Provides
    fun provideNewsAdapter(activityNavigationUtil: ActivityNavigationUtil): NewsAdapter = NewsAdapter(activityNavigationUtil, Picasso.get(), NewsSourceMap())

    @Provides
    fun provideNewsViewModel(apiClient: SignedCoinKeeperApiClient): NewsViewModel = NewsViewModel(CoroutineContextProvider(), apiClient)

}
