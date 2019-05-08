package com.coinninja.coinkeeper.cn.dropbit;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

import static com.coinninja.coinkeeper.util.DropbitIntents.ACTION_CANCEL_DROPBIT;
import static com.coinninja.coinkeeper.util.DropbitIntents.ACTION_CREATE_NOTIFICATION;

public class DropBitService extends IntentService {

    @Inject
    DropBitCancellationManager dropBitCancellationManager;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    DropBitAddMemoManager dropBitAddMemoManager;

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
        if (intent.getAction() == null || intent.getAction().equals("")) { return; }

        switch (intent.getAction()) {
            case ACTION_CREATE_NOTIFICATION:
                if (!intent.hasExtra(DropbitIntents.EXTRA_DROPBIT_MEMO) || !intent.hasExtra(DropbitIntents.EXTRA_DROPBIT_TXID)) { return; }
                dropBitAddMemoManager.createMemo(intent.getStringExtra(DropbitIntents.EXTRA_DROPBIT_TXID), intent.getStringExtra(DropbitIntents.EXTRA_DROPBIT_MEMO));
                break;
            case ACTION_CANCEL_DROPBIT:
                if (!intent.hasExtra(DropbitIntents.EXTRA_INVITATION_ID)) { return; }
                dropBitCancellationManager.markAsCanceled(intent.getStringExtra(DropbitIntents.EXTRA_INVITATION_ID));
                break;
        }

        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED);
    }


}
