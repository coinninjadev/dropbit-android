package com.coinninja.coinkeeper.viewModel

import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference
import dagger.Module
import dagger.Provides

@Module
class WalletViewModelModule {
    @Provides
    fun walletViewModelProvider(
            syncWalletManager: SyncWalletManager,
            syncManagerViewNotifier: SyncManagerViewNotifier,
            walletHelper: WalletHelper,
            currencyPreference: CurrencyPreference,
            thunderDomeRepository: ThunderDomeRepository,
            accountModeManager: AccountModeManager
    ): WalletViewModelProvider {
        return WalletViewModelProvider(
                syncWalletManager,
                syncManagerViewNotifier,
                walletHelper,
                currencyPreference,
                thunderDomeRepository,
                accountModeManager
        )
    }
}