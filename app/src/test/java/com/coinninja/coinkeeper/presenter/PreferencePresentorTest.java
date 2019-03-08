package com.coinninja.coinkeeper.presenter;

import com.coinninja.coinkeeper.interactor.PreferenceInteractor;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PreferencePresentorTest {

    @Mock
    PreferencesUtil preferencesUtil;

    @Mock
    PreferencePresentor.View mockView;

    @Test
    public void presentorSavesFingerprintPreference() {
        PreferencePresentor presentor = new PreferencePresentor(mockView, preferencesUtil);

        presentor.usersFingerprintAuthPreferenceCaptured(true);

        verify(preferencesUtil, times(1)).savePreference(PreferenceInteractor.PREFERENCE_FINGERPRINT, true);
    }


    @Test
    public void notifyViewPreferenceHasBeenCapturedSuccessfully() {
        PreferencePresentor presentor = new PreferencePresentor(mockView, preferencesUtil);

        presentor.usersFingerprintAuthPreferenceCaptured(true);

        verify(mockView, times(1)).preferenceCaptureComplete(PreferencePresentor.Preference.FINGERPRINT);

    }

}