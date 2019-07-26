package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertTrue
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.internal.verification.VerificationModeFactory.times
import retrofit2.Response
import java.util.*

class PushNotificationEndpointManagerTest {

    private companion object {
        private const val CN_DEVICE_ID: String = "158a4cf9-0362-4636-8c68-ed7a98a7f345"
        private const val TOKEN: String = "-push_token-"
        private const val ENDPOINT_ID: String = "-- device_endpoint_id"
    }

    private val cnDeviceEndpoint = CNDeviceEndpoint("-- device_endpoint_id")

    private fun createEndpointManager(): PushNotificationEndpointManager {
        val manager = PushNotificationEndpointManager(mock(), mock(), mock(), mock(), mock(), mock())
        whenever(manager.pushNotificationTokenManager.token).thenReturn(TOKEN)
        whenever(manager.pushNotificationDeviceManager.deviceId).thenReturn(CN_DEVICE_ID)

        val response = Response.success(cnDeviceEndpoint)
        whenever(manager.apiClient.registerForPushEndpoint(CN_DEVICE_ID, TOKEN)).thenReturn(response)
        return manager
    }

    @Test
    fun removes_endpoint_locally() {
        val pushNotificationEndpointManager = createEndpointManager()

        pushNotificationEndpointManager.removeEndpoint()

        verify(pushNotificationEndpointManager.preferencesUtil)
                .removePreference(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID)
    }

    @Test
    fun registers_device_endpoint_with_coinninja_using_cached_device_id() {
        val pushNotificationEndpointManager = createEndpointManager()

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID)

