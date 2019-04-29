package com.coinninja.coinkeeper.view.activity;

import android.Manifest;
import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;

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

import androidx.appcompat.app.AppCompatActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class QrScanActivityTest {
    private static final String EXPECTED_ADDRESS = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo";

    private QrScanActivity activity;
    private ActivityController<QrScanActivity> activityController;

    @Mock
    private PermissionsUtil permissionsUtil;
    @Mock
    private CNQRScanManager scanManager;

    @After
    public void tearDown() {
        activity = null;
        activityController = null;
        scanManager = null;
        permissionsUtil = null;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(QrScanActivity.class);
        activity = activityController.create().get();
        activity.qrScanManager = scanManager;
        activity.permissionsUtil = permissionsUtil;
    }

    private void startWithPermission(boolean hasPermission) {
        when(permissionsUtil.hasPermission(anyString())).thenReturn(hasPermission);

        activityController.start().resume();
    }

    @Test
    public void onScanResult() {
        startWithPermission(true);
        String expectedAmount = "0.005";
        String qrString = "bitcoin:" + EXPECTED_ADDRESS + "?amount=" + expectedAmount;

        activity.onScanComplete(qrString);

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getResultIntent();
        assertEquals(DropbitIntents.RESULT_SCAN_OK, shadowActivity.getResultCode());
        assertEquals(qrString, intent.getStringExtra(DropbitIntents.EXTRA_SCANNED_DATA));
    }


    @Test
    public void when_clicking_the_x_to_exit___set_result_code_to_canceled() {
        startWithPermission(true);
        int expectedResultCode = AppCompatActivity.RESULT_CANCELED;

        activity.onCloseClicked();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getResultIntent();
        assertThat(shadowActivity.getResultCode(), equalTo(expectedResultCode));
        assertEquals(null, intent);
    }

    @Test
    public void check_is_we_have_permission_to_the_camera_test() {
        startWithPermission(true);

        verify(permissionsUtil, times(2)).hasPermission(Manifest.permission.CAMERA);
    }

    @Test
    public void if_have_permission_to_the_camera_then_start_barcode_capture_test() {
        startWithPermission(true);

        verify(scanManager).startCapture();
    }

    @Test
    public void if_have_permission_to_the_camera_then_stop_barcode_capture_test() {
        startWithPermission(true);

        activityController.pause().stop();

        verify(scanManager).stopCapture();
    }

    @Test
    public void if_not_have_permission_to_the_camera_then_request_test() {
        startWithPermission(false);

        verify(permissionsUtil).requestPermissions(activity, new String[]{Manifest.permission.CAMERA},
                DropbitIntents.REQUEST_PERMISSIONS_CAMERA);
    }

    @Test
    public void captureTeardown() {
        startWithPermission(true);

        activityController.pause().destroy();

        verify(scanManager).onDestroy();
    }
}