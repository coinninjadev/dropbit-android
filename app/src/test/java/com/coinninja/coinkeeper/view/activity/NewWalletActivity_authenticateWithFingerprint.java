package com.coinninja.coinkeeper.view.activity;


import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.presenter.PreferencePresentor;
import com.coinninja.coinkeeper.presenter.fragment.FingerprintAuthPresenter;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class NewWalletActivity_authenticateWithFingerprint {
    private ActivityController<CreatePinActivity> activityController;
    private CreatePinActivity activity;
    private PinFragmentPresenter pinFragmentPresenter;
    private TestCoinKeeperApplication application;

    private void initWithIntent(Intent intent) {
        activityController = Robolectric.buildActivity(CreatePinActivity.class, intent);
        activity = activityController.get();
        activityController.newIntent(intent).create();
        activity.setPinFragmentPresenter(pinFragmentPresenter);
        activityController.start().resume().visible();
    }

    private String initWithNextIntent() {
        Intent intent = new Intent(application, CreatePinActivity.class);
        String nextActivity = VerifyPhoneNumberActivity.class.getName();
        intent.putExtra(Intents.EXTRA_NEXT, nextActivity);

        initWithIntent(intent);
        return nextActivity;
    }

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        pinFragmentPresenter = mock(PinFragmentPresenter.class);
    }

    @Test
    public void present_user_with_fingerprint() {
        initWithNextIntent();
        activity.fingerprintAuthPresenter = mock(FingerprintAuthPresenter.class);

        activity.showAuthenticateWithFingerprint();

        assertNotNull(activity.getFragmentManager().findFragmentByTag("DIALOG_PREF_FINGERPRINT"));
    }

    @Test
    public void dismisses_dialog_on_fingerprint_auth_cancel() {
        initWithNextIntent();
        activity.fingerprintAuthDialog = mock(FingerprintAuthDialog.class);

        activity.onFingerprintAuthenticationCanceled();

        verify(activity.fingerprintAuthDialog, times(1)).dismiss();
    }

    @Test
    public void dismisses_dialog_on_successful_fingerprint_auth() {
        initWithNextIntent();
        activity.preferencePresentor = mock(PreferencePresentor.class);
        activity.fingerprintAuthDialog = mock(FingerprintAuthDialog.class);

        activity.onFingerprintAuthenticationComplete();

        verify(activity.fingerprintAuthDialog, times(1)).dismiss();
    }

}
