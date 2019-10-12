package com.coinninja.coinkeeper.ui.actionbar.managers

import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.android.helpers.Views.withId
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.view.widget.DrawerLayout
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class DrawerControllerTest {


    private var activity: AppCompatActivity = Robolectric.setupActivity(HomeActivity::class.java)

    private fun createController(): DrawerController {
        val controller = DrawerController(mock(), mock(), "1.1.1", mock(), mock())
        whenever(controller.walletViewModel.currentPrice).thenReturn(mock())
        return controller
    }

    private val actionbarType: TypedValue
        get() = TypedValue().also {
            it.resourceId = R.id.actionbar_up_on_with_nav_bar
        }

    @Test
    fun adds_drawer_as_root_view() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        assertThat(withId<View>(activity, R.id.drawer_action_view)).isNotNull()
    }

    @Test
    fun does_not_add_drawer_as_root_view__when_not_drawer_theme() {
        val drawerController = createController()
        actionbarType.resourceId = R.id.actionbar_up_on

        drawerController.inflateDrawer(activity, actionbarType)

        assertThat(withId<View>(activity, R.id.drawer_action_view)).isNotNull()
    }

    @Test
    fun when_inflating_drawer_set_on_click_listener_for_drawer_setting_button() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        drawerController.openDrawer()
        activity.findViewById<View>(R.id.drawer_setting).performClick()

        verify(drawerController.activityNavigationUtil).navigateToSettings(activity)
    }

    @Test
    fun when_inflating_drawer_set_on_click_listener_for_drawer_support_button() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        withId<View>(activity, R.id.drawer_support)!!.performClick()

        verify(drawerController.activityNavigationUtil).navigateToSupport(activity)
    }

    @Test
    fun open_drawer() {
        val drawerController = createController()
        drawerController.drawerLayout = mock()

        drawerController.openDrawer()

        verify(drawerController.drawerLayout)!!.openDrawer(GravityCompat.START)
    }

    @Test
    fun close_drawer() {
        val drawerController = createController()
        drawerController.drawerLayout = mock()

        drawerController.closeDrawer()

        verify(drawerController.drawerLayout)!!.closeDrawer(GravityCompat.START)
    }

    @Test
    fun handles_null_drawer___open__close() {
        val drawerController = createController()
        drawerController.drawerLayout = null

        // fails if null pointer thrown
        drawerController.openDrawer()
        drawerController.closeDrawer()
        drawerController.closeDrawerNoAnimation()
    }

    @Test
    fun close_drawer_no_animation() {
        val drawerController = createController()
        drawerController.drawerLayout = mock()

        drawerController.closeDrawerNoAnimation()

        verify(drawerController.drawerLayout, never())!!.closeDrawer(GravityCompat.START)
        verify(drawerController.drawerLayout)!!.closeDrawer(GravityCompat.START, false)
    }


    @Test
    fun return_true_when_drawer_is_open() {
        val drawerController = createController()
        val drawerLayout = mock<DrawerLayout>()
        drawerController.drawerLayout = drawerLayout

        whenever(drawerLayout.isDrawerOpen(GravityCompat.START)).thenReturn(true)
        assertThat(drawerController.isDrawerOpen).isTrue()

        whenever(drawerLayout.isDrawerOpen(GravityCompat.START)).thenReturn(false)
        assertThat(drawerController.isDrawerOpen).isFalse()

        drawerController.drawerLayout = null
        assertThat(drawerController.isDrawerOpen).isFalse()
    }

    @Test
    fun display_app_version() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        val version = drawerController.drawerLayout!!.findViewById<TextView>(R.id.drawer_action_footer_version)

        assertThat(version.text).isEqualTo("Version 1.1.1")
    }

    @Test
    fun display_app_version_protect_ageist_non_inflation() {
        val drawerController = createController()
        val type = actionbarType.also {
            it.resourceId = R.id.actionbar_up_on
        }

        drawerController.inflateDrawer(activity, type)

        assertThat(withId<View>(activity, R.id.drawer_action_footer_version)).isNull()
    }

    @Test
    fun show_back_up_now_drawer_action() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        drawerController.showBackupNowDrawerActions()

        val toolbar = withId<Toolbar>(activity, R.id.toolbar)
        val settings = withId<ImageView>(activity, R.id.setting_icon)

        verify(drawerController.badgeRenderer).renderBadge(toolbar)
        verify(drawerController.badgeRenderer).renderBadge(settings)
        assertThat(withId<View>(activity, R.id.drawer_backup_now)!!.visibility).isEqualTo(View.VISIBLE)
    }

    @Test
    fun show_badge_if_phone_is_not_verified() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        drawerController.renderBadgeForUnverifiedDeviceIfNecessary()

        val phoneImage = withId<ImageView>(activity, R.id.contact_phone)
        val toolbar = withId<Toolbar>(activity, R.id.toolbar)

        verify(drawerController.badgeRenderer).renderBadge(phoneImage)
        verify(drawerController.badgeRenderer).renderBadge(toolbar)
    }

    @Test
    fun show_back_up_now_drawer_action_set_on_click_listener_for() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)
        drawerController.showBackupNowDrawerActions()

        withId<View>(activity, R.id.drawer_backup_now)!!.performClick()

        verify(drawerController.activityNavigationUtil).navigateToBackupRecoveryWords(activity)
    }

    @Test
    fun menu_item_clicked_for_user_verification_requests_dropbit_me_view() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)
        drawerController.drawerLayout = mock()

        withId<View>(activity, R.id.drawer_phone)!!.performClick()

        verify(drawerController.activityNavigationUtil).navigateToUserVerification(activity)
    }

    @Test
    fun menu_item_clicked_id_is_android_home_then_open_drawer() {
        val drawerController = createController()
        val menuItem: MenuItem = mock()
        whenever(menuItem.itemId).thenReturn(android.R.id.home)
        drawerController.inflateDrawer(activity, actionbarType)
        drawerController.drawerLayout = mock()

        assertThat(drawerController.onMenuItemClicked(menuItem)).isTrue()
        verify(drawerController.drawerLayout)!!.openDrawer(GravityCompat.START)
    }

    @Test
    fun menu_item_clicked_protect_ageist_non_inflation() {
        val drawerController = createController()
        val menuItem: MenuItem = mock()
        whenever(menuItem.itemId).thenReturn(android.R.id.home)
        val type = actionbarType.also {
            it.resourceId = R.id.actionbar_up_on
        }

        drawerController.inflateDrawer(activity, type)

        assertThat(drawerController.onMenuItemClicked(menuItem)).isFalse()
    }

    @Test
    fun menu_item_clicked_id_is_not_android_home_then_do_nothing() {
        val drawerController = createController()
        val menuItem: MenuItem = mock()
        whenever(menuItem.itemId).thenReturn(R.id.action_close_btn)

        drawerController.inflateDrawer(activity, actionbarType)

        assertThat(drawerController.onMenuItemClicked(menuItem)).isFalse()
    }

    @Test
    fun sets_price_change_observer_when_initialized() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        verify(drawerController.walletViewModel.currentPrice).observe(activity, drawerController.currentPriceObserver)
        verify(drawerController.walletViewModel).loadHoldingBalances()
    }

    @Test
    fun updates_price_when_observed() {
        val drawerController = createController()
        drawerController.inflateDrawer(activity, actionbarType)

        drawerController.currentPriceObserver.onChanged(null)
        val price = USDCurrency(5600.00)
        drawerController.currentPriceObserver.onChanged(price)

        val priceView = withId<TextView>(activity, R.id.drawer_action_price_text)

        assertThat(priceView!!.text).isEqualTo(price.toFormattedCurrency())
    }
}