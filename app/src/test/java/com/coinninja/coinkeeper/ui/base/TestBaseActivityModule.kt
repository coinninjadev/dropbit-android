package com.coinninja.coinkeeper.ui.base

import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides

@Module
class TestBaseActivityModule {

    @Provides
    fun provideActionbarController(): ActionBarController = mock()

}
