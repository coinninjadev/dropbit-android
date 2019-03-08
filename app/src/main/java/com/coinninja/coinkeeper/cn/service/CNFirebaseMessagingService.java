package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.util.CNLogger;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class CNFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    PushNotificationServiceManager pushNotificationServiceManager;

    @Inject
    CNLogger logger;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        pushNotificationServiceManager.saveToken(token);
    }


    @Override
    public void onMessageSent(String s) {
        logger.debug("----- firebase messenger", "----- message sent");
        super.onMessageSent(s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        logger.debug("----- firebase messenger", "----- received message");
        super.onMessageReceived(remoteMessage);
    }
}
