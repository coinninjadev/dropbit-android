package com.coinninja.coinkeeper.interactor;


import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;

public class UserPreferences {
    public static final String PREFERENCE_FINGERPRINT = "useFingerprintForAuth";
    public static final String PREFERENCE_SKIP_INVITE_HELP = "skipInviteHelp";
    public static final String PREFERENCE_TWITTER_SHARE_CARD = "twitterShareCard";
    public static final String PREFERENCE_SHOW_BITCOIN_COPY_DIALOG = "skipShowBitcoinCopyDialog";

    private PreferencesUtil preferencesUtil;

    @Inject
    public UserPreferences(PreferencesUtil preferencesUtil) {
        this.preferencesUtil = preferencesUtil;
    }

    public boolean getShouldShowInviteHelp() {
        return !preferencesUtil.getBoolean(PREFERENCE_SKIP_INVITE_HELP);
    }

    public void setShouldShareOnTwitter(boolean share) {
        preferencesUtil.savePreference(PREFERENCE_TWITTER_SHARE_CARD, share);
    }

    public boolean getShouldShareOnTwitter() {
        return preferencesUtil.getBoolean(PREFERENCE_TWITTER_SHARE_CARD, true);
    }

    public void skipInviteHelpScreen(PreferencesUtil.Callback callback) {
        preferencesUtil.savePreference(PREFERENCE_SKIP_INVITE_HELP, true, callback);
    }

    public boolean getDidUserSeeCopyAddressDialog() {
        return preferencesUtil.getBoolean(PREFERENCE_SHOW_BITCOIN_COPY_DIALOG);
    }

    public void setDidUserSeeCopyAddressDialog() {
        preferencesUtil.savePreference(PREFERENCE_SHOW_BITCOIN_COPY_DIALOG, true);
    }
}
