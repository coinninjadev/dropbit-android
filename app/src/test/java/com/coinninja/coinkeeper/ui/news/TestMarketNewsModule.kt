package com.coinninja.coinkeeper.ui.news

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
class TestMarketNewsModule {
    @Provides
    fun provideNewsAdapter(): NewsAdapter = mock()

    @Provides
    fun provideNewsViewModel(): NewsViewModel = mock<NewsViewModel>().also {
        whenever(it.articles).thenReturn(mock())
    }

}
