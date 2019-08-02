package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.service.BtcBroadcastNotificationService;
import com.coinninja.coinkeeper.service.ContactLookupService;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class WalletSyncCompletedReceiver extends BroadcastReceiver {
    @Inject
    JobServiceScheduler jobServiceScheduler;

    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Inject
    CNWalletManager cnWalletManager;

    @Inject
    Analytics analytics;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        localBroadCastUtil.sendBroadcast(intent);

        jobServiceScheduler.enqueueWork(context,
                BtcBroadcastNotificationService.class,
                JobServiceScheduler.BROADCAST_NOTIFICATION_SERVICE,
                new Intent(context, BtcBroadcastNotificationService.class));

        analytics.setUserProperty(Analytics.Companion.PROPERTY_HAS_BTC_BALANCE, cnWalletManager.getHasBalance());

        jobServiceScheduler.enqueueWork(context,
                ContactLookupService.class,
                JobServiceScheduler.CONTACT_LOOKUP_SERVICE,
                new Intent(context, ContactLookupService.class));
    }

}
