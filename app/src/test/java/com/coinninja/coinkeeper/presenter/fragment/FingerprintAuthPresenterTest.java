package com.coinninja.coinkeeper.presenter.fragment;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FingerprintAuthPresenter.class)
public class FingerprintAuthPresenterTest {

    @Mock
    private FingerprintManager.AuthenticationResult mockAuthenticationResult;

    @Mock
    private CancellationSignal mockCancellationSignal;

    @Mock
    private FingerprintManager.CryptoObject mockCryptoObject;

    @Mock
    private FingerprintManager mockFingerprintManager;

    @Mock
    private ViewImpl mockView;

    @Mock
    private FingerprintAuthDialog mockDialog;

    private FingerprintAuthPresenter fingerprintAuthPresenter;


    @Before
    public void setUp() {
        mockStatic(FingerprintAuthPresenter.class);
        when(FingerprintAuthPresenter.newCancellationSignal()).thenReturn(mockCancellationSignal);
        when(mockView.getFingerprintManager()).thenReturn(mockFingerprintManager);
        fingerprintAuthPresenter = new FingerprintAuthPresenter(mockView);
        fingerprintAuthPresenter.setDialog(mockDialog);
    }

    private void configureFingerprintSupport() {
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(true);
    }

    @Test
    public void instructsTheViewToShowFingerprintPermissionWhenUserHasItSetup() {
        configureFingerprintSupport();

        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(mockView, times(1)).showAuthenticateWithFingerprint();
    }

    @Test
    public void completesFingerprintAuthWhenManagerDoesNotExist() {

        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(mockView, times(1)).onFingerprintAuthenticationNotAvailable();
    }

    @Test
    public void completesFingerprintAuthPrefrencesWhenDeviceLacksHardware() {

        // Given a Device without Fingerprint Hardware
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(false);

        fingerprintAuthPresenter.captureFingerprintAuth();

        // Then Presentor will notify mockView that defining Auth Preferences have been conducted
        verify(mockView, times(1)).onFingerprintAuthenticationNotAvailable();

    }

    @Test
    public void completesFingerprintAuthPrefrencesWhenDeviceSupportsHardwareButUserHasNoFingers() {
        when(mockFingerprintManager.isHardwareDetected()).thenReturn(true);
        when(mockFingerprintManager.hasEnrolledFingerprints()).thenReturn(false);

        fingerprintAuthPresenter.captureFingerprintAuth();

        verify(mockView, times(1)).onFingerprintAuthenticationNotAvailable();
    }

    @Test
    public void presentorListensToFingerprintAuthWhenInstructed() throws NoSuchFieldException {
        configureFingerprintSupport();
        PrivateAccessor.setField(fingerprintAuthPresenter, "fingerprintManager", mockFingerprintManager);
        PrivateAccessor.setField(fingerprintAuthPresenter, "cryptoObject", mockCryptoObject);

        fingerprintAuthPresenter.startListeningForTouch();

        verify(mockFingerprintManager, times(1)).authenticate(mockCryptoObject, mockCancellationSignal,
                0, fingerprintAuthPresenter, null);
    }

    @Test
    public void presentorStopsListeningToFingerprintAuthWhenInstructed() throws NoSuchFieldException {
        configureFingerprintSupport();
        // Given presentor is observing an for touch
        PrivateAccessor.setField(fingerprintAuthPresenter, "fingerprintManager", mockFingerprintManager);
        PrivateAccessor.setField(fingerprintAuthPresenter, "cryptoObject", mockCryptoObject);
        fingerprintAuthPresenter.startListeningForTouch();

        // When presentor instructed to stop observing system fingerprint auth
        fingerprintAuthPresenter.stopListeningForTouch();

        // Then presentor sends inturupt signal
        verify(mockCancellationSignal, times(1)).cancel();
    }

    @Test
    public void presentorInformsCallBackOfSuccess() {

        fingerprintAuthPresenter.onAuthenticationSucceeded(mockAuthenticationResult);

        verify(mockDialog, times(1)).onSucces();
    }

    @Test
    public void presentorInformsCallBackOfError() {

        int errorCode = 23;
        String errString = "an error";


        fingerprintAuthPresenter.onAuthenticationError(errorCode, errString);

        verify(mockDialog).onError(errorCode, errString);
    }

    @Test
    public void presentorInformsCallBackOfFailure() {

        fingerprintAuthPresenter.onAuthenticationFailed();

        verify(mockDialog).onFailure();
    }

    @Test
    public void presentorInformsCallBackOfHelpResponse() {
        int helpCode = 22;
        String helpString = "help";

        fingerprintAuthPresenter.onAuthenticationHelp(helpCode, helpString);

        verify(mockDialog).onHelp(helpCode, helpString);
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

        verify(mockCancellationSignal, times(1)).cancel();
    }

    @Test
    public void notifyViewThatUserCanceledFingerprintAuth() {
        fingerprintAuthPresenter.onAuthCancel();

        verify(mockView, times(1)).onFingerprintAuthenticationCanceled();
    }

    @Test
    public void notifyViewThatFingerprintAuthWasSuccessful() {
        fingerprintAuthPresenter.onSuccessfulTransition();

        verify(mockView, times(1)).onFingerprintAuthenticationComplete();
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

