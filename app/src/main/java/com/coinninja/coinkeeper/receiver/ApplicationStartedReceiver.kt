package com.coinninja.coinkeeper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import dagger.android.AndroidInjection
import javax.inject.Inject

class ApplicationStartedReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var jobServiceScheduler: JobServiceScheduler

    @Inject
    internal lateinit var syncWalletManager: SyncWalletManager

    @Inject
    internal lateinit var analytics: Analytics

    @Inject
    internal lateinit var cnWalletManager: CNWalletManager

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        analytics.setUserProperty(Analytics.PROPERTY_PLATFORM, Analytics.OS)
        if (!cnWalletManager.hasWallet) return
        jobServiceScheduler.enqueueWork(
                context,
                PushNotificationEndpointRegistrationService::class.java,
                JobServiceScheduler.ENDPOINT_REGISTRATION_SERVICE_JOB_ID,
                Intent(context, PushNotificationEndpointRegistrationService::class.java))
        //todo remove after all members are past v1.2
        syncWalletManager.cancelAllOldSyncJobs()
    }
}
