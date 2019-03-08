package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.service.DropbitServicePatchService;
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ApplicationStartedReceiver extends BroadcastReceiver {

    @Inject
    JobServiceScheduler jobServiceScheduler;

    @Inject
    SyncWalletManager syncWalletManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        jobServiceScheduler.enqueueWork(
                context,
                PushNotificationEndpointRegistrationService.class,
                JobServiceScheduler.ENDPOINT_REGISTRATION_SERVICE_JOB_ID,
                new Intent(context, PushNotificationEndpointRegistrationService.class));
        jobServiceScheduler.enqueueWork(
                context,
                DropbitServicePatchService.class,
                JobServiceScheduler.DROP_BIT_SERVICE_PATCHER_JOB_ID,
                new Intent(context, DropbitServicePatchService.class));

        //todo remove after all members are past v1.2
        syncWalletManager.cancelAllOldSyncJobs();
    }
}
