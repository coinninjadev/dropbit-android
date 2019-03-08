package com.coinninja.coinkeeper.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService;
import com.coinninja.coinkeeper.service.DeviceRegistrationService;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class WalletRegistrationCompleteReceiver extends BroadcastReceiver {

    @Inject
    JobServiceScheduler jobServiceScheduler;

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidInjection.inject(this, context);
        jobServiceScheduler.enqueueWork(
                context,
                DeviceRegistrationService.class,
                JobServiceScheduler.DEVICE_REGISTRATION_SERVICE_JOB_ID,
                new Intent(context, DeviceRegistrationService.class));

        jobServiceScheduler.enqueueWork(
                context,
                CNGlobalMessagingService.class,
                JobServiceScheduler.GLOBAL_MESSAGING_SERVICE_JOB_ID,
                new Intent(context, CNGlobalMessagingService.class)
        );
    }
}
