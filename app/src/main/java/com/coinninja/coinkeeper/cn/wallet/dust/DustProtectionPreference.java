package com.coinninja.coinkeeper.cn.wallet.dust;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;

public class DustProtectionPreference {

    static final String PREFERENCE_KEY = "CN_DUST_PROTECTION_ENABLED";

    private final PreferencesUtil preferencesUtil;

    @Inject
    DustProtectionPreference(PreferencesUtil preferencesUtil) {

        this.preferencesUtil = preferencesUtil;
    }

    public boolean isDustProtectionEnabled() {
        return preferencesUtil.contains(PREFERENCE_KEY) && preferencesUtil.getBoolean(PREFERENCE_KEY);
    }

    public boolean toggleDustProtection() {
        preferencesUtil.savePreference(PREFERENCE_KEY, !isDustProtectionEnabled());
        return isDustProtectionEnabled();
    }

    public void setProtection(boolean preference) {
        preferencesUtil.savePreference(PREFERENCE_KEY, preference);
    }
}
