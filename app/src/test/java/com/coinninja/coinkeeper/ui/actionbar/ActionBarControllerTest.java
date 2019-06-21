package com.coinninja.coinkeeper.ui.actionbar;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.actionbar.managers.TitleViewManager;
import com.coinninja.coinkeeper.ui.base.MenuItemClickListener;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ActionBarControllerTest {

    @Mock
    private TitleViewManager titleViewManager;

    @Mock
    private AppCompatActivity context;

    @InjectMocks
    private ActionBarController controller;

    @After
    public void tearDown() throws Exception {
        titleViewManager = null;
        context = null;
        controller = null;
    }

    @Test
    public void actionbar_gone_configuration_test() {

        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_gone;

        controller.setTheme(context, actionBarTyped);

        assertTrue(controller.isActionBarGone);
        assertFalse(controller.isTitleUppercase);
        assertNull(controller.isUpEnabled);
        assertNull(controller.optionMenuLayout);
    }


    @Test
    public void actionbar_light_up_on_configuration_test() {
        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_up_on;

        controller.setTheme(context, actionBarTyped);

        assertNull(controller.isActionBarGone);
        assertTrue(controller.isUpEnabled);
        assertFalse(controller.isTitleUppercase);
        assertNull(controller.optionMenuLayout);
    }

    @Test
    public void actionbar_light_up_off_configuration_test() {
        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_up_off;

        controller.setTheme(context, actionBarTyped);

        assertNull(controller.isActionBarGone);
        assertFalse(controller.isUpEnabled);
        assertFalse(controller.isTitleUppercase);
        assertNull(controller.optionMenuLayout);
    }

    @Test
    public void actionbar_light_up_off_skip_on_configuration_test() {

        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_up_off_skip_on;

        controller.setTheme(context, actionBarTyped);

        assertNull(controller.isActionBarGone);
        assertFalse(controller.isUpEnabled);
        assertFalse(controller.isTitleUppercase);
        assertThat(controller.optionMenuLayout, equalTo(R.menu.actionbar_light_skip_menu));
    }

    @Test
    public void actionbar_light_up_on_skip_on_configuration_test() {

        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on;

        controller.setTheme(context, actionBarTyped);

        assertNull(controller.isActionBarGone);
        assertTrue(controller.isUpEnabled);
        assertFalse(controller.isTitleUppercase);
        assertThat(controller.optionMenuLayout, equalTo(R.menu.actionbar_light_skip_menu));
    }

    @Test(expected = IllegalStateException.class)
    public void throw_illegal_state_exception_when_theme_unknown() {

        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = -1;

        controller.setTheme(context, actionBarTyped);

    }

    @Test
    public void remove_can_container_layout_if_them_is_action_gone() {
        View cnContainerLayout = mock(View.class);
        when((context).findViewById(R.id.cn_appbar_layout_container)).thenReturn(cnContainerLayout);
        controller.isActionBarGone = true;

        controller.displayTitle(context);

        verify(cnContainerLayout).setVisibility(View.GONE);
    }

    @Test
    public void init_title_view_directly_after_setting_up_theme() {
        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_up_on_skip_on;
        ActionBar supportActionBar = mock(ActionBar.class);
        TextView titleView = mock(TextView.class);
        when(context.getSupportActionBar()).thenReturn(supportActionBar);
        when(context.findViewById(R.id.appbar_title)).thenReturn(titleView);


        controller.setTheme(context, actionBarTyped);


        verify(titleViewManager).setActionBar(supportActionBar);
        verify(titleViewManager).setTitleView(titleView);
    }

    @Test
    public void do_not_init_title_view_when_theme_is_action_gone() {
        TypedValue actionBarTyped = new TypedValue();
        actionBarTyped.resourceId = R.id.actionbar_gone;
        ActionBar supportActionBar = mock(ActionBar.class);
        TextView titleView = mock(TextView.class);

        controller.setTheme(context, actionBarTyped);

        verify(titleViewManager, never()).setActionBar(supportActionBar);
        verify(titleViewManager, never()).setTitleView(titleView);
    }

    @Test
    public void if_upper_case_is_true_render_uppercase_title_view_when_displayTitle_is_called() {
        controller.titleViewManager = titleViewManager;
        controller.isTitleUppercase = true;

        controller.displayTitle(context);

        verify(titleViewManager).renderUpperCaseTitleView();
    }

    @Test
    public void if_up_is_enabled_then_setDisplayHomeAsUpEnabled_true() {
        Menu menu = mock(Menu.class);
        ActionBar supportActionBar = mock(ActionBar.class);
        when(context.getSupportActionBar()).thenReturn(supportActionBar);
        controller.isUpEnabled = true;

        controller.inflateActionBarMenu(context, menu);

        verify(supportActionBar).setDisplayHomeAsUpEnabled(true);
    }

    @Test
    public void if_up_is_not_enabled_then_setDisplayHomeAsUpEnabled_false() {
        Menu menu = mock(Menu.class);
        ActionBar supportActionBar = mock(ActionBar.class);
        when(context.getSupportActionBar()).thenReturn(supportActionBar);
        controller.isUpEnabled = false;

        controller.inflateActionBarMenu(context, menu);

        verify(supportActionBar).setDisplayHomeAsUpEnabled(false);
    }


    @Test
    public void if_up_is_null_then_do_nothing() {
        Menu menu = mock(Menu.class);
        ActionBar supportActionBar = mock(ActionBar.class);
        controller.isUpEnabled = null;

        controller.inflateActionBarMenu(context, menu);

        verify(supportActionBar, never()).setDisplayHomeAsUpEnabled(anyBoolean());
    }


    @Test
    public void if_optionMenuLayout_has_any_value_inflate_it() {
        Menu menu = mock(Menu.class);
        MenuInflater menuInflater = mock(MenuInflater.class);
        when(context.getMenuInflater()).thenReturn(menuInflater);
        controller.optionMenuLayout = R.menu.actionbar_dark_close_menu;

        controller.inflateActionBarMenu(context, menu);

        verify(menuInflater).inflate(R.menu.actionbar_dark_close_menu, menu);
    }

    @Test
    public void if_optionMenuLayout_is_null_then_do_nothing() {
        Menu menu = mock(Menu.class);
        MenuInflater menuInflater = mock(MenuInflater.class);
        controller.optionMenuLayout = null;

        controller.inflateActionBarMenu(context, menu);

        verify(menuInflater, never()).inflate(R.menu.actionbar_dark_close_menu, menu);
    }

    @Test
    public void if_optionMenuLayout_is_null_then_onMenuItemClicked_do_nothing() {
        MenuItem item = mock(MenuItem.class);
        MenuItemClickListener menuItemClickListener = mock(MenuItemClickListener.class);
        controller.setMenuItemClickListener(menuItemClickListener);
        controller.optionMenuLayout = null;

        boolean itemClicked = controller.onMenuItemClicked(item);

        assertFalse(itemClicked);
        verify(menuItemClickListener, never()).onCloseClicked();
        verify(menuItemClickListener, never()).onSkipClicked();
    }

    @Test
    public void if_action_skip_btn_clicked_then_call_menuItemClickListener_onSkipClicked() {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.action_skip_btn);
        MenuItemClickListener menuItemClickListener = mock(MenuItemClickListener.class);
        controller.setMenuItemClickListener(menuItemClickListener);
        controller.optionMenuLayout = R.menu.actionbar_light_skip_menu;

        boolean itemClicked = controller.onMenuItemClicked(item);

        verify(menuItemClickListener).onSkipClicked();
        assertTrue(itemClicked);
        verify(menuItemClickListener, never()).onCloseClicked();
    }

    @Test
    public void if_action_close_btn_clicked_then_call_menuItemClickListener_onSkipClicked() {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.action_close_btn);
        MenuItemClickListener menuItemClickListener = mock(MenuItemClickListener.class);
        controller.setMenuItemClickListener(menuItemClickListener);
        controller.optionMenuLayout = R.menu.actionbar_dark_close_menu;

        boolean itemClicked = controller.onMenuItemClicked(item);

        verify(menuItemClickListener).onCloseClicked();
        assertTrue(itemClicked);
        verify(menuItemClickListener, never()).onSkipClicked();
    }

    @Test
    public void if_a_menu_item_was_clicked_but_is_unknown_the_return_false() {
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(-1);
        MenuItemClickListener menuItemClickListener = mock(MenuItemClickListener.class);
        controller.setMenuItemClickListener(menuItemClickListener);
        controller.optionMenuLayout = R.menu.actionbar_dark_close_menu;

        boolean itemClicked = controller.onMenuItemClicked(item);

        assertFalse(itemClicked);
        verify(menuItemClickListener, never()).onCloseClicked();
        verify(menuItemClickListener, never()).onSkipClicked();
    }

    @Test
    public void if_isActionBarGone_false_then_when_updateTitle_then_call_titleViewManager_renderTitleView() {
        controller.isActionBarGone = false;

        controller.updateTitle("--- some new title");

        verify(titleViewManager).renderTitleView("--- some new title");
    }


    @Test
    public void if_isActionBarGone_true_then_do_nothing_when_updateTitle() {
        controller.isActionBarGone = true;

        controller.updateTitle("--- some new title");

        verify(titleViewManager, never()).renderTitleView(anyString());
    }


    @Test
    public void if_isActionBarGone_null_then_do_nothing_when_updateTitle() {
        controller.isActionBarGone = true;

        controller.updateTitle("--- some new title");

        verify(titleViewManager, never()).renderTitleView(anyString());
    }

}