package com.coinninja.coinkeeper.ui.base;

import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.YearlyHighViewModel;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.ui.actionbar.ActionBarController;
import com.coinninja.coinkeeper.ui.actionbar.managers.DrawerController;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static junit.framework.Assert.assertFalse;
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
    private CNWalletManager cnWalletManager;
    @Mock
    private ActionBarController actionBarController;
    @Mock
    private DrawerController drawerController;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    private BaseActivity activity;
    private ActivityController<SettingsActivity> activityController;
    private TestCoinKeeperApplication application;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        application = ApplicationProvider.getApplicationContext();
        application.actionBarController = actionBarController;
        application.drawerController = drawerController;
        application.activityNavigationUtil = activityNavigationUtil;
        application.cnWalletManager = cnWalletManager;
        activityController = Robolectric.buildActivity(SettingsActivity.class);
        application.yearlyHighViewModel = mock(YearlyHighViewModel.class);
        MutableLiveData<Boolean> liveData = mock(MutableLiveData.class);
        when(application.yearlyHighViewModel.isSubscribedToYearlyHigh()).thenReturn(liveData);
    }

    @After
    public void tearDown() {
        actionBarController = null;
        cnWalletManager = null;
        activityNavigationUtil = null;
        activity = null;
        activityController = null;
        application = null;
    }

    @Test
    public void during_setContentView_configure_action_bar_controller() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);

        verify(actionBarController).setTheme(activity, activity.actionBarType);
    }

    @Test
    public void during_setContentView_set_the_resolveAttribute_on_injected_TypedValue() {
        setupWithTheme(R.style.CoinKeeperTheme_NoActionBar_BlockChain);
        TypedValue actionBarType = activity.actionBarType;

        assertThat(actionBarType.resourceId, equalTo(R.id.actionbar_gone));
    }

    @Test
    public void during_setContentView_display_title() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);

        verify(actionBarController).displayTitle(activity);
    }

    @Test
    public void during_onResume_if_hasSkippedBackup_then_showBackupNowDrawerActions() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);

        verify(drawerController).showBackupNowDrawerActions();
    }

    @Test
    public void during_onCreateOptionsMenu_inflate_menu() {
        Menu menu = mock(Menu.class);
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);

        activity.onCreateOptionsMenu(menu);

        verify(actionBarController).inflateActionBarMenu(activity, menu);
    }

    @Test
    public void when_onOptionsItemSelected_call_onMenuItemClicked_and_return_boolean() {
        MenuItem item = mock(MenuItem.class);
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        when(actionBarController.onMenuItemClicked(item)).thenReturn(true);

        boolean itemSelected = activity.onOptionsItemSelected(item);

        verify(actionBarController).onMenuItemClicked(item);
        assertTrue(itemSelected);
    }

    @Test
    public void when_onOptionsItemSelected_call_drawerController_onMenuItemClicked_and_return_boolean() {
        MenuItem item = mock(MenuItem.class);
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        when(actionBarController.onMenuItemClicked(item)).thenReturn(false);
        when(drawerController.onMenuItemClicked(item)).thenReturn(true);

        boolean itemSelected = activity.onOptionsItemSelected(item);

        verify(drawerController).onMenuItemClicked(item);
        assertTrue(itemSelected);
    }

    @Test
    public void during_onBackPressed_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        when(drawerController.isDrawerOpen()).thenReturn(true);

        activity.onBackPressed();

        verify(drawerController).closeDrawer();
    }

    @Test
    public void during_onPause_close_drawer() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        when(drawerController.isDrawerOpen()).thenReturn(true);

        activity.onPause();

        verify(drawerController).closeDrawerNoAnimation();
    }

    @Test
    public void during_onPriceReceived_updatePriceOfBtcDisplay_of_drawer() {
        USDCurrency price = new USDCurrency("500");
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);

        activity.onPriceReceived(price);

        verify(drawerController).updatePriceOfBtcDisplay(price);
    }

    @Test
    public void if_onOptionsItemSelected_returns_false_then_call_super() {
        MenuItem item = mock(MenuItem.class);
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        when(actionBarController.onMenuItemClicked(item)).thenReturn(false);

        boolean itemSelected = activity.onOptionsItemSelected(item);

        verify(actionBarController).onMenuItemClicked(item);
        assertFalse(itemSelected);
    }

    @Test
    public void during_onCreateOptionsMenu_set_menu_item_click_listener() {
        Menu menu = mock(Menu.class);
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        ActionBarController actionBarController = mock(ActionBarController.class);
        activity.actionBarController = actionBarController;

        activity.onCreateOptionsMenu(menu);

        verify(actionBarController).setMenuItemClickListener(activity);
    }

    @Test
    public void on_close_action_clicked_start_home_activity() {
        setupWithTheme(R.style.CoinKeeperTheme);

        activity.onCloseClicked();

        verify(activityNavigationUtil).navigateToHome(activity);
    }

    @Test
    public void navigate_to_home_activity_when_close_button_is_clicked() {
        setupWithTheme(R.style.CoinKeeperTheme);
        ShadowActivity shadowActivity = shadowOf(activity);
        shadowActivity.resetIsFinishing();

        activity.onCloseClicked();

        verify(activityNavigationUtil).navigateToHome(activity);
    }

    @Test
    public void updateActivityLabel() {
        setupWithTheme(R.style.CoinKeeperTheme_UpOff);
        activity.actionBarController = actionBarController;

        activity.updateActivityLabel("-- some text");

        verify(actionBarController).updateTitle("-- some text");
    }

    private void setupWithTheme(int theme) {
        application.typedValue = mock(TypedValue.class);
        activity = activityController.get();
        activity.setTheme(theme);
        activityController.setup();
    }
}