package com.coinninja.coinkeeper.presenter;

import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

        verify(preferencesUtil, times(1)).savePreference(UserPreferences.PREFERENCE_FINGERPRINT, true);
    }


    @Test
    public void notifyViewPreferenceHasBeenCapturedSuccessfully() {
        PreferencePresentor presentor = new PreferencePresentor(mockView, preferencesUtil);

        presentor.usersFingerprintAuthPreferenceCaptured(true);

        verify(mockView, times(1)).preferenceCaptureComplete(PreferencePresentor.Preference.FINGERPRINT);

    }

}