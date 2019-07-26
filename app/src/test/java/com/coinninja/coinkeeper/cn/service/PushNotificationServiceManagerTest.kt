package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.cn.service.testData.YEARLY_HIGH_DATA
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.verify
import org.mockito.internal.verification.VerificationModeFactory.times
import java.util.*

class PushNotificationServiceManagerTest {
    companion object {
        private const val UUID: String = "-uuid-"
        private const val TOKEN: String = "-push_token-"
        private const val CN_DEVICE_ID: String = "158a4cf9-0362-4636-8c68-ed7a98a7f345"
        private const val CN_DEVICE_ENDPOINT_ID: String = "--endpoint id"
    }

    private fun createPushNotificationServiceManager(): PushNotificationServiceManager {
        val pushNotificationServiceManager = PushNotificationServiceManager(
                mock(), mock(), mock(), mock()
        )
        whenever(pushNotificationServiceManager.pushNotificationDeviceManager.deviceId).thenReturn(CN_DEVICE_ID)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.endpoint).thenReturn(CN_DEVICE_ENDPOINT_ID)
        return pushNotificationServiceManager
    }

    @Test
    fun on_save_token_clear_cn_device_endpoint() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()

        pushNotificationServiceManager.saveToken(TOKEN)

        verify(pushNotificationServiceManager.pushNotificationTokenManager).saveToken(TOKEN)
        verify(pushNotificationServiceManager.pushNotificationEndpointManager).removeEndpoint()
    }

    @Test
    fun registers_device() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationDeviceManager.createDevice(UUID)).thenReturn(true)

        assertTrue(pushNotificationServiceManager.registerDevice(UUID))
    }

    @Test
    fun `is registered device when device id is present`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationDeviceManager.deviceId)
                .thenReturn("--device-id--")

        assertTrue(pushNotificationServiceManager.isRegisteredDevice())
    }

    // Register Endpoint with Coinninja

    @Test
    fun register_for_endpoint_when_local_is_absent() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        pushNotificationServiceManager.registerAsEndpoint()

        verify(pushNotificationServiceManager.pushNotificationEndpointManager).registersAsEndpoint()
    }

    @Test
    fun does_not_registers_endpoints_when_local_and_remotes_match() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        val endpoints = ArrayList<CNDeviceEndpoint>()
        val endpoint = CNDeviceEndpoint()
        endpoint.id = CN_DEVICE_ENDPOINT_ID
        endpoints.add(endpoint)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.fetchEndpoints()).thenReturn(endpoints)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.endpoint).thenReturn(CN_DEVICE_ENDPOINT_ID)

        pushNotificationServiceManager.registerAsEndpoint()

        verify(pushNotificationServiceManager.pushNotificationEndpointManager).fetchEndpoints()
        verify(pushNotificationServiceManager.pushNotificationEndpointManager, times(0)).registersAsEndpoint()
    }

    @Test
    fun unregisters_remote_endpoints_that_do_not_match_local() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        val endpointToUnregister = "-- deadend"
        val endpoints = ArrayList<CNDeviceEndpoint>()
        val endpoint = CNDeviceEndpoint()
        endpoint.id = CN_DEVICE_ENDPOINT_ID
        endpoints.add(endpoint)
        val endpoint2 = CNDeviceEndpoint()
        endpoint2.id = endpointToUnregister
        endpoints.add(endpoint2)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.fetchEndpoints()).thenReturn(endpoints)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.endpoint).thenReturn(CN_DEVICE_ENDPOINT_ID)

        pushNotificationServiceManager.registerAsEndpoint()

        verify(pushNotificationServiceManager.pushNotificationEndpointManager).unRegister(endpointToUnregister)
        verify(pushNotificationServiceManager.pushNotificationEndpointManager, times(0)).unRegister(CN_DEVICE_ENDPOINT_ID)
        verify(pushNotificationServiceManager.pushNotificationEndpointManager, times(0)).registersAsEndpoint()
    }

    @Test
    fun remove_local_endpoint_when_not_matching_remote() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        val endpointToUnregister = "-- deadend"
        val endpoints = ArrayList<CNDeviceEndpoint>()
        val endpoint = CNDeviceEndpoint()
        endpoint.id = "--- deadend 2"
        endpoints.add(endpoint)
        val endpoint2 = CNDeviceEndpoint()
        endpoint.id = endpointToUnregister
        endpoints.add(endpoint2)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.fetchEndpoints()).thenReturn(endpoints)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.endpoint).thenReturn(CN_DEVICE_ENDPOINT_ID)

        pushNotificationServiceManager.registerAsEndpoint()

        verify(pushNotificationServiceManager.pushNotificationEndpointManager).removeEndpoint()
        verify(pushNotificationServiceManager.pushNotificationEndpointManager).registersAsEndpoint()
    }

    @Test
    fun subscribes_to_subscriptions_for_device_endpoint() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        pushNotificationServiceManager.subscribeToChannels()

        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager).subscribeToChannels(CN_DEVICE_ID, CN_DEVICE_ENDPOINT_ID)
    }

    @Test
    fun does_not_subscribes_to_subscriptions_given_no_endpoint() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)

        pushNotificationServiceManager.subscribeToChannels()

        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager, times(0))
                .subscribeToChannels(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun `provides access to subscription state`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        val subscriptionState = Gson().fromJson(YEARLY_HIGH_DATA.NOT_SUBSCRIBED_TO_YEARLY_HIGH, CNSubscriptionState::class.java)
        whenever(pushNotificationServiceManager.pushNotificationSubscriptionManager.getSubscriptionState(CN_DEVICE_ID, CN_DEVICE_ENDPOINT_ID)).thenReturn(subscriptionState)
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        assertThat(pushNotificationServiceManager.getSubscriptionState()).isEqualTo(subscriptionState)
    }

    @Test
    fun `provides proxy to subscribe to a topic`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        pushNotificationServiceManager.subscribeTo("--topic-id--")

        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager)
                .subscribeToTopicIds(deviceId = CN_DEVICE_ID, deviceEndpointId = CN_DEVICE_ENDPOINT_ID, topics = listOf("--topic-id--"))
    }

    @Test
    fun `create register as endpoint when endpoint does not exist`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)
                .thenReturn(false).thenReturn(true)

        pushNotificationServiceManager.subscribeTo("--topic-id--")

        verify(pushNotificationServiceManager.pushNotificationEndpointManager).registersAsEndpoint()
        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager).subscribeToChannels(CN_DEVICE_ID, CN_DEVICE_ENDPOINT_ID)
        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager)
                .subscribeToTopicIds(deviceId = CN_DEVICE_ID, deviceEndpointId = CN_DEVICE_ENDPOINT_ID, topics = listOf("--topic-id--"))
    }

    @Test
    fun `provides a proxy to unsubscribe from a topic`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        pushNotificationServiceManager.unsubscribeFrom("--topic-id--")

        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager)
                .unsubscribeFrom(deviceId = CN_DEVICE_ID, deviceEndpointId = CN_DEVICE_ENDPOINT_ID, topic = "--topic-id--")
    }

    @Test
    fun `only provides a proxy to unsubscribe from a topic when endpoint exists`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)

        pushNotificationServiceManager.unsubscribeFrom("--topic-id--")

        verify(pushNotificationServiceManager.pushNotificationSubscriptionManager, times(0))
                .unsubscribeFrom(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
    }

    @Test
    fun `verify token request token manager to ensure presence of a token`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        val observer: PushTokenVerifiedObserver = mock()

        pushNotificationServiceManager.acquireToken(observer)

        verify(pushNotificationServiceManager.pushNotificationTokenManager).retrieveTokenIfNecessary(observer)
    }

    @Test
    fun `proxies knowledge of registered as endpoint`() {
        val pushNotificationServiceManager = createPushNotificationServiceManager()
        whenever(pushNotificationServiceManager.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        assertTrue(pushNotificationServiceManager.isRegisteredEndpoint())


    }
}