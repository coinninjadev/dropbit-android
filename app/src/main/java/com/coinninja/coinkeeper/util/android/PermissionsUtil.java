package com.coinninja.coinkeeper.util.android;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsUtil {

    Context context;

    @Inject
    PermissionsUtil(@ApplicationContext Context context) {
        this.context = context;
    }

    public boolean hasPermission(@NonNull String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public void requestPermissions(@NonNull Activity activity, @NonNull String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public boolean hasGranted(String permission, String[] permissions, int[] grantResults) {
        boolean hasGranted = false;
        if (permission != null && !permission.isEmpty() && permissions.length == grantResults.length) {
            for (int i = 0; i < permissions.length; i++) {
                if (permission.equals(permissions[i]) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    hasGranted = true;
                }
            }
        }
        return hasGranted;
    }
}
