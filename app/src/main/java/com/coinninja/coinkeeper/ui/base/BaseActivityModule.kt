package com.coinninja.coinkeeper.ui.base

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.actionbar.ActionbarControllerProvider
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerControllerProvider
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.ui.BadgeRenderer
import com.coinninja.coinkeeper.viewModel.WalletViewModelProvider
import dagger.Module
import dagger.Provides

@Module
class BaseActivityModule {

    @Provides
    fun walletViewModelProvider(
            syncWalletManager: SyncWalletManager,
            syncManagerViewNotifier: SyncManagerViewNotifier,
            walletHelper: WalletHelper,
            currencyPreference: CurrencyPreference,
            thunderDomeRepository: ThunderDomeRepository,
            accountModeManager: AccountModeManager
    ): WalletViewModelProvider = WalletViewModelProvider(syncWalletManager, syncManagerViewNotifier,
            walletHelper, currencyPreference, thunderDomeRepository, accountModeManager)

    @Provides
    fun provideActionbarControllerProvider(): ActionbarControllerProvider = ActionbarControllerProvider()

    @Provides
    fun drawerControllerProvider(@BuildVersionName buildVersionName: String,
                                 dropbitAccountHelper: DropbitAccountHelper): DrawerControllerProvider =
            DrawerControllerProvider(BadgeRenderer(), buildVersionName, dropbitAccountHelper)

}
