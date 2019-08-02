package com.coinninja.coinkeeper.ui.spending;

import android.content.pm.PackageManager;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocationUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;

import java.util.HashMap;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(AndroidJUnit4.class)
public class SpendBitcoinActivityTest {
    @Mock
    private LocationUtil locationUtil;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    private ActivityController<SpendBitcoinActivity> activityController;
    private SpendBitcoinActivity activity;
    private HashMap<CoinNinjaParameter, String> parameters;
    private Location location;

    @Test
    public void clicking_on_where_to_buy_navigates_to_buy_gift_card() {
        clickOn(withId(activity, R.id.buy_giftcard_button));

        verify(activityNavigationUtil).navigateToBuyGiftCard(activity);
    }

    @Test
    public void clicking_on_where_to_buy_navigates_to_where_to_buy() {
        clickOn(withId(activity, R.id.online_button));

        verify(activityNavigationUtil).navigateToWhereToSpend(activity);
    }

    @Test
    public void navigates_to_map_when_spending_button_clicked_and_location_permission_already_granted() {
        when(locationUtil.canReadLocation()).thenReturn(true);
        when(locationUtil.getLastKnownLocation()).thenReturn(location);

        clickOn(withId(activity, R.id.around_me_button));

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, location, Analytics.Companion.EVENT_SPEND_AROUND_ME);
    }

    @Test
    public void requests_permission_when_spending_button_clicked() {
        when(locationUtil.canReadLocation()).thenReturn(false);

        clickOn(withId(activity, R.id.around_me_button));

        verify(locationUtil).requestPermissionToAccessLocationFor(activity);
    }

    @Test
    public void navigates_to_map_for_spending_with_no_location_when_permission_denied() {
        int[] grantResults = new int[1];
        grantResults[0] = PackageManager.PERMISSION_DENIED;
        when(locationUtil.hasGrantedPermission(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults)).thenReturn(false);

        activity.onRequestPermissionsResult(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults);

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, null, Analytics.Companion.EVENT_SPEND_AROUND_ME);
    }

    @Test
    public void navigates_to_map_for_spending_with_location_when_permission_granted() {
        int[] grantResults = new int[1];
        grantResults[0] = PackageManager.PERMISSION_GRANTED;
        when(locationUtil.hasGrantedPermission(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults)).thenReturn(true);

        activity.onRequestPermissionsResult(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults);

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, location, Analytics.Companion.EVENT_SPEND_AROUND_ME);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configureDI();
        location = mock(Location.class);
        when(locationUtil.canReadLocation()).thenReturn(true);
        when(locationUtil.getLastKnownLocation()).thenReturn(location);
        activityController = Robolectric.buildActivity(SpendBitcoinActivity.class).setup();
        activity = activityController.get();
        parameters = new HashMap<>();
        parameters.put(CoinNinjaParameter.TYPE, "spend");
    }

    @After
    public void tearDown() {
        activityController = null;
        activity = null;
        activityNavigationUtil = null;
        locationUtil = null;
    }

    private void configureDI() {
        TestCoinKeeperApplication testCoinKeeperApplication = ApplicationProvider.getApplicationContext();
        testCoinKeeperApplication.activityNavigationUtil = activityNavigationUtil;
        testCoinKeeperApplication.locationUtil = locationUtil;
    }
}