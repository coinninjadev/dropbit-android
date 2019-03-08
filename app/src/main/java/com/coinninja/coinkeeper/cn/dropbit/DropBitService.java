package com.coinninja.coinkeeper.cn.dropbit;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class DropBitService extends IntentService {

    public DropBitService(String name) {
        super(name);
    }

    public DropBitService() {
        super(DropBitService.class.getName());
    }

    @Inject
    DropBitCancellationManager dropBitCancellationManager;

    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.hasExtra(Intents.EXTRA_INVITATION_ID)) {
            dropBitCancellationManager.markAsCanceled(intent.getStringExtra(Intents.EXTRA_INVITATION_ID));
            localBroadCastUtil.sendBroadcast(Intents.ACTION_TRANSACTION_DATA_CHANGED);
        }

    }
}
