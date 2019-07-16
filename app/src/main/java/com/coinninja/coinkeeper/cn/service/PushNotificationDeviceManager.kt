package com.coinninja.coinkeeper.cn.service

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNDevice
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class PushNotificationDeviceManager @Inject internal constructor(
        internal val preferencesUtil: PreferencesUtil,
        internal val apiClient: SignedCoinKeeperApiClient,
        internal val logger: CNLogger) {

    val deviceId: String
        get() = preferencesUtil.getString(PUSH_NOTIFICATION_SERVER_DEVICE_ID, "")

    /**
     * @param uuid
     * @return true if created
     */
    fun createDevice(uuid: String): Boolean {
        val response = apiClient.createCNDevice(uuid)
        return if (response.isSuccessful) {
            response.body()?.also {
                saveDevice(it as CNDevice)
            }
            true
        } else {
            logger.logError(TAG, DEVICE_REGISTRATION_ERROR_MESSAGE, response)
            false
        }
    }

    internal fun saveDevice(device: CNDevice) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ID, device.id)
    }

    fun removeCNDevice() {
        preferencesUtil.removePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ID)
    }

    companion object {
        internal val TAG = PushNotificationDeviceManager::class.java.simpleName
        internal const val DEVICE_REGISTRATION_ERROR_MESSAGE = "----- Device Registration Failed"
        internal const val PUSH_NOTIFICATION_SERVER_DEVICE_ID = "push_notification_server_device_id"
    }
}
