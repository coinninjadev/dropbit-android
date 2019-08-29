package com.coinninja.coinkeeper.cn.service

import com.coinninja.coinkeeper.service.client.model.CNDevice
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.internal.verification.VerificationModeFactory.times
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
class PushNotificationDeviceManagerTest {
    companion object {
        private const val CN_DEVICE_ID: String = "158a4cf9-0362-4636-8c68-ed7a98a7f345"
        private const val JSON: String = "{\n" +
                "  \"id\": \"158a4cf9-0362-4636-8c68-ed7a98a7f345\",\n" +
                "  \"created_at\": 1531921356,\n" +
                "  \"updated_at\": 1531921355,\n" +
                "  \"application\": \"DropBit\",\n" +
                "  \"platform\": \"android\",\n" +
                "  \"uuid\": \"998207d6-5b1e-47c9-84e9-895f52a1b455\"\n" +
                "}"

        private const val UUID: String = "-uuid-"

    }

    internal val device: CNDevice get() = Gson().fromJson(JSON, CNDevice::class.java)

    fun createManager(): PushNotificationDeviceManager {
        val manager = PushNotificationDeviceManager(mock(), mock(), mock())
        whenever(manager.apiClient.createCNDevice(UUID)).thenReturn(Response.success(device))
        return manager
    }

    @Test
    fun saves_server_device_id() {
        val id = "158a4cf9-0362-4636-8c68-ed7a98a7f345"
        val device = CNDevice()
        device.id = id
        val pushNotificationDeviceManager = createManager()

        pushNotificationDeviceManager.saveDevice(device)

        verify(pushNotificationDeviceManager.preferencesUtil)
                .savePreference(PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID, id)
    }

    @Test
    fun provides_device_id() {
        val pushNotificationDeviceManager = createManager()
        whenever(pushNotificationDeviceManager.preferencesUtil.getString(
                PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID, ""))
                .thenReturn(CN_DEVICE_ID)

        assertThat(pushNotificationDeviceManager.deviceId, equalTo(CN_DEVICE_ID))
    }

    @Test
    fun registers_device_with_coin_ninja() {
        val pushNotificationDeviceManager = createManager()
        pushNotificationDeviceManager.createDevice(UUID)

        verify(pushNotificationDeviceManager.apiClient).createCNDevice(UUID)
    }

    @Test
    fun saves_device_registration_on_success_code_of_200() {
        val pushNotificationDeviceManager = createManager()
        whenever(pushNotificationDeviceManager.apiClient.createCNDevice(UUID)).thenReturn(generateSuccessResponse(200, device))

        pushNotificationDeviceManager.createDevice(UUID)

        verify(pushNotificationDeviceManager.preferencesUtil).savePreference(
                PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID,
                CN_DEVICE_ID)
    }

    @Test
    fun saves_device_registration_on_success_code_of_201() {
        val pushNotificationDeviceManager = createManager()
        whenever(pushNotificationDeviceManager.apiClient.createCNDevice(UUID)).thenReturn(generateSuccessResponse(201, device))

        pushNotificationDeviceManager.createDevice(UUID)

        verify(pushNotificationDeviceManager.preferencesUtil).savePreference(
                PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID,
                CN_DEVICE_ID)
    }

    @Test
    fun returns_true_when_device_created() {
        val pushNotificationDeviceManager = createManager()
        whenever(pushNotificationDeviceManager.apiClient.createCNDevice(UUID)).thenReturn(generateSuccessResponse(201, device))

        assertTrue(pushNotificationDeviceManager.createDevice(UUID))
    }

    @Test
    fun logs_unsuccessful_responses() {
        val pushNotificationDeviceManager = createManager()
        val body = ResponseBody.create(MediaType.parse("text"), "der")
        val response = Response.error<Any>(500, body)
        whenever(pushNotificationDeviceManager.apiClient.createCNDevice(UUID)).thenReturn(response as Response<CNDevice>)

        pushNotificationDeviceManager.createDevice(UUID)

        verify(pushNotificationDeviceManager.preferencesUtil, times(0))
                .savePreference(any(), any<String>())

        verify(pushNotificationDeviceManager.logger).logError(PushNotificationDeviceManager.TAG,
                PushNotificationDeviceManager.DEVICE_REGISTRATION_ERROR_MESSAGE, response)

        assertFalse(pushNotificationDeviceManager.createDevice(UUID))
    }

    @Test
    fun removes_cn_device_locally() {
        val pushNotificationDeviceManager = createManager()

        pushNotificationDeviceManager.removeCNDevice()

        verify(pushNotificationDeviceManager.preferencesUtil)
                .removePreference(PushNotificationDeviceManager.PUSH_NOTIFICATION_SERVER_DEVICE_ID)
    }

    private fun <T> generateSuccessResponse(responseCode: Int, responseData: T?): Response<T> {
        return Response.success(responseData, okhttp3.Response.Builder()
                .code(responseCode)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(Request.Builder().url("http://localhost/").build())
                .build())
    }
}