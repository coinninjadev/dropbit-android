package com.coinninja.coinkeeper.interactor;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Looper;

import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.view.activity.StartActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowFingerprintManager;
import org.robolectric.shadows.ShadowLooper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AuthenticationImplTest {

    @Mock
    Context context;
    @Mock
    PreferencesUtil preferencesUtil;
    @Mock
    SyncWalletManager syncWalletManager;
    @Mock
    Handler timeoutHandler;
    @Mock
    CoinKeeperLifecycleListener coinKeeperLifecycleListener;

    @InjectMocks
    AuthenticationImpl authenticationImpl;
    StartActivity activity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(preferencesUtil.getBoolean(UserPreferences.PREFERENCE_FINGERPRINT)).thenReturn(false);
    }

    @After
    public void tearDown() throws Exception {
        context = null;
        preferencesUtil = null;
        syncWalletManager = null;
        timeoutHandler = null;
        authenticationImpl = null;
        activity = null;
        coinKeeperLifecycleListener = null;
    }

    @Test
    public void registersAsReceiver(){
        verify(coinKeeperLifecycleListener).registerReceiver(authenticationImpl);
    }




    @Test
    public void deAuthentication_never_time_out() {
        boolean expectedInitAuthState = true;
        boolean expectedFinalAuthState = true;
        authenticationImpl.setAuthenticated();

        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedInitAuthState));

        authenticationImpl.startDeAuthentication();

        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedFinalAuthState));
    }

    @Test
    public void deAuthentication_timeout_test_wait_for_real_timeout() {
        boolean expectedInitAuthState = true;
        boolean expectedFinalAuthState = false;
        authenticationImpl.setAuthenticated();
        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedInitAuthState));

        timeoutHandler = new Handler(Looper.getMainLooper());
        authenticationImpl.timeOutHandler = timeoutHandler;

        authenticationImpl.startDeAuthentication();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedFinalAuthState));
        verify(syncWalletManager).schedule30SecondSync();
    }

    @Test
    public void sending_to_background_starts_deAuthentication() {
        boolean expectedInitAuthState = true;
        boolean expectedFinalAuthState = false;
        authenticationImpl.setAuthenticated();
        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedInitAuthState));

        timeoutHandler = new Handler(Looper.getMainLooper());
        authenticationImpl.timeOutHandler = timeoutHandler;

        authenticationImpl.onSentToBackground();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedFinalAuthState));
        verify(syncWalletManager).schedule30SecondSync();
    }

    @Test
    public void deAuthentication_verify_runnable_post() {
        long expectedTimeout = BuildConfig.AUTHENTICATION_TIME_OUT_MS;
        authenticationImpl.setAuthenticated();

        authenticationImpl.startDeAuthentication();

        verify(timeoutHandler).postDelayed(any(), eq(expectedTimeout));
    }

    @Test
    public void deAuthentication_verify_runnable_post_removed() {
        authenticationImpl.cancelDeAuthentication();

        verify(timeoutHandler).removeCallbacks(any());
    }

    @Test
    public void brought_to_foreground_cancels_deauthentication() {
        authenticationImpl.onBroughtToForeground();

        verify(timeoutHandler).removeCallbacks(any());
    }

    @Test
    public void forceDeAuthenticate() {
        boolean expectedInitAuthState = true;
        boolean expectedFinalAuthState = false;
        authenticationImpl.setAuthenticated();

        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedInitAuthState));

        authenticationImpl.forceDeAuthenticate();

        assertThat(authenticationImpl.isAuthenticated(), equalTo(expectedFinalAuthState));
        verify(syncWalletManager).cancel30SecondSync();
    }

    @Test
    public void setAuthenticated() {
        authenticationImpl.setAuthenticated();
        assertTrue(authenticationImpl.isAuthenticated());
        verify(syncWalletManager).schedule30SecondSync();
    }

    @Test
    @Config(sdk = 22)
    public void hasFingerpring_returnsFalse_API_notSupported() {
        assertFalse(authenticationImpl.hasOptedIntoFingerprintAuth());
    }

    public void setupHasFingerprintTests(boolean isHardwareDetected, boolean hasEnrolledFingerprints, boolean hasOptedIn) {
        activity = Robolectric.setupActivity(StartActivity.class);
        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
        ShadowFingerprintManager shadowFingerprintManager = shadowOf(fingerprintManager);
        shadowFingerprintManager.setIsHardwareDetected(isHardwareDetected);
        shadowFingerprintManager.setHasEnrolledFingerprints(hasEnrolledFingerprints);
        when(context.getSystemService(Context.FINGERPRINT_SERVICE)).thenReturn(fingerprintManager);
        when(preferencesUtil.getBoolean(UserPreferences.PREFERENCE_FINGERPRINT)).thenReturn(hasOptedIn);
    }

    @Test
    public void hasFingerpring_returnsTrue_API_support_withHardware_withUsersFingerprints_andOptedIn() {
        setupHasFingerprintTests(true, true, true);

        assertTrue(authenticationImpl.hasOptedIntoFingerprintAuth());
    }

    @Test
    public void hasFingerpring_returnsTrue_API_support_withHardware_withUsersFingerprints_andNotOptedIn() {
        setupHasFingerprintTests(true, true, false);

        assertFalse(authenticationImpl.hasOptedIntoFingerprintAuth());
    }

    @Test
    public void hasFingerpring_returnsFalse_API_supported_NoHardware() {
        setupHasFingerprintTests(false, false, false);

        assertFalse(authenticationImpl.hasOptedIntoFingerprintAuth());
    }

    @Test
    public void hasFingerpring_returnsFalse_API_supported_withHardware_noFingerprints() {
        setupHasFingerprintTests(true, false, false);

        assertFalse(authenticationImpl.hasOptedIntoFingerprintAuth());
    }
}