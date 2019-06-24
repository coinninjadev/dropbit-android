package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.core.app.JobIntentService
import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager
import com.coinninja.coinkeeper.cn.service.YearlyHighSubscription
import dagger.android.AndroidInjection
import javax.inject.Inject

class PushNotificationEndpointRegistrationService : JobIntentService() {
    @Inject
    internal lateinit var pushNotificationServiceManager: PushNotificationServiceManager
    @Inject
    internal lateinit var yearlyHighSubscription: YearlyHighSubscription

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleWork(intent: Intent) {
        pushNotificationServiceManager.verifyToken()

        if (!pushNotificationServiceManager.isRegisteredEndpoint()) {
            pushNotificationServiceManager.registerAsEndpoint()
            yearlyHighSubscription.subscribe()
        }

        pushNotificationServiceManager.subscribeToChannels()
    }
}
