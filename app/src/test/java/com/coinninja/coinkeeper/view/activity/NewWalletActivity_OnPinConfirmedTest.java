package com.coinninja.coinkeeper.view.activity;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowFingerprintManager;

import junitx.util.PrivateAccessor;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class NewWalletActivity_OnPinConfirmedTest {

    private final String goodPin = "#DFSKLJFDSKFDSFLKJDLKSF";
    private TestCoinKeeperApplication application = (TestCoinKeeperApplication) RuntimeEnvironment.application;


    @Test
    public void coinNinjaWillRequestFingerprintAuth_whenPinHasBeenConfirmed() {
        CreatePinActivity newWalletActivity = Robolectric.setupActivity(CreatePinActivity.class);
        ShadowFingerprintManager fingerprintManager = shadowOf((FingerprintManager) newWalletActivity.getSystemService(Context.FINGERPRINT_SERVICE));
        fingerprintManager.setIsHardwareDetected(true);
        fingerprintManager.setHasEnrolledFingerprints(true);

        newWalletActivity.onPinConfirmed(goodPin);

        assertNotNull(newWalletActivity.getSupportFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void clearPinFieldWhenUsersPinIsConfirmed() throws Exception {
        PinFragmentPresenter mockPinFragmentPresenter = mock(PinFragmentPresenter.class);
        CreatePinActivity newWalletActivity = Robolectric.setupActivity(CreatePinActivity.class);
        PrivateAccessor.setField(newWalletActivity, "pinFragmentPresenter", mockPinFragmentPresenter);

        newWalletActivity.onPinConfirmed(goodPin);

        verify(mockPinFragmentPresenter).clearPin();
    }

    @Test
    public void userGetsAuthenticatedWhenPinIsConfirmedAndSaved() {
        CreatePinActivity newWalletActivity = Robolectric.setupActivity(CreatePinActivity.class);

        newWalletActivity.onPinConfirmed(goodPin);

        verify(application.authentication).setAuthenticated();
    }
}
