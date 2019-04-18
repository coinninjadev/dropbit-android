package com.coinninja.coinkeeper.util.android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;
import android.location.LocationManager;

import com.coinninja.coinkeeper.util.Intents;

import javax.inject.Inject;

public class LocationUtil {

    private final LocationManager locationManager;
    private final PermissionsUtil permissionsUtil;

    @Inject
    LocationUtil(LocationManager locationManager, PermissionsUtil permissionsUtil) {
        this.locationManager = locationManager;
        this.permissionsUtil = permissionsUtil;
    }

    @SuppressLint("MissingPermission")
    public Location getLastKnownLocation() {
        if (canReadLocation()) {
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return new Location(LocationManager.GPS_PROVIDER);
    }

    public boolean canReadLocation() {
        return permissionsUtil.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                permissionsUtil.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public void requestPermissionToAccessLocationFor(Activity activity) {
        permissionsUtil.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                Intents.REQUEST_PERMISSIONS_LOCATION);
    }

    public boolean hasGrantedPermission(int requestCode, String[] permissions, int[] grantResults) {
        return requestCode == Intents.REQUEST_PERMISSIONS_LOCATION &&
                permissionsUtil.hasGranted(Manifest.permission.ACCESS_COARSE_LOCATION, permissions, grantResults) &&
                permissionsUtil.hasGranted(Manifest.permission.ACCESS_FINE_LOCATION, permissions, grantResults);
    }
}
