package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.service.ContactLookupService;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class StartupCompleteReceiver extends BroadcastReceiver {

    @Inject
    Analytics analytics;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        analytics.trackEvent(Analytics.EVENT_APP_OPEN);
        analytics.flush();
    }

}
