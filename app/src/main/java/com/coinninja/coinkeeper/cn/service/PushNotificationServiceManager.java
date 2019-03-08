package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;

import javax.inject.Inject;

public class PushNotificationServiceManager {
    private PushNotificationTokenManager pushNotificationTokenManager;
    private PushNotificationDeviceManager pushNotificationDeviceManager;
    private PushNotificationEndpointManager pushNotificationEndpointManager;
    private PushNotificationSubscriptionManager pushNotificationSubscriptionManager;

    @Inject
    PushNotificationServiceManager(PushNotificationTokenManager pushNotificationTokenManager,
                                   PushNotificationDeviceManager pushNotificationDeviceManager,
                                   PushNotificationEndpointManager pushNotificationEndpointManager,
                                   PushNotificationSubscriptionManager pushNotificationSubscriptionManager) {
        this.pushNotificationTokenManager = pushNotificationTokenManager;
        this.pushNotificationDeviceManager = pushNotificationDeviceManager;
        this.pushNotificationEndpointManager = pushNotificationEndpointManager;
        this.pushNotificationSubscriptionManager = pushNotificationSubscriptionManager;
    }


    public void saveToken(String token) {
        pushNotificationTokenManager.saveToken(token);
        pushNotificationEndpointManager.removeEndpoint();
    }

    public void registerDevice(String uuid) {
        if (pushNotificationDeviceManager.createDevice(uuid)) {
            pushNotificationEndpointManager.registersAsEndpoint(pushNotificationDeviceManager.getDeviceId());
        }
    }

    public void registerAsEndpoint() {
        if (pushNotificationEndpointManager.hasEndpoint()) {
            String endpoint = pushNotificationEndpointManager.getEndpoint();
            boolean matched = false;
            for (CNDeviceEndpoint cnDeviceEndpoint : pushNotificationEndpointManager.fetchEndpoints()) {
                if (endpoint.equals(cnDeviceEndpoint.getID())) {
                    matched = true;
                } else {
                    pushNotificationEndpointManager.unRegister(cnDeviceEndpoint.getID());
                }
            }
            if (!matched) {
                pushNotificationEndpointManager.removeEndpoint();
                pushNotificationEndpointManager.registersAsEndpoint();
            }
        } else {
            pushNotificationEndpointManager.registersAsEndpoint();
        }

    }

    public void subscribeToChannels() {
        if (pushNotificationEndpointManager.hasEndpoint()) {
            pushNotificationSubscriptionManager
                    .subscribeToChannels(pushNotificationDeviceManager.getDeviceId(),
                            pushNotificationEndpointManager.getEndpoint());
        }
    }
}
