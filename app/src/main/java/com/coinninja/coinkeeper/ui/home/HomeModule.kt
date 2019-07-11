package com.coinninja.coinkeeper.ui.home

import androidx.lifecycle.Lifecycle
import dagger.Module
import dagger.Provides


@Module
class HomeModule {
    @Provides
    fun provideHomeScreenViewPagerAdapterProvider(): HomePagerAdapterProvider = HomePagerAdapterProvider()
}