package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class FulfillSentInvitesService extends IntentService {
    public static final String TAG = FulfillSentInvitesService.class.getName();
    @Inject
    FulfillSentInvitesRunner fulfillSentInvitesRunner;

    public FulfillSentInvitesService() {
        this(TAG);
    }

    public FulfillSentInvitesService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        fulfillSentInvitesRunner.run();
    }

    @Override
    public void onDestroy() {
        fulfillSentInvitesRunner = null;
        super.onDestroy();
    }
}
