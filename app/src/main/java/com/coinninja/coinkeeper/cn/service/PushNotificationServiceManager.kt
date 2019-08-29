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

    fun hasPushToken(): Boolean  = pushNotificationTokenManager.token.isNotEmpty()
    fun acquireToken(observer: PushTokenVerifiedObserver? = null) = pushNotificationTokenManager.retrieveTokenIfNecessary(observer)
    fun isRegisteredEndpoint(): Boolean = pushNotificationEndpointManager.hasEndpoint()
    fun isRegisteredDevice(): Boolean = pushNotificationDeviceManager.deviceId.isNotEmpty()
    fun registerDevice(uuid: String): Boolean = pushNotificationDeviceManager.createDevice(uuid)
    fun removeAllDeviceEndpoints() = pushNotificationEndpointManager.removeAllRemoteEndpointsForDevice()
}
