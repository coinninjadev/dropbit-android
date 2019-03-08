package com.coinninja.coinkeeper.util.android;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PermissionsUtilTest {

    private PermissionsUtil permissionsUtil;
    private final Activity context = mock(Activity.class);

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