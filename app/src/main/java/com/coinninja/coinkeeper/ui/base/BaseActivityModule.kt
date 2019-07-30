package com.coinninja.coinkeeper.ui.base

import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.ui.BadgeRenderer
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import dagger.Module
import dagger.Provides

@Module
class BaseActivityModule {

    @Provides
    fun provideWalletViewModel(syncWalletManager: SyncWalletManager,
                               syncManagerViewNotifier: SyncManagerViewNotifier,
                               walletManager: WalletHelper,
                               currencyPreference: CurrencyPreference): WalletViewModel {

        return WalletViewModel(syncWalletManager, syncManagerViewNotifier, walletManager, currencyPreference)
    }

    @Provides
    fun provideActionbarController(walletViewModel: WalletViewModel): ActionBarController = ActionBarController(walletViewModel)

    @Provides
    fun provideDrawerController(activityNavigationUtil: ActivityNavigationUtil,
                                @BuildVersionName buildVersionName: String,
                                dropbitAccountHelper: DropbitAccountHelper, walletViewModel: WalletViewModel): DrawerController =
            DrawerController(BadgeRenderer(), activityNavigationUtil, buildVersionName, dropbitAccountHelper, walletViewModel)

}
