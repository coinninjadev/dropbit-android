package com.coinninja.coinkeeper.ui.actionbar

import android.content.Context
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test


class ActionBarControllerTest {

    private val context: Context = mock()
    private fun createController(): ActionBarController = ActionBarController(mock())

    @Test
    fun actionbar_gone_configuration_test() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_gone

        controller.setTheme(context, actionBarTyped)

        assertTrue(controller.isActionBarGone)
        assertFalse(controller.isTitleUppercase)
        assertFalse(controller.isUpEnabled)
        assertThat(controller.optionMenuLayout).isNotNull()
    }


    @Test
    fun actionbar_light_up_on_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_on

        controller.setTheme(context, actionBarTyped)

        assertNull(controller.isActionBarGone)
        assertTrue(controller.isUpEnabled)
        assertFalse(controller.isTitleUppercase)
        assertThat(controller.optionMenuLayout).isNull()
    }

    @Test
    fun actionbar_light_up_off_configuration_test() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_off

        controller.setTheme(context, actionBarTyped)

        assertNull(controller.isActionBarGone)
        assertFalse(controller.isUpEnabled)
        assertFalse(controller.isTitleUppercase)
        assertNull(controller.optionMenuLayout)
    }

    @Test
    fun actionbar_light_up_off_skip_on_configuration_test() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_off_skip_on

        controller.setTheme(context, actionBarTyped)

        assertThat(controller.isActionBarGone).isEqualTo(false)
        assertThat(controller.isUpEnabled).isEqualTo(false)
        assertThat(controller.isTitleUppercase).isEqualTo(false)
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_skip_menu)
    }

    @Test
    fun actionbar_light_up_on_skip_on_configuration_test() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on

        controller.setTheme(context, actionBarTyped)

        assertFalse(controller.isActionBarGone)
        assertTrue(controller.isUpEnabled)
        assertFalse(controller.isTitleUppercase)
        assertThat(controller.optionMenuLayout).isEqualTo(R.menu.actionbar_light_skip_menu)
    }

    @Test(expected = IllegalStateException::class)
    private fun throw_illegal_state_exception_when_theme_unknown() {
        val controller = createController()

        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = -1

        controller.setTheme(context, actionBarTyped)

    }

    @Test
    fun remove_can_container_layout_if_them_is_action_gone() {
        val controller = createController()
        val cnContainerLayout = mock<View>()
        whenver<Any>(context.findViewById(R.id.cn_appbar_layout_container)).thenReturn(cnContainerLayout)
        controller.setIsActionBarGone(true)

        controller.displayTitle(context)

        verify(cnContainerLayout).visibility = View.GONE
    }

    @Test
    fun init_title_view_directly_after_setting_up_theme() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on
        val supportActionBar = mock(ActionBar::class.java)
        val titleView = mock(TextView::class.java)
        whenver(context.supportActionBar).thenReturn(supportActionBar)
        whenver<Any>(context.findViewById(R.id.appbar_title)).thenReturn(titleView)


        controller.setTheme(context, actionBarTyped)


        verify(titleViewManager).setActionBar(supportActionBar)
        verify(titleViewManager).setTitleView(titleView)
    }

    @Test
    fun do_not_init_title_view_when_theme_is_action_gone() {
        val controller = createController()
        val actionBarTyped = TypedValue()
        actionBarTyped.resourceId = R.id.actionbar_gone
        val supportActionBar = mock(ActionBar::class.java)
        val titleView = mock(TextView::class.java)

        controller.setTheme(context, actionBarTyped)

        verify(titleViewManager, never()).setActionBar(supportActionBar)
        verify(titleViewManager, never()).setTitleView(titleView)
    }

    @Test
    fun if_upper_case_is_true_render_uppercase_title_view_when_displayTitle_is_called() {
        val controller = createController()
        controller.setTitleViewManager(titleViewManager)
        controller.setIsTitleUppercase(true)

        controller.displayTitle(context)

        verify(titleViewManager).renderUpperCaseTitleView()
    }

    @Test
    fun if_up_is_enabled_then_setDisplayHomeAsUpEnabled_true() {
        val controller = createController()
        val menu = mock(Menu::class.java)
        val supportActionBar = mock(ActionBar::class.java)
        whenver(context.supportActionBar).thenReturn(supportActionBar)
        controller.setIsUpEnabled(true)

        controller.inflateActionBarMenu(context, menu)

        verify(supportActionBar).setDisplayHomeAsUpEnabled(true)
    }

    @Test
    fun if_up_is_not_enabled_then_setDisplayHomeAsUpEnabled_false() {
        val controller = createController()
        val menu = mock(Menu::class.java)
        val supportActionBar = mock(ActionBar::class.java)
        whenver(context.supportActionBar).thenReturn(supportActionBar)
        controller.setIsUpEnabled(false)

        controller.inflateActionBarMenu(context, menu)

        verify(supportActionBar).setDisplayHomeAsUpEnabled(false)
    }


    @Test
    fun if_up_is_null_then_do_nothing() {
        val controller = createController()
        val menu = mock(Menu::class.java)
        val supportActionBar = mock(ActionBar::class.java)
        controller.setIsUpEnabled(null)

        controller.inflateActionBarMenu(context, menu)

        verify(supportActionBar, never()).setDisplayHomeAsUpEnabled(ArgumentMatchers.anyBoolean())
    }


    @Test
    fun if_optionMenuLayout_has_any_value_inflate_it() {
        val controller = createController()
        val menu = mock(Menu::class.java)
        val menuInflater = mock(MenuInflater::class.java)
        whenver(context.menuInflater).thenReturn(menuInflater)
        controller.setOptionMenuLayout(R.menu.actionbar_dark_close_menu)

        controller.inflateActionBarMenu(context, menu)

        verify(menuInflater).inflate(R.menu.actionbar_dark_close_menu, menu)
    }

    @Test
    fun if_optionMenuLayout_is_null_then_do_nothing() {
        val controller = createController()
        val menu = mock(Menu::class.java)
        val menuInflater = mock(MenuInflater::class.java)
        controller.setOptionMenuLayout(null)

        controller.inflateActionBarMenu(context, menu)

        verify(menuInflater, never()).inflate(R.menu.actionbar_dark_close_menu, menu)
    }

    @Test
    fun if_optionMenuLayout_is_null_then_onMenuItemClicked_do_nothing() {
        val controller = createController()
        val item = mock(MenuItem::class.java)
        val menuItemClickListener = mock(MenuItemClickListener::class.java)
        controller.menuItemClickListener = menuItemClickListener
        controller.setOptionMenuLayout(null)

        val itemClicked = controller.onMenuItemClicked(item)

        assertFalse(itemClicked)
        verify(menuItemClickListener, never()).onCloseClicked()
        verify(menuItemClickListener, never()).onSkipClicked()
    }

    @Test
    fun if_action_skip_btn_clicked_then_call_menuItemClickListener_onSkipClicked() {
        val controller = createController()
        val item = mock(MenuItem::class.java)
        whenver(item.itemId).thenReturn(R.id.action_skip_btn)
        val menuItemClickListener = mock(MenuItemClickListener::class.java)
        controller.menuItemClickListener = menuItemClickListener
        controller.setOptionMenuLayout(R.menu.actionbar_light_skip_menu)

        val itemClicked = controller.onMenuItemClicked(item)

        verify(menuItemClickListener).onSkipClicked()
        assertTrue(itemClicked)
        verify(menuItemClickListener, never()).onCloseClicked()
    }

    @Test
    fun if_action_close_btn_clicked_then_call_menuItemClickListener_onSkipClicked() {
        val controller = createController()
        val item = mock(MenuItem::class.java)
        whenver(item.itemId).thenReturn(R.id.action_close_btn)
        val menuItemClickListener = mock(MenuItemClickListener::class.java)
        controller.menuItemClickListener = menuItemClickListener
        controller.setOptionMenuLayout(R.menu.actionbar_dark_close_menu)

        val itemClicked = controller.onMenuItemClicked(item)

        verify(menuItemClickListener).onCloseClicked()
        assertTrue(itemClicked)
        verify(menuItemClickListener, never()).onSkipClicked()
    }

    @Test
    fun if_a_menu_item_was_clicked_but_is_unknown_the_return_false() {
        val controller = createController()
        val item = mock(MenuItem::class.java)
        whenver(item.itemId).thenReturn(-1)
        val menuItemClickListener = mock(MenuItemClickListener::class.java)
        controller.menuItemClickListener = menuItemClickListener
        controller.setOptionMenuLayout(R.menu.actionbar_dark_close_menu)

        val itemClicked = controller.onMenuItemClicked(item)

        assertFalse(itemClicked)
        verify(menuItemClickListener, never()).onCloseClicked()
        verify(menuItemClickListener, never()).onSkipClicked()
    }

    @Test
    fun if_isActionBarGone_false_then_when_updateTitle_then_call_titleViewManager_renderTitleView() {
        val controller = createController()
        controller.setIsActionBarGone(false)

        controller.updateTitle("--- some new title")

        verify(titleViewManager).renderTitleView("--- some new title")
    }


    @Test
    fun if_isActionBarGone_true_then_do_nothing_when_updateTitle() {
        val controller = createController()
        controller.setIsActionBarGone(true)

        controller.updateTitle("--- some new title")

        verify(titleViewManager, never()).renderTitleView(ArgumentMatchers.anyString())
    }


    @Test
    fun if_isActionBarGone_null_then_do_nothing_when_updateTitle() {
        val controller = createController()
        controller.setIsActionBarGone(true)

        controller.updateTitle("--- some new title")

        verify(titleViewManager, never()).renderTitleView(ArgumentMatchers.anyString())
    }

    @Test
    fun observes_chart_menu_item_click() {
        val controller = createController()
        val item = mock(MenuItem::class.java)
        whenver(item.itemId).thenReturn(R.id.action_chart_button)
        val menuItemClickListener = mock(MenuItemClickListener::class.java)
        controller.menuItemClickListener = menuItemClickListener
        controller.setOptionMenuLayout(R.menu.actionbar_light_charts_menu)

        val itemClicked = controller.onMenuItemClicked(item)

        assertTrue(itemClicked)
        verify(menuItemClickListener).onShowMarketData()
    }
}