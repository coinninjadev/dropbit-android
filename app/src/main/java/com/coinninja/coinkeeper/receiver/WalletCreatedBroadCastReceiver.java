package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class WalletCreatedBroadCastReceiver extends BroadcastReceiver {

    @Inject
    Analytics analytics;

    @Inject
    SyncWalletManager syncWalletManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET, true);
        syncWalletManager.schedule30SecondSync();
        syncWalletManager.syncNow();
        syncWalletManager.scheduleHourlySync();
    }
}
