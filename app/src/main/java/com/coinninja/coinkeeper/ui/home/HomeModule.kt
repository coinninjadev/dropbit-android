package com.coinninja.coinkeeper.ui.home

import dagger.Module
import dagger.Provides


@Module
class HomeModule {
    @Provides
    fun provideHomeScreenViewPagerAdapterProvider(): HomePagerAdapterProvider = HomePagerAdapterProvider()

}