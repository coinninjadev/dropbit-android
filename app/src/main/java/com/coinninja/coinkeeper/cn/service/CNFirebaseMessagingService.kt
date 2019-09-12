package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.util.CNLogger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import javax.inject.Inject

class CNFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    internal lateinit var pushNotificationServiceManager: PushNotificationServiceManager
    @Inject
    internal lateinit var logger: CNLogger

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushNotificationServiceManager.saveToken(token)
    }

    override fun onMessageSent(s: String) {
        logger.debug("----- firebase messenger", "----- message sent")
        super.onMessageSent(s)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.debug("----- firebase messenger", "----- received message")
        super.onMessageReceived(remoteMessage)
    }
}