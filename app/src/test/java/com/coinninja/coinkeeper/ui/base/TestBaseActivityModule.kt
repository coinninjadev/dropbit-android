package com.coinninja.coinkeeper.ui.base

import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor
import com.coinninja.coinkeeper.service.runner.HealthCheckTimerRunner
import com.coinninja.coinkeeper.ui.actionbar.ActionbarControllerProvider
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerControllerProvider
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.coinninja.coinkeeper.viewModel.WalletViewModelProvider
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides

@Module
class TestBaseActivityModule {

    @Provides
    fun provideActionbarController(): ActionbarControllerProvider {
        val actionbarControllerProvider: ActionbarControllerProvider = mock()
        whenever(actionbarControllerProvider.provide(any(), any())).thenReturn(mock())
        return actionbarControllerProvider
    }

    @Provides
    fun drawerControllerProvider(): DrawerControllerProvider {
        val drawerControllerProvider: DrawerControllerProvider = mock()
        whenever(drawerControllerProvider.provide(any(), any())).thenReturn(mock())
        return drawerControllerProvider
    }

    @Provides
    fun provideHealthCheckRunner(): HealthCheckTimerRunner = mock()


    @Provides
    fun provideInternalNotificationInteractor(): InternalNotificationsInteractor = mock()

    @Provides
    fun provideAccountModeManager(): AccountModeManager {
        val manager: AccountModeManager = mock()
        whenever(manager.accountMode).thenReturn(AccountMode.LIGHTNING)
        return manager
    }

    @Provides
    fun walletViewModelProvider(): WalletViewModelProvider {
        val walletViewModelProvider: WalletViewModelProvider = mock()
        val walletViewModel = mock<WalletViewModel>()
        whenever(walletViewModelProvider.provide(any<BaseActivity>())).thenReturn(walletViewModel)
        whenever(walletViewModel.fetchBtcLatestPrice()).thenReturn(mock())
        whenever(walletViewModel.fetchLightningBalance()).thenReturn(mock())
        whenever(walletViewModel.isLightningLocked).thenReturn(mock())
        whenever(walletViewModel.currentPrice).thenReturn(mock())
        whenever(walletViewModel.accountMode).thenReturn(mock())
        whenever(walletViewModel.holdingsWorth).thenReturn(mock())
        whenever(walletViewModel.holdings).thenReturn(mock())
        return walletViewModelProvider
    }
}
