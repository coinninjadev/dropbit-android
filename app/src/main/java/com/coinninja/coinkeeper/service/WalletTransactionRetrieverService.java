package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import dagger.android.AndroidInjection;

public class WalletTransactionRetrieverService extends JobIntentService {

    @Inject
    FullSyncWalletRunner fullSyncWalletRunner;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        fullSyncWalletRunner.run();
    }
}
