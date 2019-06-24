package com.coinninja.coinkeeper.cn.service

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNSubscription
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState
import com.coinninja.coinkeeper.service.client.model.CNTopic
import com.coinninja.coinkeeper.service.client.model.CNTopicSubscription
import com.coinninja.coinkeeper.util.CNLogger
import javax.inject.Inject

@Mockable
class PushNotificationSubscriptionManager @Inject internal constructor(
        internal val apiClient: SignedCoinKeeperApiClient,
        internal val logger: CNLogger) {

    fun subscribeToChannels(deviceId: String, deviceEndpointId: String) {
        val subscriptionState = getSubscriptionState(deviceId, deviceEndpointId)
        val availableTopics = subscriptionState.availableTopics
        val subscriptions = subscriptionState.subscriptions
        identifyTopics(availableTopics, subscriptions)
        subscribeToTopics(deviceId, deviceEndpointId, availableTopics)

        if (!hasWalletSubscription(subscriptions))
            subscribeToWalletNotifications(deviceEndpointId)
    }

    private fun hasWalletSubscription(subscriptions: List<CNSubscription>): Boolean {
        var hasSubscription = false
        for (cnSubscription in subscriptions) {
            if ("Wallet" == cnSubscription.ownerType) {
                hasSubscription = true
            }
        }
        return hasSubscription
    }

    internal fun identifyTopics(availableTopics: MutableList<CNTopic>, subscriptions: List<CNSubscription>) {
        btc_high@ for (x in 0 until availableTopics.size) {
            val topic = availableTopics[x]
            if (topic.name == "btc_high") {
                availableTopics.remove(topic)
                continue@btc_high
            }
        }

        for (subscription in subscriptions) {
            availableTopics.remove(CNTopic(subscription.ownerId))
        }

    }

    internal fun subscribeToTopics(deviceId: String, deviceEndpointId: String, availableTopics: List<CNTopic>) {
        if (availableTopics.isEmpty()) return
        val topics = mutableListOf<String>()

        availableTopics.forEach {
            topics.add(it.id)
        }

        subscribeToTopicIds(deviceId, deviceEndpointId, topics)

    }

    internal fun subscribeToTopicIds(deviceId: String, deviceEndpointId: String, topics: List<String>) {
        val response = apiClient.subscribeToTopics(deviceId, deviceEndpointId,
                CNTopicSubscription(topics = topics))
        if (!response.isSuccessful) {
            logger.logError(TAG, SUBSCRIBE_FAILED, response)
        }
    }

    internal fun unsubscribeFrom(deviceId: String, deviceEndpointId: String, topic: String) {
        val response = apiClient.unsubscribeFromTopic(deviceId, deviceEndpointId, topic)
        if (!response.isSuccessful) {
            logger.logError(TAG, UNSUBSCRIBE_FAILED, response)
        }
    }

    internal fun subscribeToWalletNotifications(deviceEndpoint: String) {
        val response = apiClient.subscribeToWalletNotifications(deviceEndpoint)
        if (response.code() == 409) {
            updateWalletSubscriptionForEndpoint(deviceEndpoint)
        } else if (!response.isSuccessful) {
            logger.logError(TAG, WALLET_SUBSCRIBE_FAILED, response)
        }
    }

    private fun updateWalletSubscriptionForEndpoint(deviceEndpoint: String) {
        val response = apiClient.updateWalletSubscription(deviceEndpoint)
        if (!response.isSuccessful) {
            logger.logError(TAG, UPDATE_WALLET_SUBSCRIBE_FAILED, response)
        }
    }

    internal fun getSubscriptionState(deviceId: String, deviceEndpointId: String): CNSubscriptionState {
        val response = apiClient.fetchDeviceEndpointSubscriptions(deviceId, deviceEndpointId)
        if (response.isSuccessful) {
            return response.body() as CNSubscriptionState
        } else {
            logger.logError(TAG, FETCH_SUBSCRIPTIONS_FAILED, response)
            return CNSubscriptionState()
        }
    }

    companion object {
        val TAG = PushNotificationSubscriptionManager::class.java.simpleName
        internal val FETCH_SUBSCRIPTIONS_FAILED = "-- Fetch Subscriptions Failed"
        internal val SUBSCRIBE_FAILED = "-- Subscribing To Topics Failed"
        internal val WALLET_SUBSCRIBE_FAILED = "-- Subscribing To Wallet Failed"
        internal val UPDATE_WALLET_SUBSCRIBE_FAILED = "-- Updating Wallet Subscription Failed"
        internal val UNSUBSCRIBE_FAILED = "-- UnSubscribe from topic Failed"
    }
}
