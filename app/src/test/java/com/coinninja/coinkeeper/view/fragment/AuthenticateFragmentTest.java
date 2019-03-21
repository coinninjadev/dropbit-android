package com.coinninja.coinkeeper.view.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.AuthenticationImpl;
import com.coinninja.coinkeeper.presenter.fragment.AuthenticateFragmentPresenter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowFingerprintManager;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AuthenticateFragmentTest {

    private AuthenticateFragment fragment;

    @Mock
    AuthenticateFragmentPresenter authPresenter;


    private void setupFingerprint() {
        ShadowFingerprintManager manager = shadowOf((FingerprintManager) fragment.getContext().getSystemService(Context.FINGERPRINT_SERVICE));
        manager.setIsHardwareDetected(true);
        manager.setHasEnrolledFingerprints(true);
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        FragmentController<AuthenticateFragment> fragmentController = Robolectric.buildFragment(AuthenticateFragment.class);
        fragment = fragmentController.get();
        fragment.setAuthentication(mock(AuthenticationImpl.class));
        fragmentController.create();
        fragment.authPresenter = authPresenter;
        fragmentController.start().resume().visible();
        authPresenter = mock(AuthenticateFragmentPresenter.class);
    }

    @After
    public void tearDown() throws Exception {
        fragment = null;
        authPresenter = null;
    }

    @Test
    public void shows_fingerprint_auth_when_not_muted() {
        FragmentController<AuthenticateFragment> fragmentController = Robolectric.buildFragment(AuthenticateFragment.class);
        fragment = fragmentController.get();
        fragmentController.create();
        fragment.muteViews();
        fragment.authPresenter = authPresenter;
        fragmentController.start().resume().visible();

        fragment.teardownMute();

        verify(authPresenter).startAuth(anyBoolean());
    }

    @Test
    public void hides_fingerprint_auth_when_muted() {
        FragmentController<AuthenticateFragment> fragmentController = Robolectric.buildFragment(AuthenticateFragment.class);
        fragment = fragmentController.get();
        fragmentController.create();
        fragment.muteViews();
        fragment.authPresenter = authPresenter;
        fragmentController.start().resume().visible();

        verify(authPresenter, times(0)).startAuth(anyBoolean());
    }

    @Test
    public void recovers_views_from_unlocked_notification() {
        View view = fragment.getView();

        fragment.onWalletLock();
        fragment.onWalletLockRemoved();

        EditText input = view.findViewById(R.id.pin_entry_edittext);

        assertThat(input.isEnabled(), equalTo(true));
        assertThat(input.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void locks_screen_when_instructed() {
        View view = fragment.getView();

        fragment.onWalletLock();

        EditText input = view.findViewById(R.id.pin_entry_edittext);
        TextView error = view.findViewById(R.id.error_message);

        assertThat(input.isEnabled(), equalTo(false));
        assertThat(input.getVisibility(), equalTo(View.INVISIBLE));
        assertThat(error.getVisibility(), equalTo(View.INVISIBLE));
    }

    @Test
    public void fragmentInflatesViewOnCreateView() {
        assertNotNull(fragment.getView().findViewById(R.id.logo_fragment_pin));
    }

    @Test
    public void showFingerprintAuth() {
        fragment.showFingerprintAuth();

        View fingerprintBtn = fragment.getView().findViewById(R.id.finger_btn_fragment_pin);
        assertTrue(fingerprintBtn.isEnabled());
        assertThat(fingerprintBtn.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void showFingerprintAuthDialogWhenInstructedToAuthWithFingerprint() {
        setupFingerprint();

        fragment.authenticateWithFingerprint();

        assertNotNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void hideFingerprintButtonIfUserTrashesFingerprintsWhileAppIsRunning() {
        setupFingerprint();

        fragment.showFingerprintAuth();
        fragment.onFingerprintAuthenticationNotAvailable();

        View fingerprintBtn = fragment.getView().findViewById(R.id.finger_btn_fragment_pin);
        assertFalse(fingerprintBtn.isEnabled());
        assertThat(fingerprintBtn.getVisibility(), equalTo(View.INVISIBLE));
    }

    @Test
    public void onFingerprintAuthenticationCanceledDismissesDialog() {
        setupFingerprint();
        fragment.authenticateWithFingerprint();

        fragment.onFingerprintAuthenticationCanceled();

        assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void instructPresenterToHandleSuccessfulFingerprintAuth() throws NoSuchFieldException {
        setupFingerprint();
        fragment.authenticateWithFingerprint();
        AuthenticateFragmentPresenter mockPresenter = mock(AuthenticateFragmentPresenter.class);
        PrivateAccessor.setField(fragment, "authPresenter", mockPresenter);

        fragment.onFingerprintAuthenticationComplete();

        verify(mockPresenter, times(1)).onFingerprintAuthenticated();
        assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
        assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void showFingerprintAuthDialogWhenFingerprintButtonPressed() {
        setupFingerprint();

        fragment.showFingerprintAuth();
        fragment.getView().findViewById(R.id.finger_btn_fragment_pin).performClick();

        assertNotNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void onPinMismatch() throws Exception {

        fragment.onPinMismatch();
        Resources resources = fragment.getContext().getResources();
        TextView pinErrorDisplay = fragment.getView().findViewById(R.id.error_message);

        assertThat(pinErrorDisplay.getText(), equalTo(resources.getText(R.string.pin_mismatch_error)));
        assertThat(pinErrorDisplay.getVisibility(), equalTo(View.VISIBLE));

    }

    @Test
    public void canProvideTheFingerprintManager() {

        FingerprintManager fingerprintManager = (FingerprintManager) fragment.getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        assertThat(fragment.getFingerprintManager(), equalTo(fingerprintManager));
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void willReturnNullForFingerprintOnNonSupportedSDKs() {
        assertNull(fragment.getFingerprintManager());
    }

    @Test
    public void fragmentDismissesDialogWhenGoingIntoBackground() {

        setupFingerprint();

        fragment.authenticateWithFingerprint();
        fragment.onPause();

        assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void showDialogOnResumeToContinueFingerprintAuth() {
        setupFingerprint();
        fragment.authenticateWithFingerprint();

        fragment.onPause();
        fragment.onResume();

        assertNotNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }
}