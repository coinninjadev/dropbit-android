package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService;

public class AuthenticationCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, CNGlobalMessagingService.class));
    }
}
