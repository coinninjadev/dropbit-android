package com.coinninja.coinkeeper.cn.dropbit;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class DropBitService extends IntentService {

    @Inject
    DropBitCancellationManager dropBitCancellationManager;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    public DropBitService(String name) {
        super(name);
    }

    public DropBitService() {
        super(DropBitService.class.getName());
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.hasExtra(DropbitIntents.EXTRA_INVITATION_ID)) {
            dropBitCancellationManager.markAsCanceled(intent.getStringExtra(DropbitIntents.EXTRA_INVITATION_ID));
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED);
        }
    }
}
