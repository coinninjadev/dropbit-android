package com.coinninja.coinkeeper.presenter;

import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

public class PreferencePresentor {

    private View view;
    private PreferencesUtil preferencesUtil;

    public static PreferencePresentor newInstance(View view, PreferencesUtil preferencesUtil) {
        return new PreferencePresentor(view, preferencesUtil);
    }

    public PreferencePresentor(View view, PreferencesUtil preferencesUtil) {
        this.view = view;
        this.preferencesUtil = preferencesUtil;
    }

    public void usersFingerprintAuthPreferenceCaptured(boolean useFingerprint) {
        preferencesUtil.savePreference(UserPreferences.PREFERENCE_FINGERPRINT, useFingerprint);
        view.preferenceCaptureComplete(Preference.FINGERPRINT);
    }


    public interface View {
        void preferenceCaptureComplete(Preference which);
    }

    public enum Preference {
        FINGERPRINT,
    }
}
