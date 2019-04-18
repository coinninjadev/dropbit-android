package com.coinninja.coinkeeper.util.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class LocalBroadCastUtil {

    private final LocalBroadcastManager localBroadcastManager;
    private final Context context;

    @Inject
    public LocalBroadCastUtil(@ApplicationContext Context context) {
        this.context = context;
        localBroadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
    }

    public void sendBroadcast(String action) {
        localBroadcastManager.sendBroadcast(new Intent(action));
    }

    public void sendBroadcast(Intent intent) {
        localBroadcastManager.sendBroadcast(intent);
    }

    public void registerReceiver(@NonNull BroadcastReceiver receiver, IntentFilter filter) {
        localBroadcastManager.registerReceiver(receiver, filter);
    }

    public void unregisterReceiver(@NonNull BroadcastReceiver receiver) {
        localBroadcastManager.unregisterReceiver(receiver);
    }

    public void sendGlobalBroadcast(Class klass, String intentAction) {
        context.sendBroadcast(new Intent(context, klass).setAction(intentAction));
    }
}
