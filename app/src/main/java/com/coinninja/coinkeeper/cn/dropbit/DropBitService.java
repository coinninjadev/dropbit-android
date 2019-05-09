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
    DropBitMeServiceManager dropBitMeServiceManager;
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
        if (intent.getAction() == null) return;

        switch (intent.getAction()) {
            case DropbitIntents.ACTION_CANCEL_DROPBIT:
                onCancelDropbit(intent);
                break;
            case DropbitIntents.ACTION_DROPBIT_ME_ENABLE_ACCOUNT:
                dropBitMeServiceManager.enableAccount();
                break;
            case DropbitIntents.ACTION_DROPBIT_ME_DISABLE_ACCOUNT:
                dropBitMeServiceManager.disableAccount();
                break;
            case ACTION_CREATE_NOTIFICATION:
                if (!intent.hasExtra(DropbitIntents.EXTRA_DROPBIT_MEMO) || !intent.hasExtra(DropbitIntents.EXTRA_DROPBIT_TXID)) { return; }
                dropBitAddMemoManager.createMemo(intent.getStringExtra(DropbitIntents.EXTRA_DROPBIT_TXID), intent.getStringExtra(DropbitIntents.EXTRA_DROPBIT_MEMO));
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED);
                break;
            default:
                return;

        }

    }

    private void onCancelDropbit(Intent intent) {
        if (!intent.hasExtra(DropbitIntents.EXTRA_INVITATION_ID)) return;

        dropBitCancellationManager.markAsCanceled(intent.getStringExtra(DropbitIntents.EXTRA_INVITATION_ID));
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED);
    }
}
