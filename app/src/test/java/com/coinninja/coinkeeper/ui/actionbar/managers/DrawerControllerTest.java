package com.coinninja.coinkeeper.ui.actionbar.managers;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.util.ui.BadgeRenderer;
import com.coinninja.coinkeeper.view.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class DrawerControllerTest {

    @Mock
    private BadgeRenderer badgeRenderer;

    @Mock
    private ActivityNavigationUtil navigationUtil;

    @Mock
    private DropbitAccountHelper dropbitAccountHelper;

    private AppCompatActivity activity;

    private DrawerController drawerController;

    private TypedValue actionbarType = new TypedValue();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        drawerController = new DrawerController(badgeRenderer, navigationUtil, "1.1.1", dropbitAccountHelper);
        activity = Robolectric.setupActivity(A.class);
        actionbarType.resourceId = R.id.actionbar_up_on_with_nav_bar;
    }

    @After
    public void tearDown() throws Exception {
        drawerController = null;
        badgeRenderer = null;
        activity = null;
        navigationUtil = null;
        dropbitAccountHelper = null;
        actionbarType = null;
    }

    @Test
    public void adds_drawer_as_root_view() {
        drawerController.inflateDrawer(activity, actionbarType);

        assertNotNull(withId(activity, R.id.drawer_action_view));
    }

    @Test
    public void does_not_add_drawer_as_root_view__when_not_drawer_theme() {
        actionbarType.resourceId = R.id.actionbar_up_on;

        drawerController.inflateDrawer(activity, actionbarType);

        assertNull(withId(activity, R.id.drawer_action_view));
    }

    @Test
    public void when_inflating_drawer_setup_nav_view() {
        drawerController.inflateDrawer(activity, actionbarType);

        NavigationView navigationView = withId(activity, R.id.drawer_action_view);

        assertNull(navigationView.getItemIconTintList());
    }

    @Test
    public void when_inflating_drawer_set_on_click_listener_for_drawer_setting_button() {
        drawerController.inflateDrawer(activity, actionbarType);

        drawerController.openDrawer();
        withId(activity, R.id.drawer_setting).performClick();

        verify(navigationUtil).navigateToSettings(activity);
    }

    @Test
    public void when_inflating_drawer_set_on_click_listener_for_drawer_support_button() {
        drawerController.inflateDrawer(activity, actionbarType);

        withId(activity, R.id.drawer_support).performClick();

        verify(navigationUtil).navigateToSupport(activity);
    }

    @Test
    public void open_drawer() {
        drawerController.drawerLayout = mock(DrawerLayout.class);

        drawerController.openDrawer();

        verify(drawerController.drawerLayout).openDrawer(GravityCompat.START);

    }

    @Test
    public void close_drawer() {
        drawerController.drawerLayout = mock(DrawerLayout.class);

        drawerController.closeDrawer();

        verify(drawerController.drawerLayout).closeDrawer(GravityCompat.START);
    }

    @Test
    public void handles_null_drawer___open__close() {
        drawerController.drawerLayout = null;

        // fails if null pointer thrown
        drawerController.openDrawer();
        drawerController.closeDrawer();
        drawerController.closeDrawerNoAnimation();
        drawerController.updatePriceOfBtcDisplay(new USDCurrency("500.00"));
    }

    @Test
    public void close_drawer_no_animation() {
        drawerController.drawerLayout = mock(DrawerLayout.class);

        drawerController.closeDrawerNoAnimation();

        verify(drawerController.drawerLayout, never()).closeDrawer(GravityCompat.START);
        verify(drawerController.drawerLayout).closeDrawer(GravityCompat.START, false);
    }


    @Test
    public void return_true_when_drawer_is_open() {
        drawerController.drawerLayout = mock(DrawerLayout.class);

        when(drawerController.drawerLayout.isDrawerOpen(GravityCompat.START)).thenReturn(true);
        assertTrue(drawerController.isDrawerOpen());

        when(drawerController.drawerLayout.isDrawerOpen(GravityCompat.START)).thenReturn(false);
        assertFalse(drawerController.isDrawerOpen());

        drawerController.drawerLayout = null;
        assertFalse(drawerController.isDrawerOpen());
    }

    @Test
    public void update_price() {
        USDCurrency price = new USDCurrency("500");
        drawerController.inflateDrawer(activity, actionbarType);
        drawerController.updatePriceOfBtcDisplay(price);

        TextView priceView = withId(activity, R.id.drawer_action_price_text);

        assertThat(priceView, hasText(price.toFormattedCurrency()));
    }

    @Test
    public void display_app_version() {
        drawerController.inflateDrawer(activity, actionbarType);

        TextView version = withId(drawerController.drawerLayout, R.id.drawer_action_footer_version);

        assertThat(version, hasText("Version 1.1.1"));
    }

    @Test
    public void display_app_version_protect_ageist_non_inflation() {
        actionbarType.resourceId = R.id.actionbar_up_on;

        drawerController.inflateDrawer(activity, actionbarType);

        assertNull(withId(activity, R.id.drawer_action_footer_version));
    }

    @Test
    public void show_back_up_now_drawer_action() {
        drawerController.inflateDrawer(activity, actionbarType);

        drawerController.showBackupNowDrawerActions();

        Toolbar toolbar = withId(activity, R.id.toolbar);
        ImageView settings = withId(activity, R.id.setting_icon);

        verify(badgeRenderer).renderBadge(toolbar);
        verify(badgeRenderer).renderBadge(settings);
        assertThat(withId(activity, R.id.drawer_backup_now), isVisible());
    }

    @Test
    public void show_badge_if_phone_is_not_verified() {
        drawerController.inflateDrawer(activity, actionbarType);

        drawerController.renderBadgeForUnverifiedDeviceIfNecessary();

        ImageView phoneImage = withId(activity, R.id.contact_phone);
        Toolbar toolbar = withId(activity, R.id.toolbar);

        verify(badgeRenderer).renderBadge(phoneImage);
        verify(badgeRenderer).renderBadge(toolbar);
    }

    @Test
    public void show_back_up_now_drawer_action_set_on_click_listener_for() {
        drawerController.inflateDrawer(activity, actionbarType);
        drawerController.showBackupNowDrawerActions();

        withId(activity, R.id.drawer_backup_now).performClick();

        verify(navigationUtil).navigateToBackupRecoveryWords(activity);
    }

    @Test
    public void menu_item_clicked_for_user_verification_requests_dropbit_me_view() {
        drawerController.inflateDrawer(activity, actionbarType);
        drawerController.drawerLayout = mock(DrawerLayout.class);

        withId(activity, R.id.drawer_phone).performClick();

        verify(navigationUtil).navigateToUserVerification(activity);
    }

    @Test
    public void menu_item_clicked_id_is_android_home_then_open_drawer() {
        MenuItem menuItem = mock(MenuItem.class);
        when(menuItem.getItemId()).thenReturn(android.R.id.home);
        drawerController.inflateDrawer(activity, actionbarType);
        drawerController.drawerLayout = mock(DrawerLayout.class);

        assertTrue(drawerController.onMenuItemClicked(menuItem));
        verify(drawerController.drawerLayout).openDrawer(GravityCompat.START);
    }

    @Test
    public void menu_item_clicked_protect_ageist_non_inflation() {
        MenuItem menuItem = mock(MenuItem.class);
        when(menuItem.getItemId()).thenReturn(android.R.id.home);
        actionbarType.resourceId = R.id.actionbar_up_on;

        drawerController.inflateDrawer(activity, actionbarType);

        assertFalse(drawerController.onMenuItemClicked(menuItem));
    }

    @Test
    public void menu_item_clicked_id_is_not_android_home_then_do_nothing() {
        MenuItem menuItem = mock(MenuItem.class);
        when(menuItem.getItemId()).thenReturn(R.id.action_close_btn);

        drawerController.inflateDrawer(activity, actionbarType);

        assertFalse(drawerController.onMenuItemClicked(menuItem));
    }


    public static class A extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.cn_base_layout);
        }
    }
}