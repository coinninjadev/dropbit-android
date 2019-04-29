package com.coinninja.coinkeeper.util.android;

import android.Manifest;
import android.content.pm.PackageManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.appcompat.app.AppCompatActivity;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PermissionsUtilTest {

    private PermissionsUtil permissionsUtil;
    private final AppCompatActivity context = mock(AppCompatActivity.class);

    @Before
    public void setup() {
        permissionsUtil = new PermissionsUtil(context);
    }

    @Test
    public void ask_for_permission_test() {
        when(context.checkPermission(eq(Manifest.permission.READ_CONTACTS), anyInt(), anyInt())).thenReturn(
                PackageManager.PERMISSION_DENIED);

        permissionsUtil.requestPermissions(context, new String[]{Manifest.permission.READ_CONTACTS}, 1001);

        verify(context).requestPermissions(new String[]{"android.permission.READ_CONTACTS"},
                1001);
    }

    @Test
    public void already_have_permission_test() {
        when(context.checkPermission(eq(Manifest.permission.READ_CONTACTS), anyInt(), anyInt())).thenReturn(
                PackageManager.PERMISSION_GRANTED);

        boolean hasPermissions = permissionsUtil.hasPermission(Manifest.permission.READ_CONTACTS);

        assertTrue(hasPermissions);
    }

}