package com.coinninja.coinkeeper.interactor;


import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;

public class PreferenceInteractor {
    public static final String PREFERENCE_FINGERPRINT = "useFingerprintForAuth";
    public static final String PREFERENCE_SKIP_INVITE_HELP = "skipInviteHelp";

    private PreferencesUtil preferencesUtil;

    @Inject
    public PreferenceInteractor(PreferencesUtil preferencesUtil) {
        this.preferencesUtil = preferencesUtil;
    }

    public boolean getShouldShowInviteHelp() {
        return !preferencesUtil.getBoolean(PREFERENCE_SKIP_INVITE_HELP);
    }

    public void skipInviteHelpScreen(PreferencesUtil.Callback callback) {
        preferencesUtil.savePreference(PREFERENCE_SKIP_INVITE_HELP, true, callback);
    }

}
