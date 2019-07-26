package com.coinninja.coinkeeper.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import dagger.android.AndroidInjection
import javax.inject.Inject

class WalletCreatedBroadCastReceiver : BroadcastReceiver() {

    @Inject
    internal lateinit var analytics: Analytics

    @Inject
    internal lateinit var syncWalletManager: SyncWalletManager

    @Inject
    internal lateinit var jobServiceScheduler: JobServiceScheduler

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET, true)
        syncWalletManager.schedule60SecondSync()
        syncWalletManager.syncNow()
        syncWalletManager.scheduleHourlySync()
        jobServiceScheduler.enqueueWork(
                context,
                PushNotificationEndpointRegistrationService::class.java,
                JobServiceScheduler.ENDPOINT_REGISTRATION_SERVICE_JOB_ID,
                Intent(context, PushNotificationEndpointRegistrationService::class.java))
    }
}
