package com.coinninja.coinkeeper.ui.actionbar.managers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.BuildVersionName
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.ui.BadgeRenderer
import com.coinninja.coinkeeper.viewModel.WalletViewModel

@Mockable
class DrawerControllerProvider constructor(
        internal val badgeRenderer: BadgeRenderer,
        @BuildVersionName internal val versionName: String,
        internal val dropbitAccountHelper: DropbitAccountHelper
) {
    fun provide(walletViewModel: WalletViewModel, activityNavigationUtil: ActivityNavigationUtil): DrawerController {
        return DrawerController(badgeRenderer, activityNavigationUtil, versionName, dropbitAccountHelper, walletViewModel)

    }
}