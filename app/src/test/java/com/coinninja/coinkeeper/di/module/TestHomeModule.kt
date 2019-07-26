package com.coinninja.coinkeeper.di.module

import com.coinninja.coinkeeper.ui.home.HomePagerAdapter
import com.coinninja.coinkeeper.ui.home.HomePagerAdapterProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
class TestHomeModule {
    @Provides
    fun provideHomeScreenViewPagerAdapter(): HomePagerAdapterProvider = mock<HomePagerAdapterProvider>().also {
        val homePagerAdapter = mock<HomePagerAdapter>()
        whenever(it.provide(any(), any())).thenReturn(homePagerAdapter)
        whenever(homePagerAdapter.count).thenReturn(3)
    }
}
