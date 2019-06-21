package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;


public class PushNotificationTokenManager {
    static final String PUSH_NOTIFICATION_DEVICE_TOKEN = "push_notification_device_token";

    private PreferencesUtil preferencesUtil;

    @Inject
    PushNotificationTokenManager(PreferencesUtil preferencesUtil) {
        this.preferencesUtil = preferencesUtil;
    }

    public void saveToken(String token) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_DEVICE_TOKEN, token);
    }

    public String getToken() {
        return preferencesUtil.getString(PUSH_NOTIFICATION_DEVICE_TOKEN, "");
    }

}
