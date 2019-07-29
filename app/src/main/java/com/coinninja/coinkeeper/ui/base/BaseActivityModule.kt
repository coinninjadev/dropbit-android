package com.coinninja.coinkeeper.ui.base

import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import dagger.Module
import dagger.Provides

@Module
class BaseActivityModule {

    @Provides
    fun provideWalletViewModel(syncWalletManager: SyncWalletManager,
                               syncManagerViewNotifier: SyncManagerViewNotifier,
                               walletManager: WalletHelper,
                               currencyPreference: CurrencyPreference): WalletViewModel  {

        return WalletViewModel(syncWalletManager, syncManagerViewNotifier, walletManager, currencyPreference)
    }

    @Provides
    fun provideActionbarController(walletViewModel: WalletViewModel):ActionBarController = ActionBarController(walletViewModel)
}
