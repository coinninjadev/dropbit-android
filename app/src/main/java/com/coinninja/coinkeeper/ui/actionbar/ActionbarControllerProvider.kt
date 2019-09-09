package com.coinninja.coinkeeper.ui.actionbar

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.viewModel.WalletViewModel

@Mockable
class ActionbarControllerProvider {
    fun provide(walletViewModel: WalletViewModel, activityNavigationUtil: ActivityNavigationUtil): ActionBarController {
        return ActionBarController(walletViewModel, activityNavigationUtil)
    }
}