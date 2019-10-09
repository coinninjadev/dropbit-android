package com.coinninja.coinkeeper.ui.actionbar

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil

@Mockable
class ActionbarControllerProvider {
    fun provide(activityNavigationUtil: ActivityNavigationUtil): ActionBarController {
        return ActionBarController(activityNavigationUtil)
    }
}