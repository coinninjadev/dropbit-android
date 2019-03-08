package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class DeviceRebootBootCompletedReceiver extends BroadcastReceiver {

    @Inject
    SyncWalletManager syncWalletManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        syncWalletManager.scheduleHourlySync();
    }
}
