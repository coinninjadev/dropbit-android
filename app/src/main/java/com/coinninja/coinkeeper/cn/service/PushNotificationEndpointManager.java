package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class PushNotificationEndpointManager {
    static final String TAG = PushNotificationEndpointManager.class.getSimpleName();
    static final String DEVICE_ENDPOINT_ERROR_MESSAGE = "----- Device Endpoint Creation Failed";
    static final String FETCH_DEVICE_ENDPOINT_ERROR_MESSAGE = "----- Fetch Endpoints Failed";
    static final String PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID = "push_notification_server_device_endpoint_id";
    static final String UNREGISTER_DEVICE_ENDPOINT_ERROR_MESSAGE = "----- Unregister Endpoints Failed";

    private PreferencesUtil preferencesUtil;
    private PushNotificationTokenManager pushNotificationTokenManager;
    private SignedCoinKeeperApiClient apiClient;
    private CNLogger logger;
    private PushNotificationDeviceManager pushNotificationDeviceManager;
    private PushNotificationSubscriptionManager pushNotificationSubscriptionManager;

    @Inject
    PushNotificationEndpointManager(PreferencesUtil preferencesUtil,
                                    PushNotificationTokenManager pushNotificationTokenManager,
                                    SignedCoinKeeperApiClient apiClient, CNLogger logger,
                                    PushNotificationDeviceManager pushNotificationDeviceManager,
                                    PushNotificationSubscriptionManager pushNotificationSubscriptionManager) {
        this.preferencesUtil = preferencesUtil;
        this.pushNotificationTokenManager = pushNotificationTokenManager;
        this.apiClient = apiClient;
        this.logger = logger;
        this.pushNotificationDeviceManager = pushNotificationDeviceManager;
        this.pushNotificationSubscriptionManager = pushNotificationSubscriptionManager;
    }

    public void removeEndpoint() {
        preferencesUtil.removePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID);
    }

    public void registersAsEndpoint(String deviceId) {
        String token = pushNotificationTokenManager.getToken();
        registersAsEndpoint(deviceId, token);
    }

    public void registersAsEndpoint(String deviceId, String token) {
        Response response = apiClient.registerForPushEndpoint(deviceId, token);
        if (response.isSuccessful()) {
            CNDeviceEndpoint cnDeviceEndpoint = (CNDeviceEndpoint) response.body();
            saveEndpoint(cnDeviceEndpoint);
            pushNotificationSubscriptionManager.subscribeToChannels(deviceId, cnDeviceEndpoint.getID());
        } else {
            logger.logError(TAG, DEVICE_ENDPOINT_ERROR_MESSAGE, response);
        }
    }

    public void registersAsEndpoint() {
        String deviceId = pushNotificationDeviceManager.getDeviceId();
        String token = pushNotificationTokenManager.getToken();

        if (shouldNotCreateEndpoint(deviceId, token)) return;

        registersAsEndpoint(deviceId, token);
    }

    public boolean hasEndpoint() {
        return preferencesUtil.contains(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID);
    }

    public List<CNDeviceEndpoint> fetchEndpoints() {
        Response response = apiClient.fetchRemoteEndpointsFor(pushNotificationDeviceManager.getDeviceId());
        if (response.isSuccessful()) {
            return (List<CNDeviceEndpoint>) response.body();
        } else {
            logger.logError(TAG, FETCH_DEVICE_ENDPOINT_ERROR_MESSAGE, response);
        }

        return new ArrayList<>();
    }

    public String getEndpoint() {
        return preferencesUtil.getString(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, "");
    }

    public void unRegister() {
        unRegister(getEndpoint());
    }

    public void unRegister(String endpointId) {
        Response response = apiClient.unRegisterDeviceEndpoint(pushNotificationDeviceManager.getDeviceId(), endpointId);
        if (!response.isSuccessful()) {
            logger.logError(TAG, UNREGISTER_DEVICE_ENDPOINT_ERROR_MESSAGE, response);
        }
    }

    private boolean shouldNotCreateEndpoint(String deviceId, String token) {
        return token == null || token.isEmpty() || deviceId == null || deviceId.isEmpty();
    }

    private void saveEndpoint(CNDeviceEndpoint endpoint) {
        preferencesUtil.savePreference(PUSH_NOTIFICATION_SERVER_DEVICE_ENDPOINT_ID, endpoint.getID());
    }
}
