package com.coinninja.coinkeeper.cn.service;

import android.content.Intent;

import com.coinninja.coinkeeper.cn.service.runner.CNGlobalMessagesRunner;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;

public class CNGlobalMessagingService extends JobIntentService {

    @Inject
    CNGlobalMessagesRunner cnGlobalMessagesRunner;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        cnGlobalMessagesRunner.run();
    }
}
