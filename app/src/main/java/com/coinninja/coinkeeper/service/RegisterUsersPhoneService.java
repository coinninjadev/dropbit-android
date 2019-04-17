package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.runner.RegisterPhoneNumberRunnable;
import com.coinninja.coinkeeper.util.Intents;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class RegisterUsersPhoneService extends IntentService {

    public static final String LOG_TAG = RegisterUsersPhoneService.class.getSimpleName();

    @Inject
    RegisterPhoneNumberRunnable runner;

    public RegisterUsersPhoneService() {
        this(LOG_TAG);
    }

    public RegisterUsersPhoneService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        PhoneNumber phoneNumber;
        if (!intent.hasExtra(Intents.EXTRA_PHONE_NUMBER)) {
            stopSelf();
            return;
        }

        phoneNumber = intent.getParcelableExtra(Intents.EXTRA_PHONE_NUMBER);
        runner.setCNPhoneNumber(new CNPhoneNumber(phoneNumber));
        runner.run();

    }
}
