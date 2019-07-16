package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.core.app.JobIntentService
import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager
import com.coinninja.coinkeeper.cn.service.PushTokenVerifiedObserver
import com.coinninja.coinkeeper.cn.service.YearlyHighSubscription
import com.coinninja.coinkeeper.di.interfaces.UUID
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import dagger.android.AndroidInjection
import javax.inject.Inject

class PushNotificationEndpointRegistrationService : JobIntentService() {
    @Inject
    internal lateinit var pushNotificationServiceManager: PushNotificationServiceManager
    @Inject
    internal lateinit var yearlyHighSubscription: YearlyHighSubscription
    @Inject
    internal lateinit var jobServiceScheduler: JobServiceScheduler

    @Inject
    @field:UUID
    internal lateinit var uuid: String

    internal val pushTokenVerifiedObserver: PushTokenVerifiedObserver = object : PushTokenVerifiedObserver {
        override fun onTokenAcquired(token: String) {
            jobServiceScheduler.enqueueWork(
                    applicationContext,
                    PushNotificationEndpointRegistrationService::class.java,
                    JobServiceScheduler.ENDPOINT_REGISTRATION_SERVICE_JOB_ID,
                    Intent(applicationContext, PushNotificationEndpointRegistrationService::class.java))
        }
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleWork(intent: Intent) {
        if (pushNotificationServiceManager.hasPushToken()) {
            if (!pushNotificationServiceManager.isRegisteredDevice()) {
                pushNotificationServiceManager.registerDevice(uuid)
            }

            if (!pushNotificationServiceManager.isRegisteredEndpoint()) {
                pushNotificationServiceManager.registerAsEndpoint()
                yearlyHighSubscription.subscribe()
            }
            
            pushNotificationServiceManager.subscribeToChannels()
        } else {
            pushNotificationServiceManager.acquireToken(observer = pushTokenVerifiedObserver)
        }
    }
}
