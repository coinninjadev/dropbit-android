package com.coinninja.coinkeeper.ui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.service.DeleteWalletService;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

public class DeleteWalletPresenter {

    private final Context context;
    private final LocalBroadCastUtil localBroadCastUtil;
    private OnDeleteWalletCallback onDeleteWalletCallback;
    final DeleteWalletCompleteReceiver receiver;
    private final IntentFilter filter;

    @Inject
    public DeleteWalletPresenter(@ApplicationContext Context context, LocalBroadCastUtil localBroadCastUtil) {
        this.context = context;
        this.localBroadCastUtil = localBroadCastUtil;
        receiver = new DeleteWalletCompleteReceiver();
        filter = new IntentFilter();
        filter.addAction(Intents.ACTION_ON_WALLET_DELETED);
    }

    public void onDelete() {
        localBroadCastUtil.registerReceiver(receiver, filter);
        context.startService(new Intent(context, DeleteWalletService.class));
    }

    private void onDeleteCompleted() {
        if (null != onDeleteWalletCallback)
            onDeleteWalletCallback.onDeleted();
        localBroadCastUtil.unregisterReceiver(receiver);
    }

    public void setCallback(OnDeleteWalletCallback onDeleteWalletCallback) {
        this.onDeleteWalletCallback = onDeleteWalletCallback;
    }

    public class DeleteWalletCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onDeleteCompleted();
        }

    }
}
