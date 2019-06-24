package com.coinninja.coinkeeper.cn.service

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState
import javax.inject.Inject

@Mockable
class PushNotificationServiceManager @Inject
internal constructor(internal val pushNotificationTokenManager: PushNotificationTokenManager,
                     internal val pushNotificationDeviceManager: PushNotificationDeviceManager,
                     internal val pushNotificationEndpointManager: PushNotificationEndpointManager,
                     internal val pushNotificationSubscriptionManager: PushNotificationSubscriptionManager) {


    fun saveToken(token: String) {
        pushNotificationTokenManager.saveToken(token)
        pushNotificationEndpointManager.removeEndpoint()
    }

    fun registerDevice(uuid: String) {
        if (pushNotificationDeviceManager.createDevice(uuid)) {
            pushNotificationEndpointManager.registersAsEndpoint(pushNotificationDeviceManager.deviceId)
        }
    }

    fun registerAsEndpoint() {
        if (pushNotificationEndpointManager.hasEndpoint()) {
            val endpoint = pushNotificationEndpointManager.endpoint
            var matched = false
            for (cnDeviceEndpoint in pushNotificationEndpointManager.fetchEndpoints()) {
                if (endpoint == cnDeviceEndpoint.id) {
                    matched = true
                } else {
                    pushNotificationEndpointManager.unRegister(cnDeviceEndpoint.id)
                }
            }
            if (!matched) {
                pushNotificationEndpointManager.removeEndpoint()
                pushNotificationEndpointManager.registersAsEndpoint()
            }
        } else {
            pushNotificationEndpointManager.registersAsEndpoint()
        }

    }

    fun subscribeToChannels() {
        if (pushNotificationEndpointManager.hasEndpoint()) {
            pushNotificationSubscriptionManager
                    .subscribeToChannels(pushNotificationDeviceManager.deviceId,
                            pushNotificationEndpointManager.endpoint)
        }
    }

    fun getSubscriptionState(): CNSubscriptionState? {
        if (pushNotificationEndpointManager.hasEndpoint()) {
            return pushNotificationSubscriptionManager.getSubscriptionState(pushNotificationDeviceManager.deviceId,
                    pushNotificationEndpointManager.endpoint)
        }
        return null
    }

    fun unsubscribeFrom(topicId: String) {
        if (pushNotificationEndpointManager.hasEndpoint()) {
            pushNotificationSubscriptionManager.unsubscribeFrom(
                    pushNotificationDeviceManager.deviceId,
                    pushNotificationEndpointManager.endpoint,
                    topicId)
        }
    }

    fun subscribeTo(topicId: String) {
        if (!pushNotificationEndpointManager.hasEndpoint()) {
            registerAsEndpoint()
            subscribeToChannels()
        }

        pushNotificationSubscriptionManager.subscribeToTopicIds(
                pushNotificationDeviceManager.deviceId,
                pushNotificationEndpointManager.endpoint,
                listOf(topicId)
        )
    }

    fun verifyToken() {
        pushNotificationTokenManager.retrieveTokenIfNecessary()
    }

    fun isRegisteredEndpoint(): Boolean {
        return pushNotificationEndpointManager.hasEndpoint()
    }
}
