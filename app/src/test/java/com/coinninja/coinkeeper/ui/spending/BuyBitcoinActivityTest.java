package com.coinninja.coinkeeper.ui.spending;

import android.content.pm.PackageManager;
import android.location.Location;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.Intents;
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

import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class BuyBitcoinActivityTest {
    @Mock
    private LocationUtil locationUtil;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    private ActivityController<BuyBitcoinActivity> activityController;
    private BuyBitcoinActivity activity;
    private HashMap<CoinNinjaParameter, String> parameters;
    @Mock
    private Location location;

    @Test
    public void forwards_buy_with_credit_card() {
        clickOn(withId(activity, R.id.buy_with_credit_card));

        verify(activityNavigationUtil).navigateToBuyBitcoinWithCreditCard(activity);
    }

    @Test
    public void forwards_buy_with_gift_card() {
        clickOn(withId(activity, R.id.buy_with_gift_card));

        verify(activityNavigationUtil).navigateToBuyBitcoinWithGiftCard(activity);
    }

    @Test
    public void forwards_find_atm_on_click__when_permission_to_access_location() {
        when(locationUtil.canReadLocation()).thenReturn(true);

        clickOn(withId(activity, R.id.buy_at_atm));

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, location, Analytics.EVENT_BUY_BITCOIN_AT_ATM);
    }

    @Test
    public void requests_location_permission_on_atm_click__when_no_permission_to_access_location() {
        when(locationUtil.canReadLocation()).thenReturn(false);

        clickOn(withId(activity, R.id.buy_at_atm));

        verify(locationUtil).requestPermissionToAccessLocationFor(activity);
    }

    @Test
    public void navigates_to_map_for_spending_with_no_location_when_permission_denied() {
        int[] grantResults = new int[1];
        grantResults[0] = PackageManager.PERMISSION_DENIED;
        when(locationUtil.hasGrantedPermission(Intents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults)).thenReturn(false);

        activity.onRequestPermissionsResult(Intents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults);

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, null, Analytics.EVENT_BUY_BITCOIN_AT_ATM);
    }

    @Test
    public void navigates_to_map_for_spending_with_location_when_permission_granted() {
        int[] grantResults = new int[1];
        grantResults[0] = PackageManager.PERMISSION_GRANTED;
        when(locationUtil.hasGrantedPermission(Intents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults)).thenReturn(true);

        activity.onRequestPermissionsResult(Intents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults);

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, location, Analytics.EVENT_BUY_BITCOIN_AT_ATM);
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configureDI();
        activityController = Robolectric.buildActivity(BuyBitcoinActivity.class).setup();
        activity = activityController.get();
        when(locationUtil.canReadLocation()).thenReturn(true);
        when(locationUtil.getLastKnownLocation()).thenReturn(location);
        parameters = new HashMap<>();
        parameters.put(CoinNinjaParameter.TYPE, "atms");
    }

    @After
    public void tearDown() {
        activityController = null;
        activity = null;
        activityNavigationUtil = null;
        locationUtil = null;
        location = null;
    }

    private void configureDI() {
        TestCoinKeeperApplication testCoinKeeperApplication = ApplicationProvider.getApplicationContext();
        testCoinKeeperApplication.activityNavigationUtil = activityNavigationUtil;
        testCoinKeeperApplication.locationUtil = locationUtil;
    }
}