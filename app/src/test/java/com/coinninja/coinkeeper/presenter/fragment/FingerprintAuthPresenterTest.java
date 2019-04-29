package com.coinninja.coinkeeper.presenter.fragment;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowFingerprintManager;

import java.security.Signature;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class FingerprintAuthPresenterTest {

    @Mock
    private FingerprintManager.AuthenticationResult authenticationResult;

    @Mock
    private CancellationSignal cancellationSignal;

    private FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(mock(Signature.class));

    private FingerprintManager fingerprintManager;

    @Mock
    private ViewImpl view;

    @Mock
    private FingerprintAuthDialog dialog;

    private FingerprintAuthPresenter fingerprintAuthPresenter;
    private ShadowFingerprintManager shadowFingerprintManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        fingerprintManager = (FingerprintManager) ApplicationProvider.getApplicationContext().getSystemService(Context.FINGERPRINT_SERVICE);
        shadowFingerprintManager = shadowOf(fingerprintManager);
        when(view.getFingerprintManager()).thenReturn(fingerprintManager);
        fingerprintAuthPresenter = new FingerprintAuthPresenter(view);
        fingerprintAuthPresenter.cancellationSignal = cancellationSignal;
        fingerprintAuthPresenter.cryptoObject = cryptoObject;
        fingerprintAuthPresenter.fingerprintManager = fingerprintManager;
        fingerprintAuthPresenter.setDialog(dialog);
    }

    @After
    public void tearDown() {
        authenticationResult = null;
        cancellationSignal = null;
        cryptoObject = null;
        fingerprintAuthPresenter = null;
        fingerprintManager = null;
        view = null;
        dialog = null;
        shadowFingerprintManager = null;
    }

    @Test
    public void instructsTheViewToShowFingerprintPermissionWhenUserHasItSetup() {
        configureFingerprintSupport();

        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(view).showAuthenticateWithFingerprint();
    }

    @Test
    public void completesFingerprintAuthWhenManagerDoesNotExist() {

        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(view).onFingerprintAuthenticationNotAvailable();
    }

    @Test
    public void completesFingerprintAuthPrefrencesWhenDeviceLacksHardware() {
        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(view).onFingerprintAuthenticationNotAvailable();
    }

    @Test
    public void completesFingerprintAuthPrefrencesWhenDeviceSupportsHardwareButUserHasNoFingers() {
        shadowFingerprintManager.setIsHardwareDetected(true);
        shadowFingerprintManager.setDefaultFingerprints(0);

        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(view).onFingerprintAuthenticationNotAvailable();
    }

    @Test
    public void presenterListensToFingerprintAuthWhenInstructed_successful() {
        configureFingerprintSupport();

        fingerprintAuthPresenter.startListeningForTouch();
        shadowFingerprintManager.authenticationSucceeds();

        verify(dialog).onSucces();
    }

    @Test
    public void presenterListensToFingerprintAuthWhenInstructed_failure() {
        configureFingerprintSupport();

        fingerprintAuthPresenter.startListeningForTouch();
        shadowFingerprintManager.authenticationFails();

        verify(dialog).onFailure();
    }

    @Test
    public void presenterStopsListeningToFingerprintAuthWhenInstructed() {
        configureFingerprintSupport();
        fingerprintAuthPresenter.startListeningForTouch();

        fingerprintAuthPresenter.stopListeningForTouch();

        verify(cancellationSignal).cancel();
    }

    @Test
    public void presenterInformsCallBackOfSuccess() {

        fingerprintAuthPresenter.onAuthenticationSucceeded(authenticationResult);

        verify(dialog).onSucces();
    }

    @Test
    public void presenterInformsCallBackOfError() {

        int errorCode = 23;
        String errString = "an error";


        fingerprintAuthPresenter.onAuthenticationError(errorCode, errString);

        verify(dialog).onError(errorCode, errString);
    }

    @Test
    public void presenterInformsCallBackOfFailure() {

        fingerprintAuthPresenter.onAuthenticationFailed();

        verify(dialog).onFailure();
    }

    @Test
    public void presenterInformsCallBackOfHelpResponse() {
        int helpCode = 22;
        String helpString = "help";

        fingerprintAuthPresenter.onAuthenticationHelp(helpCode, helpString);

        verify(dialog).onHelp(helpCode, helpString);
    }

    @Test
    public void tearingDownCleansUpAuthCallbackAndFingerprintManager() throws NoSuchFieldException {
        fingerprintAuthPresenter.tearDown();

        assertNull(PrivateAccessor.getField(fingerprintAuthPresenter, "view"));
    }

    @Test
    public void stopObservingFingerprintWhenUserOnAuthCancel() {
        configureFingerprintSupport();

        fingerprintAuthPresenter.onAuthCancel();

        verify(cancellationSignal).cancel();
    }

    @Test
    public void notifyViewThatUserCanceledFingerprintAuth() {
        fingerprintAuthPresenter.onAuthCancel();

        verify(view).onFingerprintAuthenticationCanceled();
    }

    @Test
    public void notifyViewThatFingerprintAuthWasSuccessful() {
        fingerprintAuthPresenter.onSuccessfulTransition();

        verify(view).onFingerprintAuthenticationComplete();
    }

    private void configureFingerprintSupport() {
        shadowFingerprintManager.setIsHardwareDetected(true);
        shadowFingerprintManager.setDefaultFingerprints(2);
    }

    // implement interface for mocking
    class ViewImpl implements FingerprintAuthPresenter.View {
        @Override
        public FingerprintManager getFingerprintManager() {
            return null;
        }

        @Override
        public void showAuthenticateWithFingerprint() {
        }

        @Override
        public void onFingerprintAuthenticationNotAvailable() {
        }

        @Override
        public void onFingerprintAuthenticationComplete() {

        }

        @Override
        public void onFingerprintAuthenticationCanceled() {

        }
    }
}

