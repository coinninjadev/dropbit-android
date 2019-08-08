package com.coinninja.coinkeeper.ui.base

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController

@RunWith(AndroidJUnit4::class)
class BaseActivityTest {


    private val activityController: ActivityController<SettingsActivity> = Robolectric.buildActivity(SettingsActivity::class.java)
    private val activity: BaseActivity = activityController.get()
    private val application get() = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()

    @Before
    @Throws(Exception::class)
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        application.typedValue = mock()
        application.activityNavigationUtil = mock()
        application.cnWalletManager = mock()
        application.yearlyHighViewModel = mock()
        whenever(application.yearlyHighViewModel.isSubscribedToYearlyHigh).thenReturn(mock())
    }

    @Test
    fun adds_tab_to_appbar() {
        setupWithTheme(R.style.CoinKeeperTheme_Drawer_BalanceOn_ChartsOn)

        activity.addTabToAppBar(R.layout.home_appbar_tab_2, 1)

        verify(activity.actionBarController).addTab(activity, R.layout.home_appbar_tab_2, 1)
    }

    @Test
    fun during_setContentView_set_the_resolveAttribute_on_injected_TypedValue() {
        setupWithTheme(R.style.CoinKeeperTheme_NoActionBar_BlockChain)
        val actionBarType = activity.actionBarType

        assertThat(actionBarType.resourceId, equalTo(R.id.actionbar_gone))
    }

    @Test
    fun during_onResume_if_hasSkippedBackup_then_showBackupNowDrawerActions() {
        whenever(application.cnWalletManager.hasSkippedBackup()).thenReturn(true)
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        verify(activity.drawerController).showBackupNowDrawerActions()
    }

    @Test
    fun during_onBackPressed_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(activity.drawerController.isDrawerOpen).thenReturn(true)

        activity.onBackPressed()

        verify(activity.drawerController).closeDrawer()
    }

    @Test
    fun during_onPause_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(activity.drawerController.isDrawerOpen).thenReturn(true)

        activityController.pause()

        verify(activity.drawerController).closeDrawerNoAnimation()
    }

    @Test
    fun on_close_action_clicked_start_home_activity() {
        setupWithTheme(R.style.CoinKeeperTheme)

        activity.onCloseClicked()

        verify(application.activityNavigationUtil).navigateToHome(activity)
    }

    @Test
    fun navigate_to_home_activity_when_close_button_is_clicked() {
        setupWithTheme(R.style.CoinKeeperTheme)
        val shadowActivity = shadowOf(activity)
        shadowActivity.resetIsFinishing()

        activity.onCloseClicked()

        verify(application.activityNavigationUtil).navigateToHome(activity)
    }

    @Test
    fun shows_market_charts_when_charts_menu_item_pressed() {
        setupWithTheme(R.style.CoinKeeperTheme_Drawer)

        activity.onShowMarketData()

        verify(application.activityNavigationUtil).showMarketCharts(activity)
    }

    private fun setupWithTheme(theme: Int) {
        activity.setTheme(theme)
        activityController.setup()
    }
}