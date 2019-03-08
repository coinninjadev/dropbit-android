package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNSubscription;
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState;
import com.coinninja.coinkeeper.service.client.model.CNTopic;
import com.coinninja.coinkeeper.util.CNLogger;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class PushNotificationSubscriptionManager {
    public static final String FETCH_SUBSCRIPTIONS_FAILED = "-- Fetch Subscriptions Failed";
    public static final String TAG = PushNotificationSubscriptionManager.class.getSimpleName();
    public static final String SUBSCRIBE_FAILED = "-- Subscribing To Topics Failed";
    public static final String WALLET_SUBSCRIBE_FAILED = "-- Subscribing To Wallet Failed";
    public static final String UPDATE_WALLET_SUBSCRIBE_FAILED = "-- Updating Wallet Subscription Failed";

    private final SignedCoinKeeperApiClient apiClient;
    private CNLogger logger;

    @Inject
    PushNotificationSubscriptionManager(SignedCoinKeeperApiClient apiClient, CNLogger logger) {
        this.apiClient = apiClient;
        this.logger = logger;
    }

    public void subscribeToChannels(String deviceId, String deviceEndpointId) {
        Response response = apiClient.fetchDeviceEndpointSubscriptions(deviceId, deviceEndpointId);
        if (response.isSuccessful()) {
            CNSubscriptionState subscriptionState = (CNSubscriptionState) response.body();
            List<CNTopic> availableTopics = subscriptionState.getAvailableTopics();
            List<CNSubscription> subscriptions = subscriptionState.getSubscriptions();
            identifyTopics(availableTopics, subscriptions);
            subscribeToTopics(deviceId, deviceEndpointId, availableTopics);

            if (!hasWalletSubscription(subscriptions))
                subscribeToWalletNotifications(deviceEndpointId);
        } else {
            logger.logError(TAG, FETCH_SUBSCRIPTIONS_FAILED, response);
        }
    }

    private boolean hasWalletSubscription(List<CNSubscription> subscriptions) {
        boolean hasSubscription = false;
        for (CNSubscription cnSubscription : subscriptions) {
            if ("Wallet".equals(cnSubscription.getOwnerType())) {
                hasSubscription = true;
            }
        }
        return hasSubscription;
    }

    void identifyTopics(List<CNTopic> availableTopics, List<CNSubscription> subscriptions) {
        for (CNSubscription subscription : subscriptions) {
            availableTopics.remove(new CNTopic(subscription.getOwnerId()));
        }
    }

    void subscribeToTopics(String deviceId, String deviceEndpointId, List<CNTopic> availableTopics) {
        if (availableTopics.isEmpty()) return;

        Response response = apiClient.subscribeToTopics(deviceId, deviceEndpointId, availableTopics);
        if (!response.isSuccessful()) {
            logger.logError(TAG, SUBSCRIBE_FAILED, response);
        }
    }

    public void subscribeToWalletNotifications(String deviceEndpoint) {
        Response response = apiClient.subscribeToWalletNotifications(deviceEndpoint);
        if (response.code() == 409) {
            updateWalletSubscriptionForEndpoint(deviceEndpoint);
        } else if (!response.isSuccessful()) {
            logger.logError(TAG, WALLET_SUBSCRIBE_FAILED, response);
        }
    }

    private void updateWalletSubscriptionForEndpoint(String deviceEndpoint) {
        Response response = apiClient.updateWalletSubscription(deviceEndpoint);
        if (!response.isSuccessful()) {
            logger.logError(TAG, UPDATE_WALLET_SUBSCRIBE_FAILED, response);
        }
    }
}
