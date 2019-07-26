package com.coinninja.coinkeeper.ui.base

import android.view.Menu
import android.view.MenuItem
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.ui.settings.SettingsActivity
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.nhaarman.mockitokotlin2.atLeast
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
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
        application.actionBarController = mock()
        application.drawerController = mock()
        application.activityNavigationUtil = mock()
        application.cnWalletManager = mock()
        application.yearlyHighViewModel = mock()
        whenever(application.yearlyHighViewModel.isSubscribedToYearlyHigh).thenReturn(mock())
    }

    @Test
    fun during_setContentView_configure_action_bar_controller() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        verify(application.actionBarController).setTheme(activity, activity.actionBarType)
    }

    @Test
    fun during_setContentView_set_the_resolveAttribute_on_injected_TypedValue() {
        setupWithTheme(R.style.CoinKeeperTheme_NoActionBar_BlockChain)
        val actionBarType = activity.actionBarType

        assertThat(actionBarType.resourceId, equalTo(R.id.actionbar_gone))
    }

    @Test
    fun during_setContentView_display_title() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        verify(application.actionBarController).displayTitle(activity)
    }

    @Test
    fun during_onResume_if_hasSkippedBackup_then_showBackupNowDrawerActions() {
        whenever(application.cnWalletManager.hasSkippedBackup()).thenReturn(true)
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        verify(application.drawerController).showBackupNowDrawerActions()
    }

    @Test
    fun during_onCreateOptionsMenu_inflate_menu() {
        val menu = mock(Menu::class.java)
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        activity.onCreateOptionsMenu(menu)

        verify(application.actionBarController).inflateActionBarMenu(activity, menu)
    }

    @Test
    fun when_onOptionsItemSelected_call_onMenuItemClicked_and_return_boolean() {
        val item = mock(MenuItem::class.java)
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(application.actionBarController.onMenuItemClicked(item)).thenReturn(true)

        val itemSelected = activity.onOptionsItemSelected(item)

        verify(application.actionBarController).onMenuItemClicked(item)
        assertTrue(itemSelected)
    }

    @Test
    fun when_onOptionsItemSelected_call_drawerController_onMenuItemClicked_and_return_boolean() {
        val item = mock(MenuItem::class.java)
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(application.actionBarController.onMenuItemClicked(item)).thenReturn(false)
        whenever(application.drawerController.onMenuItemClicked(item)).thenReturn(true)

        val itemSelected = activity.onOptionsItemSelected(item)

        verify(application.drawerController).onMenuItemClicked(item)
        assertTrue(itemSelected)
    }

    @Test
    fun during_onBackPressed_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(application.drawerController.isDrawerOpen).thenReturn(true)

        activity.onBackPressed()

        verify(application.drawerController).closeDrawer()
    }

    @Test
    fun during_onPause_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(application.drawerController.isDrawerOpen).thenReturn(true)

        activity.onPause()

        verify(application.drawerController).closeDrawerNoAnimation()
    }

    @Test
    fun during_onPriceReceived_updatePriceOfBtcDisplay_of_drawer() {
        val price = USDCurrency("500")
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        activity.onPriceReceived(price)

        verify(application.drawerController).updatePriceOfBtcDisplay(price)
    }

    @Test
    fun if_onOptionsItemSelected_returns_false_then_call_super() {
        val item = mock(MenuItem::class.java)
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)
        whenever(application.actionBarController.onMenuItemClicked(item)).thenReturn(false)

        val itemSelected = activity.onOptionsItemSelected(item)

        verify(application.actionBarController).onMenuItemClicked(item)
        assertFalse(itemSelected)
    }

    @Test
    fun during_onCreateOptionsMenu_set_menu_item_click_listener() {
        val menu: Menu = mock()
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        activity.onCreateOptionsMenu(menu)

        verify(application.actionBarController, atLeast(1)).menuItemClickListener = activity
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
    fun updateActivityLabel() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff)

        activity.updateActivityLabel("-- some text")

        verify(application.actionBarController).displayTitle(activity, "-- some text")
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