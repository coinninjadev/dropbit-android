package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.runner.ResendPhoneVerificationRunner;
import com.coinninja.coinkeeper.util.Intents;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class ResendPhoneVerificationService extends IntentService {

    public static final String TAG = ResendPhoneVerificationService.class.getName();

    @Inject
    ResendPhoneVerificationRunner runner;

    public ResendPhoneVerificationService() {
        this(TAG);
    }

    public ResendPhoneVerificationService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!intent.hasExtra(Intents.EXTRA_PHONE_NUMBER)) return;

        PhoneNumber phoneNumber = intent.getExtras().getParcelable(Intents.EXTRA_PHONE_NUMBER);
        CNPhoneNumber number = new CNPhoneNumber(phoneNumber);
        runner.setCNPhoneNumber(number);
        runner.run();
    }

    public void setRunner(ResendPhoneVerificationRunner runner) {
        this.runner = runner;
    }
}
