package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.runner.RegisterPhoneNumberRunnable;
import com.coinninja.coinkeeper.util.Intents;

import androidx.annotation.Nullable;

public class RegisterUsersPhoneService extends IntentService {

    public static final String LOG_TAG = RegisterUsersPhoneService.class.getSimpleName();
    private RegisterPhoneNumberRunnable runner;

    public RegisterUsersPhoneService() {
        this(LOG_TAG);
    }

    public RegisterUsersPhoneService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        runner = new RegisterPhoneNumberRunnable(this);
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

    public void setRunner(RegisterPhoneNumberRunnable runner) {
        this.runner = runner;
    }

    public RegisterPhoneNumberRunnable getRunner() {
        return runner;
    }
}
