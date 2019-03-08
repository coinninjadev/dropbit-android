package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner;
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner;
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class SyncDropBitService extends IntentService {
    public SyncDropBitService() {
        this(SyncDropBitService.class.getName());
    }

    public SyncDropBitService(String name) {
        super(name);
    }

    @Inject
    AccountManager accountManager;
    @Inject
    SyncIncomingInvitesRunner syncIncomingInvitesRunner;
    @Inject
    ReceivedInvitesStatusRunner receivedInvitesStatusRunner;
    @Inject
    FulfillSentInvitesRunner fulfillSentInvitesRunner;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        accountManager.cacheAddresses();
        Log.d("SYNC", "Incoming invites -- Invites Sent to me");
        syncIncomingInvitesRunner.run();
        Log.d("SYNC", "Fulfill invites -- Invites I sent");
        fulfillSentInvitesRunner.run();
        Log.d("SYNC", "Get Fulfilled invites -- Invites Sent to me");
        receivedInvitesStatusRunner.run();
    }
}
