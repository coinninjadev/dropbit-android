package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNDevice;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import javax.inject.Inject;

import retrofit2.Response;


public class PushNotificationDeviceManager {
    static final String TAG = PushNotificationDeviceManager.class.getSimpleName();
    static final String DEVICE_REGISTRATION_ERROR_MESSAGE = "----- Device Registration Failed";
    static final String PUSH_NOTIFICATION_SERVER_DEVICE_ID = "push_notification_server_device_id";

    private PreferencesUtil preferencesUtil;
    private SignedCoinKeeperApiClient apiClient;
    private CNLogger logger;

    @Inject
    PushNotificationDeviceManager(PreferencesUtil preferencesUtil,
                                  SignedCoinKeeperApiClient apiClient,
                                  CNLogger logger) {
        this.preferencesUtil = preferencesUtil;
        this.apiClient = apiClient;
        this.logger = logger;
    }

    /**
     * @param uuid
     * @return true if created
     */
    public boolean createDevice(String uuid) {
        boolean created = false;
        Response response = apiClient.createCNDevice(uuid);
        if (response.isSuccessful()) {
            CNDevice cnDevice = (CNDevice) response.body();
            saveDevice(cnDevice);
            created = true;
        } else {
            logger.logError(TAG, DEVICE_REGISTRATION_ERROR_MESSAGE, response);
        }
        return created;
    }

    void saveDevice(CNDevice device) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ID, device.getId());
    }

    public String getDeviceId() {
        return preferencesUtil.getString(PUSH_NOTIFICATION_SERVER_DEVICE_ID, "");
    }

    public void removeCNDevice() {
        preferencesUtil.removePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ID);
    }
}
