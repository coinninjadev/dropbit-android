package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.service.runner.UserPhoneConfirmationRunner;
import com.coinninja.coinkeeper.util.Intents;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class UserPhoneConfirmationService extends IntentService {

    public static final String LOG_TAG = UserPhoneConfirmationService.class.getName();

    @Inject
    UserPhoneConfirmationRunner runner;

    public UserPhoneConfirmationService() {
        this(LOG_TAG);
    }

    public UserPhoneConfirmationService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.hasExtra(Intents.EXTRA_PHONE_NUMBER_CODE)) {
            String code = intent.getExtras().getString(Intents.EXTRA_PHONE_NUMBER_CODE);
            runner.setCode(code);
            runner.run();
        }
    }
}
