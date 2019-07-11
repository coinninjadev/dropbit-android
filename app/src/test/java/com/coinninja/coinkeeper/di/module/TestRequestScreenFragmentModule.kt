package com.coinninja.coinkeeper.di.module

import com.coinninja.coinkeeper.viewModel.QrViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
class TestRequestScreenFragmentModule {

    @Provides
    fun provideQRViewModel(): QrViewModel = mock {
        whenever(it.qrCodeUri).thenReturn(mock())
    }

}
