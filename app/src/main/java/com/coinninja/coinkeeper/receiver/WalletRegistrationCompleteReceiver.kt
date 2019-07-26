package com.coinninja.coinkeeper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import dagger.android.AndroidInjection
import javax.inject.Inject

class WalletRegistrationCompleteReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var jobServiceScheduler: JobServiceScheduler

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)

        jobServiceScheduler.enqueueWork(
                context,
                CNGlobalMessagingService::class.java,
                JobServiceScheduler.GLOBAL_MESSAGING_SERVICE_JOB_ID,
                Intent(context, CNGlobalMessagingService::class.java)
        )
    }
}