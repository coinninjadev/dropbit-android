package com.coinninja.coinkeeper.ui.actionbar

import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.coinninja.coinkeeper.R
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class ActionBarControllerTest {

    private val activity: AppCompatActivity = mock()
    private fun createController(): ActionBarController = ActionBarController(mock())

    @Test
    fun actionbar_gone_configuration_test() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_gone

        controller.setTheme(activity, actionBarTyped)

        assertThat(controller.isActionBarGone).isTrue()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isNull()
    }


    @Test
    fun actionbar_light_up_on_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_on

        controller.setTheme(activity, actionBarTyped)

        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isNull()
    }

    @Test
    fun actionbar_light_up_off_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_off

        controller.setTheme(activity, actionBarTyped)

        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isNull()
    }

    @Test
    fun actionbar_light_up_off_skip_on_configuration_test() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_off_skip_on

        controller.setTheme(activity, actionBarTyped)

        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isFalse()
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_skip_menu)
    }

    @Test
    fun actionbar_light_up_on_skip_on_configuration_test() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on

        controller.setTheme(activity, actionBarTyped)

        assertThat(controller.isActionBarGone).isFalse()
        assertThat(controller.isUpEnabled).isTrue()
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_skip_menu)
    }

    @Test(expected = IllegalStateException::class)
    fun throw_illegal_state_exception_when_theme_unknown() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = -1

        controller.setTheme(activity, actionBarTyped)

    }

    @Test
    fun remove_can_container_layout_if_them_is_action_gone() {
        val controller = createController()
        val cnContainerLayout = mock<View>()
        whenever(activity.findViewById<View>(R.id.cn_appbar_layout_container)).thenReturn(cnContainerLayout)
        controller.isActionBarGone = true

        controller.displayTitle(activity)

        verify(cnContainerLayout).visibility = View.GONE
    }

    @Test
    fun init_title_view_directly_after_setting_up_theme() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on
        val titleView: TextView = mock()
        whenever(activity.supportActionBar).thenReturn(mock())
        whenever(activity.findViewById<TextView>(R.id.appbar_title)).thenReturn(titleView)


        controller.setTheme(activity, actionBarTyped)


        verify(controller.titleViewManager).actionBar = activity.supportActionBar
        verify(controller.titleViewManager).titleView = titleView
    }

    @Test
    fun do_not_init_title_view_when_theme_is_action_gone() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_gone

        controller.setTheme(activity, actionBarTyped)

        verify(controller.titleViewManager, never()).actionBar = any()
        verify(controller.titleViewManager, never()).titleView = any()
    }

    @Test
    fun if_upper_case_is_true_render_uppercase_title_view_when_displayTitle_is_called() {
        val controller = createController()

        controller.displayTitle(activity)

        verify(controller.titleViewManager).renderTitle()
    }

    @Test
    fun if_up_is_enabled_then_setDisplayHomeAsUpEnabled_true() {
        val controller = createController()
        whenever(activity.supportActionBar).thenReturn(mock())
        controller.isUpEnabled = true
        controller.optionMenuLayout = R.id.actionbar_up_on

        controller.inflateActionBarMenu(activity, mock())

        verify(activity.supportActionBar)!!.setDisplayHomeAsUpEnabled(true)
    }

    @Test
    fun if_up_is_not_enabled_then_setDisplayHomeAsUpEnabled_false() {
        val controller = createController()
        whenever(activity.supportActionBar).thenReturn(mock())
        whenever(activity.menuInflater).thenReturn(mock())
        controller.optionMenuLayout = R.id.actionbar_up_off

        controller.inflateActionBarMenu(activity, mock())

        verify(activity.supportActionBar)!!.setDisplayHomeAsUpEnabled(false)
    }

    @Test
    fun if_optionMenuLayout_has_any_value_inflate_it() {
        val controller = createController()
        whenever(activity.menuInflater).thenReturn(mock())
        controller.optionMenuLayout = R.menu.actionbar_dark_close_menu

        val menu = mock<Menu>()
        controller.inflateActionBarMenu(activity, menu)

        verify(activity.menuInflater).inflate(R.menu.actionbar_dark_close_menu, menu)
    }

    @Test
    fun if_optionMenuLayout_is_null_then_do_nothing() {
        val controller = createController()
        controller.optionMenuLayout = null
        whenever(activity.menuInflater).thenReturn(mock())

        val menu = mock<Menu>()
        controller.inflateActionBarMenu(activity, menu)

        verify(activity.menuInflater, never()).inflate(R.menu.actionbar_dark_close_menu, menu)
    }

    @Test
    fun if_optionMenuLayout_is_null_then_onMenuItemClicked_do_nothing() {
        val controller = createController()
        controller.menuItemClickListener = mock()
        controller.optionMenuLayout = null

        val itemClicked = controller.onMenuItemClicked(mock())

        assertFalse(itemClicked)
        verify(controller.menuItemClickListener, never())!!.onCloseClicked()
        verify(controller.menuItemClickListener, never())!!.onSkipClicked()
    }

    @Test
    fun if_action_skip_btn_clicked_then_call_menuItemClickListener_onSkipClicked() {
        val controller = createController()
        val item: MenuItem = mock()
        whenever(item.itemId).thenReturn(R.id.action_skip_btn)
        controller.menuItemClickListener = mock()
        controller.optionMenuLayout = R.menu.actionbar_light_skip_menu

        assertThat(controller.onMenuItemClicked(item)).isTrue()
        verify(controller.menuItemClickListener)!!.onSkipClicked()
        verify(controller.menuItemClickListener, never())!!.onCloseClicked()
    }

    @Test
    fun if_action_close_btn_clicked_then_call_menuItemClickListener_onSkipClicked() {
        val controller = createController()
        val item: MenuItem = mock()
        whenever(item.itemId).thenReturn(R.id.action_close_btn)
        controller.menuItemClickListener = mock()
        controller.optionMenuLayout = R.menu.actionbar_dark_close_menu

        assertThat(controller.onMenuItemClicked(item)).isTrue()

        verify(controller.menuItemClickListener)!!.onCloseClicked()
        verify(controller.menuItemClickListener, never())!!.onSkipClicked()
    }

    @Test
    fun if_a_menu_item_was_clicked_but_is_unknown_the_return_false() {
        val controller = createController()
        val item: MenuItem = mock()
        whenever(item.itemId).thenReturn(-1)
        controller.menuItemClickListener = mock()
        controller.optionMenuLayout = R.menu.actionbar_dark_close_menu

        val itemClicked = controller.onMenuItemClicked(item)

        assertFalse(itemClicked)
        verify(controller.menuItemClickListener, never())!!.onCloseClicked()
        verify(controller.menuItemClickListener, never())!!.onSkipClicked()
    }

    @Test
    fun if_isActionBarGone_false_then_when_updateTitle_then_call_controller__titleViewManager_renderTitleView() {
        val controller = createController()
        controller.isActionBarGone = false

        controller.updateTitle("--- some new title")

        verify(controller.titleViewManager).title = "--- some new title"
        verify(controller.titleViewManager).renderTitle()
    }

    @Test
    fun if_isActionBarGone_true_then_do_nothing_when_updateTitle() {
        val controller = createController()
        controller.isActionBarGone = true

        controller.updateTitle("--- some new title")

        verify(controller.titleViewManager, never()).renderTitle()
    }

    @Test
    fun observes_chart_menu_item_click() {
        val controller = createController()
        val item: MenuItem = mock()
        whenever(item.itemId).thenReturn(R.id.action_chart_button)
        controller.menuItemClickListener = mock()
        controller.optionMenuLayout = R.menu.actionbar_light_charts_menu

        val itemClicked = controller.onMenuItemClicked(item)

        assertTrue(itemClicked)
        verify(controller.menuItemClickListener)!!.onShowMarketData()
    }
}