        verify(pushNotificationEndpointManager.apiClient).registerForPushEndpoint(CN_DEVICE_ID, TOKEN)
    }

    @Test
    fun saves_device_endpoint_once_created() {
        val pushNotificationEndpointManager = createEndpointManager()
        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID)

        verify(pushNotificationEndpointManager.preferencesUtil).savePreference(
                PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, cnDeviceEndpoint.id)
    }

    @Test
    fun log_error_when_fail_to_create_endpoint() {
        val pushNotificationEndpointManager = createEndpointManager()
        val responseBody = ResponseBody.create(MediaType.parse("text"), "")
        val response = Response.error<Any>(400, responseBody)
        whenever(pushNotificationEndpointManager.apiClient.registerForPushEndpoint(any(), any())).thenReturn(response)

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID)

        verify(pushNotificationEndpointManager.logger).logError(PushNotificationEndpointManager.TAG,
                PushNotificationEndpointManager.DEVICE_ENDPOINT_ERROR_MESSAGE, response)
    }

    @Test
    fun saves_endpoint_when_registering_device_endpoint_with_device_id_and_token() {
        val pushNotificationEndpointManager = createEndpointManager()

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID, TOKEN)

        verify(pushNotificationEndpointManager.preferencesUtil)
                .savePreference(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID,
                        cnDeviceEndpoint.id)
    }

    @Test
    fun log_error_when_registering_device_endpoint_with_cached_values() {
        val pushNotificationEndpointManager = createEndpointManager()
        val responseBody = ResponseBody.create(MediaType.parse("text"), "")
        val response = Response.error<Any>(400, responseBody)
        whenever(pushNotificationEndpointManager.apiClient.registerForPushEndpoint(any(), any())).thenReturn(response)

        pushNotificationEndpointManager.registersAsEndpoint(CN_DEVICE_ID, TOKEN)

        verify(pushNotificationEndpointManager.logger).logError(PushNotificationEndpointManager.TAG,
                PushNotificationEndpointManager.DEVICE_ENDPOINT_ERROR_MESSAGE, response)
    }

    @Test
    fun saves_endpoint_when_registering_device_endpoint_with_cached_values() {
        val pushNotificationEndpointManager = createEndpointManager()
        pushNotificationEndpointManager.registersAsEndpoint()

        verify(pushNotificationEndpointManager.apiClient).registerForPushEndpoint(CN_DEVICE_ID, TOKEN)
    }

    @Test
    fun subscribes_to_all_available_topics_when_registering_as_endpoint() {
        val pushNotificationEndpointManager = createEndpointManager()
        pushNotificationEndpointManager.registersAsEndpoint()

        verify(pushNotificationEndpointManager.pushNotificationSubscriptionManager).subscribeToChannels(CN_DEVICE_ID, ENDPOINT_ID)
    }

    @Test
    fun does_not_register_a_device_endpoint_with_coinninja_when_device_id_not_cached() {
        val pushNotificationEndpointManager = createEndpointManager()

        whenever(pushNotificationEndpointManager.pushNotificationDeviceManager.deviceId).thenReturn("")
        pushNotificationEndpointManager.registersAsEndpoint()


        verify(pushNotificationEndpointManager.apiClient, times(0))
                .registerForPushEndpoint(any(), any())
    }

    @Test
    fun does_not_register_a_device_endpoint_with_coinninja_when_token_not_cached() {
        val pushNotificationEndpointManager = createEndpointManager()

        whenever(pushNotificationEndpointManager.pushNotificationTokenManager.token).thenReturn("")
        pushNotificationEndpointManager.registersAsEndpoint()


        verify(pushNotificationEndpointManager.apiClient, times(0))
                .registerForPushEndpoint(any(), any())
    }

    // ENDPOINT VERIFICATION


    @Test
    fun returns_true_when_endpoint_is_cached() {
        val pushNotificationEndpointManager = createEndpointManager()
        whenever(pushNotificationEndpointManager.preferencesUtil
                .contains(PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID))
                .thenReturn(true)

        assertTrue(pushNotificationEndpointManager.hasEndpoint())
    }

    @Test
    fun returns_saved_endpoint() {
        val pushNotificationEndpointManager = createEndpointManager()
        val endpoint = "--endpoint"
        whenever(pushNotificationEndpointManager.preferencesUtil.getString(
                PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, ""))
                .thenReturn(endpoint)

        assertThat(pushNotificationEndpointManager.endpoint).isEqualTo(endpoint)
    }

    @Test
    fun provides_list_of_endpoints_from_the_server() {
        val pushNotificationEndpointManager = createEndpointManager()
        val endpoints = ArrayList<CNDeviceEndpoint>()
        endpoints.add(CNDeviceEndpoint("--id--"))
        whenever(pushNotificationEndpointManager.apiClient.fetchRemoteEndpointsFor(CN_DEVICE_ID))
                .thenReturn(generateSuccessResponse(200, endpoints))

        val fetchedEndpoints: List<CNDeviceEndpoint> = pushNotificationEndpointManager.fetchEndpoints()

        assertThat(fetchedEndpoints).isEqualTo(endpoints)
    }

    @Test
    fun provides_empty_list_of_endpoints_from_the_server_on_error() {
        val pushNotificationEndpointManager = createEndpointManager()
        val response = Response.error<Any>(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(pushNotificationEndpointManager.apiClient.fetchRemoteEndpointsFor(CN_DEVICE_ID)).thenReturn(
                response)


        val fetchedEndpoints: List<CNDeviceEndpoint> = pushNotificationEndpointManager.fetchEndpoints()

        assertThat(fetchedEndpoints).isEqualTo(emptyList<CNDeviceEndpoint>())
        verify(pushNotificationEndpointManager.logger).logError(PushNotificationEndpointManager.TAG,
                PushNotificationEndpointManager.FETCH_DEVICE_ENDPOINT_ERROR_MESSAGE, response)
    }

    @Test
    fun logs_unregister_error() {
        val pushNotificationEndpointManager = createEndpointManager()
        val endpoint = "--endpoint"
        val response = Response.error<Any>(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(pushNotificationEndpointManager.apiClient.unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)).thenReturn(response)

        pushNotificationEndpointManager.unRegister(endpoint)
        verify(pushNotificationEndpointManager.logger).logError(PushNotificationEndpointManager.TAG,
                PushNotificationEndpointManager.UNREGISTER_DEVICE_ENDPOINT_ERROR_MESSAGE, response)
    }

    @Test
    fun unregisters_endpoint() {
        val pushNotificationEndpointManager = createEndpointManager()
        val endpoint = "--endpoint"
        whenever(pushNotificationEndpointManager.preferencesUtil.getString(
                PushNotificationEndpointManager.PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, ""))
                .thenReturn(endpoint)
        whenever(pushNotificationEndpointManager.apiClient.unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint))
                .thenReturn(generateSuccessResponse(200, ""))

        pushNotificationEndpointManager.unRegister()

        verify(pushNotificationEndpointManager.apiClient).unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)
    }

    @Test
    fun unregisters_provided_endpoint() {
        val pushNotificationEndpointManager = createEndpointManager()
        val endpoint = "--endpoint"
        whenever(pushNotificationEndpointManager.apiClient.unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)).thenReturn(generateSuccessResponse(200, ""))

        pushNotificationEndpointManager.unRegister(endpoint)

        verify(pushNotificationEndpointManager.apiClient).unRegisterDeviceEndpoint(CN_DEVICE_ID, endpoint)
    }

    private fun generateSuccessResponse(responseCode: Int, responseData: Any): Response<*> {
        return Response.success(responseData, okhttp3.Response.Builder()
                .code(responseCode)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }
}