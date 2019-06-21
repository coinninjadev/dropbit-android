package com.coinninja.coinkeeper.cn.service;

import androidx.annotation.NonNull;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNSubscription;
import com.coinninja.coinkeeper.service.client.model.CNSubscriptionState;
import com.coinninja.coinkeeper.service.client.model.CNTopic;
import com.coinninja.coinkeeper.util.CNLogger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationSubscriptionManagerTest {
    private String devicesId = "-- device id";
    private String deviceEndpoint = "--- device endpoint id";

    @Mock
    SignedCoinKeeperApiClient apiClient;

    @Mock
    CNLogger logger;

    @InjectMocks
    PushNotificationSubscriptionManager pushNotificationSubscriptionManager;
    private CNSubscriptionState subscriptionState;


    @Before
    public void setUp() {
        mockWalletSubscriptionResponse();
        mockSuccess_fetch_all_toplics();
        when(apiClient.subscribeToTopics(any(), any(), any())).thenReturn(Response.success(new ArrayList<CNTopic>()));
    }

    @After
    public void tearDown() throws Exception {
        devicesId = null;
        deviceEndpoint = null;
        apiClient = null;
        logger = null;
        pushNotificationSubscriptionManager = null;
        subscriptionState = null;
    }

    @NonNull
    private CNSubscriptionState mockSuccess_fetch_all_toplics() {
        subscriptionState = new CNSubscriptionState();
        List<CNTopic> topics = new ArrayList<>();
        CNTopic topic = new CNTopic();
        topic.setId("--topic id 0");
        topics.add(topic);
        CNTopic topic1 = new CNTopic();
        topic1.setId("--topic id 1");
        topics.add(topic1);
        subscriptionState.setAvailableTopics(topics);
        List<CNSubscription> subscriptions = new ArrayList<>();
        CNSubscription subscription = new CNSubscription();
        subscriptions.add(subscription);
        subscription.setOwnerId("--topic id 0");
        subscription.setOwnerType("general");
        CNSubscription sub2 = new CNSubscription();
        sub2.setOwnerId("--topic id 2");
        sub2.setOwnerType("general");
        subscriptions.add(sub2);
        subscriptionState.setSubscriptions(subscriptions);
        when(apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint))
                .thenReturn(Response.success(subscriptionState));
        return subscriptionState;
    }

    @Test
    public void subscribes_to_wallet_topic_when_wallet_not_in_subscription_list() {

        pushNotificationSubscriptionManager.subscribeToChannels(devicesId, deviceEndpoint);

        verify(apiClient).subscribeToWalletNotifications(deviceEndpoint);
    }


    @Test
    public void dosnt_subscribe_to_wallet_topic_when_wallet_is_in_subscription_list() {
        subscriptionState.getSubscriptions().get(0).setOwnerType("Wallet");

        pushNotificationSubscriptionManager.subscribeToChannels(devicesId, deviceEndpoint);

        verify(apiClient, times(0)).subscribeToWalletNotifications(any());
    }

    @Test
    public void subscribes_to_unsubscribed_topics() {
        List<CNTopic> topics = subscriptionState.getAvailableTopics();

        pushNotificationSubscriptionManager.subscribeToChannels(devicesId, deviceEndpoint);

        verify(apiClient).subscribeToTopics(devicesId, deviceEndpoint, topics);
        assertThat(topics.get(0).getId(), equalTo("--topic id 1"));
        assertThat(topics.size(), equalTo(1));

        verify(logger, times(0)).logError(anyString(),
                anyString(), any());
    }

    @Test
    public void fetch_all_topics() {
        mockSuccess_fetch_all_toplics();

        pushNotificationSubscriptionManager.subscribeToChannels(devicesId, deviceEndpoint);

        verify(apiClient).fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint);
    }

    @Test
    public void logs_failures_when_fetching_subscriptions() {
        Response response = Response.error(500,
                ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.fetchDeviceEndpointSubscriptions(devicesId, deviceEndpoint)).thenReturn(response);

        pushNotificationSubscriptionManager.subscribeToChannels(devicesId, deviceEndpoint);

        verify(logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.FETCH_SUBSCRIPTIONS_FAILED, response);
        verify(apiClient, times(0)).subscribeToTopics(anyString(), anyString(), any());
    }

    @Test
    public void logs_failures_when_subscribing_to_topics() {
        List<CNTopic> topics = new ArrayList<>();
        topics.add(new CNTopic());
        Response response = Response.error(500,
                ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.subscribeToTopics(devicesId, deviceEndpoint, topics)).thenReturn(response);

        pushNotificationSubscriptionManager.subscribeToTopics(devicesId, deviceEndpoint, topics);

        verify(logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.SUBSCRIBE_FAILED, response);
    }

    @Test
    public void do_not_subscribe_to_topics_when_available_topics_list_is_empty() {
        pushNotificationSubscriptionManager.subscribeToTopics(devicesId, deviceEndpoint, new ArrayList<CNTopic>());

        verify(apiClient, times(0)).subscribeToTopics(any(), any(), any());

    }

    private void mockWalletSubscriptionResponse() {
        when(apiClient.subscribeToWalletNotifications(any())).thenReturn(Response.success(mock(CNSubscription.class)));
        when(apiClient.updateWalletSubscription(any())).thenReturn(Response.success(mock(CNSubscription.class)));
    }

    @Test
    public void subscribes_to_wallet_notifications() {
        mockWalletSubscriptionResponse();
        pushNotificationSubscriptionManager.subscribeToWalletNotifications(deviceEndpoint);

        verify(apiClient).subscribeToWalletNotifications(deviceEndpoint);
    }

    @Test
    public void logs_error_when_subcribing_wallet_endpoint() {
        Response<Object> response = Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.subscribeToWalletNotifications(any())).thenReturn(response);

        pushNotificationSubscriptionManager.subscribeToWalletNotifications(deviceEndpoint);

        verify(logger).logError(PushNotificationSubscriptionManager.TAG, PushNotificationSubscriptionManager.WALLET_SUBSCRIBE_FAILED, response);
    }

    @Test
    public void on_409_update_the_subscription() {
        mockWalletSubscriptionResponse();
        when(apiClient.subscribeToWalletNotifications(any())).thenReturn(Response.error(409, ResponseBody.create(MediaType.parse("plain/text"), "")));

        pushNotificationSubscriptionManager.subscribeToWalletNotifications(deviceEndpoint);

        verify(apiClient).updateWalletSubscription(deviceEndpoint);
        verify(logger, times(0)).logError(any(), any(), any());
    }


    @Test
    public void logs_error_on_update_wallet_subscription() {
        when(apiClient.subscribeToWalletNotifications(any())).thenReturn(Response.error(409, ResponseBody.create(MediaType.parse("plain/text"), "")));
        Response<Object> response = Response.error(500, ResponseBody.create(MediaType.parse("plain/text"), ""));
        when(apiClient.updateWalletSubscription(any())).thenReturn(response);

        pushNotificationSubscriptionManager.subscribeToWalletNotifications(deviceEndpoint);

        verify(logger).logError(PushNotificationSubscriptionManager.TAG,
                PushNotificationSubscriptionManager.UPDATE_WALLET_SUBSCRIBE_FAILED, response);

    }
}