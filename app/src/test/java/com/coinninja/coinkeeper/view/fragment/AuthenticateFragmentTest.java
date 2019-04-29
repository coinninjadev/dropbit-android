package com.coinninja.coinkeeper.view.fragment;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.fragment.AuthenticateFragmentPresenter;
import com.coinninja.coinkeeper.presenter.fragment.FingerprintAuthPresenter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowFingerprintManager;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AuthenticateFragmentTest {
    @Mock
    FingerprintAuthPresenter fingerprintAuthPresenter;
    @Mock
    FingerprintAuthDialog fingerPrintAuthDialog;
    @Mock
    AuthenticateFragmentPresenter authPresenter;
    @Mock
    AuthenticateFragment.OnUserHasAuthenticated onUserHasAuthenticated;
    private FragmentScenario<AuthenticateFragment> scenario;


    private void setupFingerprint() {
        scenario.onFragment(fragment -> {
            ShadowFingerprintManager manager = shadowOf((FingerprintManager) fragment.getContext().getSystemService(Context.FINGERPRINT_SERVICE));
            manager.setIsHardwareDetected(true);
            manager.setDefaultFingerprints(2);
        });
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        scenario = FragmentScenario.launchInContainer(AuthenticateFragment.class);
        scenario.onFragment(fragment -> {
            fragment.authPresenter = authPresenter;
            fragment.setOnUserHasAuthenticated(onUserHasAuthenticated);
            fragment.fingerprintAuthDialog = fingerPrintAuthDialog;
            fragment.fingerprintAuthPresenter = fingerprintAuthPresenter;
        });
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @After
    public void tearDown() {
        authPresenter = null;
        scenario.moveToState(Lifecycle.State.DESTROYED);
        scenario = null;
        onUserHasAuthenticated = null;
        fingerPrintAuthDialog = null;
        fingerprintAuthPresenter = null;
    }

    @Test
    public void shows_fingerprint_auth_when_not_muted() {
        scenario.onFragment(fragment -> {
            fragment.muteViews();
            assertTrue(fragment.isMuted());

            fragment.teardownMute();
        });

        verify(authPresenter).startAuth(anyBoolean());
    }

    @Test
    public void hides_fingerprint_auth_when_muted() {
        scenario.onFragment(fragment -> {
            fragment.muteViews();
        });

        verify(authPresenter, times(0)).startAuth(anyBoolean());
    }

    @Test
    public void recovers_views_from_unlocked_notification() {
        scenario.onFragment(fragment -> {
            View view = fragment.getView();
            fragment.onWalletLock();

            fragment.onWalletLockRemoved();

            EditText input = view.findViewById(R.id.pin_entry_edittext);
            assertThat(input.isEnabled(), equalTo(true));
            assertThat(input.getVisibility(), equalTo(View.VISIBLE));
        });
    }

    @Test
    public void locks_screen_when_instructed() {
        scenario.onFragment(fragment -> {
            View view = fragment.getView();

            fragment.onWalletLock();

            EditText input = view.findViewById(R.id.pin_entry_edittext);
            TextView error = view.findViewById(R.id.error_message);

            assertThat(input.isEnabled(), equalTo(false));
            assertThat(input.getVisibility(), equalTo(View.INVISIBLE));
            assertThat(error.getVisibility(), equalTo(View.INVISIBLE));
        });
    }

    @Test
    public void fragmentInflatesViewOnCreateView() {
        scenario.onFragment(fragment -> {
            assertNotNull(fragment.getView().findViewById(R.id.logo_fragment_pin));
        });
    }

    @Test
    public void showFingerprintAuth() {
        scenario.onFragment(fragment -> {
            fragment.showFingerprintAuth();

            View fingerprintBtn = fragment.getView().findViewById(R.id.finger_btn_fragment_pin);
            assertTrue(fingerprintBtn.isEnabled());
            assertThat(fingerprintBtn.getVisibility(), equalTo(View.VISIBLE));
        });
    }

    @Test
    public void showFingerprintAuthDialogWhenInstructedToAuthWithFingerprint() {
        setupFingerprint();
        scenario.onFragment(fragment -> {
            fragment.authenticateWithFingerprint();
        });

        verify(fingerprintAuthPresenter).captureFingerprintAuth();
    }

    @Test
    public void hideFingerprintButtonIfUserTrashesFingerprintsWhileAppIsRunning() {
        setupFingerprint();
        scenario.onFragment(fragment -> {
            fragment.showFingerprintAuth();
            fragment.onFingerprintAuthenticationNotAvailable();

            View fingerprintBtn = fragment.getView().findViewById(R.id.finger_btn_fragment_pin);
            assertFalse(fingerprintBtn.isEnabled());
            assertThat(fingerprintBtn.getVisibility(), equalTo(View.INVISIBLE));
        });

    }

    @Test
    public void onFingerprintAuthenticationCanceledDismissesDialog() {
        setupFingerprint();
        scenario.onFragment(fragment -> {
            fragment.authenticateWithFingerprint();

            fragment.onFingerprintAuthenticationCanceled();

            assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
        });
    }

    @Test
    public void instructPresenterToHandleSuccessfulFingerprintAuth() {
        setupFingerprint();
        scenario.onFragment(fragment -> {
            fragment.authenticateWithFingerprint();

            fragment.onFingerprintAuthenticationComplete();

            assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
            assertNull(fragment.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
        });
    }

    @Test
    public void showFingerprintAuthDialogWhenFingerprintButtonPressed() {
        setupFingerprint();
        scenario.onFragment(fragment -> {
            fragment.showFingerprintAuth();
            fragment.getView().findViewById(R.id.finger_btn_fragment_pin).performClick();
        });

        verify(fingerprintAuthPresenter).captureFingerprintAuth();
    }

    @Test
    public void onPinMismatch() {
        scenario.onFragment(fragment -> {
            fragment.onPinMismatch();
            Resources resources = fragment.getContext().getResources();
            TextView pinErrorDisplay = fragment.getView().findViewById(R.id.error_message);

            assertThat(pinErrorDisplay.getText(), equalTo(resources.getText(R.string.pin_mismatch_error)));
            assertThat(pinErrorDisplay.getVisibility(), equalTo(View.VISIBLE));
        });
    }

    @Test
    public void canProvideTheFingerprintManager() {
        scenario.onFragment(fragment -> {
            FingerprintManager fingerprintManager = (FingerprintManager) fragment.getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

            assertThat(fragment.getFingerprintManager(), equalTo(fingerprintManager));
        });
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void willReturnNullForFingerprintOnNonSupportedSDKs() {
        verifyZeroInteractions(fingerPrintAuthDialog);
        verifyZeroInteractions(fingerprintAuthPresenter);
    }

    @Test
    public void fragmentDismissesDialogWhenGoingIntoBackground() {
        setupFingerprint();
        scenario.onFragment(AuthenticateFragment::authenticateWithFingerprint);
        when(fingerPrintAuthDialog.isAdded()).thenReturn(true);

        scenario.moveToState(Lifecycle.State.DESTROYED);

        verify(fingerPrintAuthDialog).dismiss();
    }

    @Test
    public void showDialogOnResumeToContinueFingerprintAuth() {
        setupFingerprint();
        scenario.onFragment(AuthenticateFragment::authenticateWithFingerprint);

        scenario.moveToState(Lifecycle.State.RESUMED);

        verify(fingerprintAuthPresenter).captureFingerprintAuth();
    }
}