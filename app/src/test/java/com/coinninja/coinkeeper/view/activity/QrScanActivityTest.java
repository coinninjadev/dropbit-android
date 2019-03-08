package com.coinninja.coinkeeper.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.qrscanner.QRScanManager;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class QrScanActivityTest {
    public static final String EXPECTED_ADDRESS = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo";
    private QrScanActivity activity;
    private ActivityController<QrScanActivity> activityController;
    private QRScanManager mockScanManager;
    private TestCoinKeeperApplication application;

    @After
    public void tearDown() {
        activity = null;
        activityController = null;
        mockScanManager = null;
        application = null;
    }

    @Before
    public void setUp() throws Exception {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        mockScanManager = application.getScanManager(null, null, null);
        activityController = Robolectric.buildActivity(QrScanActivity.class);

        activity = activityController.get();
        activityController.create().resume().start();
    }

    @Test
    public void onScanResult() {
        String expectedAmount = "0.005";
        String qrString = "bitcoin:" + EXPECTED_ADDRESS + "?amount=" + expectedAmount;

        activity.onScanComplete(qrString);

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getResultIntent();
        assertEquals(Intents.RESULT_SCAN_OK, shadowActivity.getResultCode());
        assertEquals(qrString, intent.getStringExtra(Intents.EXTRA_SCANNED_DATA));
    }


    @Test
    public void when_clicking_the_x_to_exit___set_result_code_to_canceled() {
        int expectedResultCode = Activity.RESULT_CANCELED;

        activity.onCloseClicked();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getResultIntent();
        assertThat(shadowActivity.getResultCode(), equalTo(expectedResultCode));
        assertEquals(null, intent);
    }

    @Test
    public void on_resume_check_is_we_have_permission_to_the_camera_test() {
        PermissionsUtil mockPermissionsUtil = mock(PermissionsUtil.class);
        activity.permissionsUtil = mockPermissionsUtil;

        activity.onResume();

        verify(mockPermissionsUtil).hasPermission(Manifest.permission.CAMERA);
    }

    @Test
    public void on_resume_if_have_permission_to_the_camera_then_start_barcode_capture_test() {
        PermissionsUtil mockPermissionsUtil = mock(PermissionsUtil.class);
        activity.permissionsUtil = mockPermissionsUtil;
        when(mockPermissionsUtil.hasPermission(anyString())).thenReturn(true);

        activity.onResume();

        verify(mockScanManager).startCapture();
    }

    @Test
    public void on_pause_if_have_permission_to_the_camera_then_stop_barcode_capture_test() {
        PermissionsUtil mockPermissionsUtil = mock(PermissionsUtil.class);
        activity.permissionsUtil = mockPermissionsUtil;
        when(mockPermissionsUtil.hasPermission(anyString())).thenReturn(true);

        activity.onPause();

        verify(mockScanManager).stopCapture();
    }

    @Test
    public void on_create_if_not_have_permission_to_the_camera_then_request_test() {
        PermissionsUtil mockPermissionsUtil = mock(PermissionsUtil.class);
        application.permissionsUtil = mockPermissionsUtil;
        when(mockPermissionsUtil.hasPermission(anyString())).thenReturn(false);

        activity = Robolectric.setupActivity(QrScanActivity.class);

        verify(mockPermissionsUtil).requestPermissions(activity, new String[]{Manifest.permission.CAMERA},
                Intents.REQUEST_PERMISSIONS_CAMERA);
    }

    @Test
    public void captureTeardown() {
        activityController.pause().destroy();

        verify(mockScanManager).onDestroy();
    }
}