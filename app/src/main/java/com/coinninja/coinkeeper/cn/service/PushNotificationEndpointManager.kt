package com.coinninja.coinkeeper.cn.service

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class PushNotificationEndpointManager @Inject internal constructor(
        internal val preferencesUtil: PreferencesUtil,
        internal val pushNotificationTokenManager: PushNotificationTokenManager,
        internal val apiClient: SignedCoinKeeperApiClient,
        internal val logger: CNLogger,
        internal val pushNotificationDeviceManager: PushNotificationDeviceManager,
        internal val pushNotificationSubscriptionManager: PushNotificationSubscriptionManager) {

    val endpoint: String get() = preferencesUtil.getString(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, "")

    fun removeEndpoint() {
        preferencesUtil.removePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID)
    }

    fun registersAsEndpoint(deviceId: String) {
        val token = pushNotificationTokenManager.token
        registersAsEndpoint(deviceId, token)
    }

    fun registersAsEndpoint(deviceId: String, token: String) {
        val response = apiClient.registerForPushEndpoint(deviceId, token)
        if (response.isSuccessful) {
            val cnDeviceEndpoint = response.body() as CNDeviceEndpoint
            saveEndpoint(cnDeviceEndpoint)
            pushNotificationSubscriptionManager.subscribeToChannels(deviceId, cnDeviceEndpoint.id)
        } else {
            logger.logError(TAG, DEVICE_ENDPOINT_ERROR_MESSAGE, response)
        }
    }

    fun registersAsEndpoint() {
        val deviceId = pushNotificationDeviceManager.deviceId
        val token = pushNotificationTokenManager.token

        if (shouldNotCreateEndpoint(deviceId, token)) return

        registersAsEndpoint(deviceId, token)
    }

    fun hasEndpoint(): Boolean {
        return preferencesUtil.contains(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID)
    }

    @Suppress("UNCHECKED_CAST")
    fun fetchEndpoints(): List<CNDeviceEndpoint> {
        val response = apiClient.fetchRemoteEndpointsFor(pushNotificationDeviceManager.deviceId)
        if (response.isSuccessful) {
            return response.body() as List<CNDeviceEndpoint>
        } else {
            logger.logError(TAG, FETCH_DEVICE_ENDPOINT_ERROR_MESSAGE, response)
        }

        return emptyList()
    }

    @JvmOverloads
    fun unRegister(endpointId: String? = endpoint) {
        endpointId?.let {
            val response = apiClient.unRegisterDeviceEndpoint(pushNotificationDeviceManager.deviceId, it)
            if (!response.isSuccessful) {
                logger.logError(TAG, UNREGISTER_DEVICE_ENDPOINT_ERROR_MESSAGE, response)
            }
        }
    }

    private fun shouldNotCreateEndpoint(deviceId: String, token: String): Boolean {
        return token.isEmpty() || deviceId.isEmpty()
    }

    private fun saveEndpoint(endpoint: CNDeviceEndpoint) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, endpoint.id)
    }

    companion object {
        internal val TAG = PushNotificationEndpointManager::class.java.simpleName
        internal const val DEVICE_ENDPOINT_ERROR_MESSAGE = "----- Device Endpoint Creation Failed"
        internal const val FETCH_DEVICE_ENDPOINT_ERROR_MESSAGE = "----- Fetch Endpoints Failed"
        internal const val PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID = "push_notification_server_device_endpoint_id"
        internal const val UNREGISTER_DEVICE_ENDPOINT_ERROR_MESSAGE = "----- Unregister Endpoints Failed"
    }
}
