package com.coinninja.coinkeeper.util.android;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.coinninja.coinkeeper.util.DropbitIntents;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowLocationManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.runner.AndroidJUnit4;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class LocationUtilTest {

    private ShadowLocationManager shadowLocationManager;
    private LocationUtil locationUtil;
    @Mock
    private PermissionsUtil permissionsUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Context applicationContext = ApplicationProvider.getApplicationContext();
        LocationManager locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        locationUtil = new LocationUtil(locationManager, permissionsUtil);
        when(permissionsUtil.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(true);
        when(permissionsUtil.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)).thenReturn(true);
    }

    @Test
    public void checks_permission() {
        assertTrue(locationUtil.canReadLocation());
    }

    @Test
    public void provides_access_to_last_known_location() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(87d);
        location.setLongitude(11d);
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, location);

        assertThat(locationUtil.getLastKnownLocation(), equalTo(location));
    }

    @Test
    public void provides_empty_location_when_no_permission_granted() {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(87d);
        location.setLongitude(11d);
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, location);
        when(permissionsUtil.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(false);
        Location unknown = new Location(LocationManager.GPS_PROVIDER);

        Location lastKnownLocation = locationUtil.getLastKnownLocation();
        assertThat(lastKnownLocation.getLatitude(), equalTo(0d));
        assertThat(lastKnownLocation.getLongitude(), equalTo(0d));
    }

    @Test
    public void conducts_permission_check_for_location() {
        AppCompatActivity activity = mock(AppCompatActivity.class);
        String[] permissions = new String[2];
        permissions[0] = Manifest.permission.ACCESS_COARSE_LOCATION;
        permissions[1] = Manifest.permission.ACCESS_FINE_LOCATION;

        locationUtil.requestPermissionToAccessLocationFor(activity);

        verify(permissionsUtil).requestPermissions(activity, permissions, DropbitIntents.REQUEST_PERMISSIONS_LOCATION);
    }


}