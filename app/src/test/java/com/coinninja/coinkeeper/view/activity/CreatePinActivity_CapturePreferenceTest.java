package com.coinninja.coinkeeper.view.activity;


import com.coinninja.coinkeeper.presenter.PreferencePresentor;
import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(RobolectricTestRunner.class)
public class CreatePinActivity_CapturePreferenceTest {
    private PreferencePresentor preferencePresentor;

    private ActivityController<CreatePinActivity> activityController;
    private CreatePinActivity createPinActivity;

    @Before
    public void setUp() {
        preferencePresentor = mock(PreferencePresentor.class);
        activityController = Robolectric.buildActivity(CreatePinActivity.class);
        createPinActivity = activityController.get();
        activityController.create();
        createPinActivity.preferencePresentor = preferencePresentor;
    }

    @Test
    public void savesFingerprintAuthPreferenceWhenSuccessfulAuthOccurs() {
        FingerprintAuthDialog fingerprintAuthDialog = mock(FingerprintAuthDialog.class);
        createPinActivity.fingerprintAuthDialog = fingerprintAuthDialog;
        createPinActivity.onFingerprintAuthenticationComplete();

        verify(preferencePresentor).usersFingerprintAuthPreferenceCaptured(true);
        verify(fingerprintAuthDialog).dismiss();
        ;
    }

}
