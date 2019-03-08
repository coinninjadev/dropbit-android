package com.coinninja.coinkeeper.ui.base;

import android.content.Intent;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.CalculatorActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BaseActivityTest {

    @Mock
    private ActionBarController actionBarController;

    @Mock
    private DrawerController drawerController;


    @Mock
    CNWalletManager cnWalletManager;


    private BaseActivity activity;

    private ActivityController<SettingsActivity> activityController;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    @After
    public void tearDown() {
        actionBarController = null;
    }

    private void setupWithTheme(int theme) {
        activityController = Robolectric.buildActivity(SettingsActivity.class);
        activity = activityController.get();
        TestCoinKeeperApplication application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.typedValue = mock(TypedValue.class);
        activity.setTheme(theme);
        activityController.create().resume().start().visible();
        activity.drawerController = drawerController;
        activity.cnWalletManager = cnWalletManager;
        activity.actionBarController = actionBarController;
    }

    @Test
    public void during_setContentView_configure_action_bar_controller() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);

        activity.setContentView(R.layout.activty_calculator);

        verify(actionBarController).setTheme(activity, activity.actionBarType);
    }


    @Test
    public void during_setContentView_set_the_resolveAttribute_on_injected_TypedValue() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        TypedValue actionBarType = activity.actionBarType;

        activity.setContentView(R.layout.activity_splash);

        assertThat(actionBarType.resourceId, equalTo(R.id.actionbar_gone));
    }

    @Test
    public void during_setContentView_display_title() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);

        activity.setContentView(R.layout.activity_splash);

        verify(actionBarController).displayTitle(activity);
    }

    @Test
    public void during_setContentView_inflate_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        ActionBarController actionBarController = mock(ActionBarController.class);


        activity.setContentView(R.layout.activity_splash);

        verify(drawerController).inflateDrawer(activity, activity.actionBarType);
    }

    @Test
    public void during_onResume_if_hasSkippedBackup_then_showBackupNowDrawerActions() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);

        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);


        activity.onResume();

        verify(drawerController).showBackupNowDrawerActions();
    }

    @Test
    public void during_onCreateOptionsMenu_inflate_menu() {
        Menu menu = mock(Menu.class);
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);

        activity.onCreateOptionsMenu(menu);

        verify(actionBarController).inflateActionBarMenu(activity, menu);
    }

    @Test
    public void when_onOptionsItemSelected_call_onMenuItemClicked_and_return_boolean() {
        MenuItem item = mock(MenuItem.class);
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        when(actionBarController.onMenuItemClicked(item)).thenReturn(true);

        boolean itemSelected = activity.onOptionsItemSelected(item);

        verify(actionBarController).onMenuItemClicked(item);
        assertTrue(itemSelected);
    }

    @Test
    public void when_onOptionsItemSelected_call_drawerController_onMenuItemClicked_and_return_boolean() {
        MenuItem item = mock(MenuItem.class);
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        when(actionBarController.onMenuItemClicked(item)).thenReturn(false);
        when(drawerController.onMenuItemClicked(item)).thenReturn(true);

        boolean itemSelected = activity.onOptionsItemSelected(item);

        verify(drawerController).onMenuItemClicked(item);
        assertTrue(itemSelected);
    }

    @Test
    public void during_onBackPressed_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        when(drawerController.isDrawerOpen()).thenReturn(true);

        activity.onBackPressed();

        verify(drawerController).closeDrawer();
    }

    @Test
    public void during_onPause_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        when(drawerController.isDrawerOpen()).thenReturn(true);

        activity.onPause();

        verify(drawerController).closeDrawerNoAnimation();
    }

    @Test
    public void during_onPriceReceived_updatePriceOfBtcDisplay_of_drawer() {
        USDCurrency price = new USDCurrency("500");
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);

        activity.onPriceReceived(price);

        verify(drawerController).updatePriceOfBtcDisplay(price);
    }


    @Test
    public void if_onOptionsItemSelected_returns_false_then_call_super() {
        MenuItem item = mock(MenuItem.class);
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        when(actionBarController.onMenuItemClicked(item)).thenReturn(false);

        boolean itemSelected = activity.onOptionsItemSelected(item);

        verify(actionBarController).onMenuItemClicked(item);
        assertFalse(itemSelected);
    }

    @Test
    public void during_onCreateOptionsMenu_set_menu_item_click_listener() {
        Menu menu = mock(Menu.class);
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        ActionBarController actionBarController = mock(ActionBarController.class);
        activity.actionBarController = actionBarController;

        activity.onCreateOptionsMenu(menu);

        verify(actionBarController).setMenuItemClickListener(activity);
    }


    @Test
    public void on_close_action_clicked_start_calculator_activity() throws Exception {
        setupWithTheme(R.style.CoinKeeperTheme_LightActionBar);

        activity.onCloseClicked();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(CalculatorActivity.class.getName()));
    }

    @Test
    public void navigate_to_calculator_activity_when_close_button_is_clicked() {
        setupWithTheme(R.style.CoinKeeperTheme_LightActionBar);
        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.resetIsFinishing();

        activity.onCloseClicked();

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(CalculatorActivity.class.getName()));
        assertThat(intent.getFlags(), equalTo(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }


    @Test
    public void addsTabsWhenRequested() throws Exception {
        setupWithTheme(R.style.CoinKeeperTheme_LightActionBar);
        activity.addTabbar(R.layout.tabbar_activity_calculator);
        assertNotNull(activity.findViewById(R.id.id_navigation_tabs));
    }


    @Test
    public void updateActivityLabel() {
        setupWithTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff);
        activity.actionBarController = actionBarController;

        activity.updateActivityLabel("-- some text");

        verify(actionBarController).updateTitle("-- some text");
    }
}