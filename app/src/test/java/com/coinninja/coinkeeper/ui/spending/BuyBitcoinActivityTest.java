package com.coinninja.coinkeeper.ui.spending;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.UserPreferences;
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
import org.robolectric.shadows.ShadowDialog;

import java.util.HashMap;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class BuyBitcoinActivityTest {
    @Mock
    private Location location;
    @Mock
    private LocationUtil locationUtil;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;
    @Mock
    UserPreferences userPreferences;

    BuyBitcoinActivity activity;

    private HashMap<CoinNinjaParameter, String> parameters;
    private ActivityScenario<BuyBitcoinActivity> scenario;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        configureDI();
        when(locationUtil.canReadLocation()).thenReturn(true);
        when(locationUtil.getLastKnownLocation()).thenReturn(location);
        parameters = new HashMap<>();
        parameters.put(CoinNinjaParameter.TYPE, "atms");
        scenario = ActivityScenario.launch(BuyBitcoinActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
        scenario.onActivity(activity -> {
            this.activity = activity;
        });
    }

    private void configureDI() {
        TestCoinKeeperApplication testCoinKeeperApplication = ApplicationProvider.getApplicationContext();
        testCoinKeeperApplication.activityNavigationUtil = activityNavigationUtil;
        testCoinKeeperApplication.locationUtil = locationUtil;
        testCoinKeeperApplication.userPreferences = userPreferences;
    }

    @After
    public void tearDown() {
        activity = null;
        activityNavigationUtil = null;
        locationUtil = null;
        location = null;
        scenario.close();
    }

    @Test
    public void forwards_buy_with_credit_card() {
        clickOn(withId(activity, R.id.buy_with_credit_card));

        Dialog alertDialog = ShadowDialog.getLatestDialog();
        assert alertDialog != null;
        ShadowDialog shadowDialog = shadowOf(alertDialog);
        shadowDialog.clickOn(R.id.ok);

        verify(activityNavigationUtil).navigateToBuyBitcoinWithCreditCard(activity);
    }

    @Test
    public void forwards_buy_with_gift_card() {
        clickOn(withId(activity, R.id.buy_with_gift_card));

        Dialog alertDialog = ShadowDialog.getLatestDialog();
        assert alertDialog != null;
        ShadowDialog shadowDialog = shadowOf(alertDialog);
        shadowDialog.clickOn(R.id.ok);

        verify(activityNavigationUtil).navigateToBuyBitcoinWithGiftCard(activity);
    }

    @Test
    public void forwards_find_atm_on_click__when_permission_to_access_location() {
        when(locationUtil.canReadLocation()).thenReturn(true);

        clickOn(withId(activity, R.id.buy_at_atm));

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, location, Analytics.Companion.EVENT_BUY_BITCOIN_AT_ATM);
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
        when(locationUtil.hasGrantedPermission(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults)).thenReturn(false);

        activity.onRequestPermissionsResult(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults);

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, null, Analytics.Companion.EVENT_BUY_BITCOIN_AT_ATM);
    }

    @Test
    public void navigates_to_map_for_spending_with_location_when_permission_granted() {
        int[] grantResults = new int[1];
        grantResults[0] = PackageManager.PERMISSION_GRANTED;
        when(locationUtil.hasGrantedPermission(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults)).thenReturn(true);

        activity.onRequestPermissionsResult(DropbitIntents.REQUEST_PERMISSIONS_LOCATION, new String[0], grantResults);

        verify(activityNavigationUtil).navigatesToMapWith(activity, parameters, location, Analytics.Companion.EVENT_BUY_BITCOIN_AT_ATM);
    }

}