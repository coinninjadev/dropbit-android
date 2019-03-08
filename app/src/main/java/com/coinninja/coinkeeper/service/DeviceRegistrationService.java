package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager;
import com.coinninja.coinkeeper.di.interfaces.UUID;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;

public class DeviceRegistrationService extends JobIntentService {
    static final String TAG = DeviceRegistrationService.class.getName();

    @Inject
    PushNotificationServiceManager pushNotificationServiceManager;

    @Inject
    @UUID
    String uuid;


    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        pushNotificationServiceManager.registerDevice(uuid);
    }
}
