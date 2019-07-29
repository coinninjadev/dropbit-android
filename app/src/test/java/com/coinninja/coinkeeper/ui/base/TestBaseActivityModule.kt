package com.coinninja.coinkeeper.ui.base

import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
class TestBaseActivityModule {

    @Provides
    fun provideWalletViewModel(): WalletViewModel = WalletViewModel(mock(), mock(), mock(), mock()).also {
        whenever(it.syncInProgress).thenReturn(mock())
        whenever(it.chainHoldings).thenReturn(mock())
        whenever(it.chainHoldingsWorth).thenReturn(mock())
        whenever(it.defaultCurrencyPreference).thenReturn(mock())
    }

    @Provides
    fun provideActionbarController(walletViewModel: WalletViewModel):ActionBarController = ActionBarController(walletViewModel)

}
