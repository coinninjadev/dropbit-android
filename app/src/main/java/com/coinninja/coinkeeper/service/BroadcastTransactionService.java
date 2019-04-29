package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.service.runner.SaveTransactionRunner;
import com.coinninja.coinkeeper.util.DropbitIntents;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class BroadcastTransactionService extends IntentService {
    public static final String TAG = BroadcastTransactionService.class.getName();

    @Inject
    SaveTransactionRunner runner;

    public BroadcastTransactionService() {
        this(TAG);
    }

    public BroadcastTransactionService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.hasExtra(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO)) {
            CompletedBroadcastDTO completedBroadcastActivityDTO = intent.getParcelableExtra(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO);
            runner.setCompletedBroadcastActivityDTO(completedBroadcastActivityDTO);
            runner.run();
        }
    }

    public void setRunner(SaveTransactionRunner runner) {
        this.runner = runner;
    }
}
