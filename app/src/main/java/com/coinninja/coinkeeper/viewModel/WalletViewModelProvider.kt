package com.coinninja.coinkeeper.viewModel

import androidx.lifecycle.ViewModelProviders
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference


@Mockable
class WalletViewModelProvider constructor(val syncWalletManager: SyncWalletManager,
                                          val syncManagerViewNotifier: SyncManagerViewNotifier,
                                          val walletHelper: WalletHelper,
                                          val currencyPreference: CurrencyPreference,
                                          val thunderDomeRepository: ThunderDomeRepository,
                                          val accountModeManager: AccountModeManager
) {
    fun <T : BaseActivity> provide(activity: T): WalletViewModel {
        return bind(ViewModelProviders.of(activity)[WalletViewModel::class.java])
    }

    fun <T : BaseFragment> provide(fragment: T): WalletViewModel {
        return bind(ViewModelProviders.of(fragment)[WalletViewModel::class.java])
    }

    private fun bind(walletViewModel: WalletViewModel): WalletViewModel {
        walletViewModel.syncWalletManager = syncWalletManager
        walletViewModel.syncManagerViewNotifier = syncManagerViewNotifier
        walletViewModel.walletHelper = walletHelper
        walletViewModel.currencyPreference = currencyPreference
        walletViewModel.thunderDomeRepository = thunderDomeRepository
        walletViewModel.accountModeManager = accountModeManager
        walletViewModel.setupObservers()
        walletViewModel.checkLightningLock()
        walletViewModel.currentMode()
        return walletViewModel
    }
}