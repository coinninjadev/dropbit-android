package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.service.runner.ResendPhoneVerificationRunner;
import com.coinninja.coinkeeper.util.Intents;

import androidx.annotation.Nullable;

public class ResendPhoneVerificationService extends IntentService {

    public static final String TAG = ResendPhoneVerificationService.class.getName();
    private ResendPhoneVerificationRunner runner;

    public ResendPhoneVerificationService() {
        this(TAG);
    }

    public ResendPhoneVerificationService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        runner = new ResendPhoneVerificationRunner(this);
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
