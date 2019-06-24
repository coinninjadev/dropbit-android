package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNSubscription
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState
import com.coinninja.coinkeeper.service.client.model.CNTopic
import com.coinninja.coinkeeper.service.client.model.CNTopicSubscription
import com.coinninja.coinkeeper.util.CNLogger
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Response
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class PushNotificationSubscriptionManagerTest {
    companion object {
        private val devicesId: String = "-- device id"
        private val deviceEndpoint: String = "--- device endpoint id"
    }

    fun createSubscriptionManager(): PushNotificationSubscriptionManager {
        val pushNotificationSubscriptionManager = PushNotificationSubscriptionManager(
                mock(SignedCoinKeeperApiClient::class.java),
                mock(CNLogger::class.java))
        mockWalletSubscriptionResponse(pushNotificationSubscriptionManager)
        whenever(pushNotificationSubscriptionManager.apiClient.subscribeToTopics(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Response.success(ArrayList<CNTopic>()))
        return pushNotificationSubscriptionManager
    }

    private fun mockWalletSubscriptionResponse(subscriptionManager: PushNotificationSubscriptionManager) {
        whenever(subscriptionManager.apiClient.subscribeToWalletNotifications(ArgumentMatchers.any())).thenReturn(Response.success(mock(CNSubscription::class.java)))
        whenever(subscriptionManager.apiClient.updateWalletSubscription(ArgumentMatchers.any())).thenReturn(Response.success(mock(CNSubscription::class.java)))
    }

    private fun createSubscriptionState(): CNSubscriptionState {
        val subscriptionState = CNSubscriptionState()
        val topics = ArrayList<CNTopic>()
        val topic = CNTopic()
        topic.id = "--topic id 0"
        topics.add(topic)
        val topic1 = CNTopic()
        topic1.id = "--topic id 1"
        topics.add(topic1)
        subscriptionState.availableTopics = topics
        val subscriptions = ArrayList<CNSubscription>()
        val subscription = CNSubscription()
        subscriptions.add(subscription)
        subscription.ownerId = "--topic id 0"
        subscription.ownerType = "general"
        val sub2 = CNSubscription()
        sub2.ownerId = "--topic id 2"
        sub2.ownerType = "general"
        subscriptions.add(sub2)
        subscriptionState.subscriptions = subscriptions
        return subscriptionState
    }

    @Test
    fun subscribes_to_wallet_topic_when_wallet_not_in_subscription_list() {
        val subscriptionManager = createSubscriptionManager()
        whenever(subscriptionManager.apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
                .thenReturn(Response.success(createSubscriptionState()))

        subscriptionManager.subscribeToChannels(devicesId, deviceEndpoint)

        verify(subscriptionManager.apiClient).subscribeToWalletNotifications(deviceEndpoint)
    }

    @Test
    fun does_not_subscribe_to_wallet_topic_when_wallet_is_in_subscription_list() {
        val subscriptionManager = createSubscriptionManager()
        val subscriptionState = createSubscriptionState()
        subscriptionState.subscriptions[0].ownerType = "Wallet"
        whenever(subscriptionManager.apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
                .thenReturn(Response.success(subscriptionState))

        subscriptionManager.subscribeToChannels(devicesId, deviceEndpoint)

        verify(subscriptionManager.apiClient, times(0)).subscribeToWalletNotifications(ArgumentMatchers.any())
    }

    @Test
    fun subscribes_to_unsubscribed_topics() {
        val subscriptionManager = createSubscriptionManager()
        val subscriptionState = createSubscriptionState()
        subscriptionState.subscriptions[0].ownerType = "Wallet"
        whenever(subscriptionManager.apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
                .thenReturn(Response.success(subscriptionState))

        subscriptionManager.subscribeToChannels(devicesId, deviceEndpoint)

        verify<SignedCoinKeeperApiClient>(subscriptionManager.apiClient)
                .subscribeToTopics(devicesId, deviceEndpoint, CNTopicSubscription(listOf("--topic id 1")))

        verify(subscriptionManager.logger, times(0)).logError(ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any())
    }

    @Test
    fun fetch_all_topics() {
        val subscriptionManager = createSubscriptionManager()
        val subscriptionState = createSubscriptionState()
        whenever(subscriptionManager.apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
                .thenReturn(Response.success(subscriptionState))

        subscriptionManager.subscribeToChannels(devicesId, deviceEndpoint)

        verify(subscriptionManager.apiClient).fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint)
    }

    @Test
    fun logs_failures_when_fetching_subscriptions() {
        val subscriptionManager = createSubscriptionManager()
        val response = Response.error<Any>(500,
                ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(subscriptionManager.apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint)).thenReturn(response)

        subscriptionManager.subscribeToChannels(devicesId, deviceEndpoint)

        verify(subscriptionManager.logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.FETCH_SUBSCRIPTIONS_FAILED, response)
        verify(subscriptionManager.apiClient, times(0)).subscribeToTopics(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.any())
    }

    @Test
    fun logs_failures_when_subscribing_to_topics() {
        val subscriptionManager = createSubscriptionManager()
        val subscription = CNTopicSubscription(topics = listOf("--topic id 1"))
        val response = Response.error<Any>(500,
                ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(subscriptionManager.apiClient.subscribeToTopics(devicesId, deviceEndpoint, subscription)).thenReturn(response)

        subscriptionManager.subscribeToTopics(devicesId, deviceEndpoint, listOf(CNTopic("--topic id 1")))

        verify(subscriptionManager.logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.SUBSCRIBE_FAILED, response)
    }

    @Test
    fun do_not_subscribe_to_topics_when_available_topics_list_is_empty() {
        val subscriptionManager = createSubscriptionManager()

        subscriptionManager.subscribeToTopics(devicesId, deviceEndpoint, ArrayList())

        verify(subscriptionManager.apiClient, times(0)).subscribeToTopics(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    }

    @Test
    fun subscribes_to_wallet_notifications() {
        val subscriptionManager = createSubscriptionManager()
        subscriptionManager.subscribeToWalletNotifications(deviceEndpoint)

        verify(subscriptionManager.apiClient).subscribeToWalletNotifications(deviceEndpoint)
    }

    @Test
    fun logs_error_when_subscribing_wallet_endpoint() {
        val subscriptionManager = createSubscriptionManager()
        val response = Response.error<Any>(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(subscriptionManager.apiClient.subscribeToWalletNotifications(ArgumentMatchers.any())).thenReturn(response)

        subscriptionManager.subscribeToWalletNotifications(deviceEndpoint)

        verify(subscriptionManager.logger).logError(PushNotificationSubscriptionManager.TAG, PushNotificationSubscriptionManager.WALLET_SUBSCRIBE_FAILED, response)
    }

    @Test
    fun on_409_update_the_subscription() {
        val subscriptionManager = createSubscriptionManager()
        whenever(subscriptionManager.apiClient.subscribeToWalletNotifications(ArgumentMatchers.any())).thenReturn(Response.error<Any>(409, ResponseBody.create(MediaType.parse("plain/text"), "")))

        subscriptionManager.subscribeToWalletNotifications(deviceEndpoint)

        verify(subscriptionManager.apiClient).updateWalletSubscription(deviceEndpoint)
        verify(subscriptionManager.logger, times(0)).logError(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    }

    @Test
    fun logs_error_on_update_wallet_subscription() {
        val subscriptionManager = createSubscriptionManager()
        whenever(subscriptionManager.apiClient.subscribeToWalletNotifications(ArgumentMatchers.any())).thenReturn(Response.error<Any>(409, ResponseBody.create(MediaType.parse("plain/text"), "")))
        val response = Response.error<Any>(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(subscriptionManager.apiClient.updateWalletSubscription(ArgumentMatchers.any())).thenReturn(response)

        subscriptionManager.subscribeToWalletNotifications(deviceEndpoint)

        verify(subscriptionManager.logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.UPDATE_WALLET_SUBSCRIBE_FAILED, response)

    }

    @Test
    fun `retrieves subscriptions state`() {
        val subscriptionManager = createSubscriptionManager()
        whenever(subscriptionManager.apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
                .thenReturn(Response.success(createSubscriptionState()))

        assertThat(subscriptionManager.getSubscriptionState(devicesId, deviceEndpoint)).isEqualTo(createSubscriptionState())
    }

    @Test
    fun `does not subscribe to btc_high topic automatically`() {
        val subscriptionManager = createSubscriptionManager()
        val topic = CNTopic()
        topic.id = "--topic id --btc-high"
        topic.name = "btc_high"
        val availableTopics = mutableListOf(topic)

        subscriptionManager.identifyTopics(availableTopics, listOf())

        assertThat(availableTopics.size).isEqualTo(0)
    }

    @Test
    fun `unsubscribe from a topic`() {
        val subscriptionManager = createSubscriptionManager()
        whenever(subscriptionManager.apiClient.unsubscribeFromTopic(devicesId, deviceEndpoint, "--topic-id--"))
                .thenReturn(Response.success(200))

        subscriptionManager.unsubscribeFrom(devicesId, deviceEndpoint, "--topic-id--")

        verify(subscriptionManager.apiClient).unsubscribeFromTopic(devicesId, deviceEndpoint, "--topic-id--")
    }

    @Test
    fun `logs failure when removing a subscription from a topic`() {
        val subscriptionManager = createSubscriptionManager()
        val response = Response.error<Any>(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(subscriptionManager.apiClient.unsubscribeFromTopic(devicesId, deviceEndpoint, "--topic-id--"))
                .thenReturn(response)

        subscriptionManager.unsubscribeFrom(devicesId, deviceEndpoint, "--topic-id--")

        verify(subscriptionManager.logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.UNSUBSCRIBE_FAILED, response)

    }
}