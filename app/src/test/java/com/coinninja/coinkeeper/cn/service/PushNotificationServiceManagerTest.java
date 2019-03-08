package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.service.client.model.CNDeviceEndpoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationServiceManagerTest {

    private String UUID = "-uuid-";
    private String TOKEN = "-push_token-";
    private String CN_DEVICE_ID = "158a4cf9-0362-4636-8c68-ed7a98a7f345";
    private String CN_DEVICE_ENDPOINT_ID = "--endpoint id";

    @Mock
    PushNotificationTokenManager pushNotificationTokenManager;

    @Mock
    PushNotificationDeviceManager pushNotificationDeviceManager;

    @Mock
    PushNotificationEndpointManager pushNotificationEndpointManager;

    @Mock
    PushNotificationSubscriptionManager pushNotificationSubscriptionManager;

    @InjectMocks
    private PushNotificationServiceManager pushNotificationServiceManager;

    @Before
    public void setUp() {
        when(pushNotificationTokenManager.getToken()).thenReturn(TOKEN);
        when(pushNotificationDeviceManager.getDeviceId()).thenReturn(CN_DEVICE_ID);
        when(pushNotificationEndpointManager.getEndpoint()).thenReturn(CN_DEVICE_ENDPOINT_ID);
    }

    @After
    public void tearDown() {
        UUID = null;
        TOKEN = null;
        CN_DEVICE_ID = null;
        CN_DEVICE_ENDPOINT_ID = null;
        pushNotificationTokenManager = null;
        pushNotificationDeviceManager = null;
        pushNotificationSubscriptionManager = null;
        pushNotificationEndpointManager = null;
        pushNotificationServiceManager = null;
    }

    @Test
    public void on_save_token_clear_cn_device_endpoint() {
        String token = "--token";

        pushNotificationServiceManager.saveToken(token);

        verify(pushNotificationTokenManager).saveToken(token);
        verify(pushNotificationEndpointManager).removeEndpoint();
    }

    @Test
    public void registers_device() {
        pushNotificationServiceManager.registerDevice(UUID);

        verify(pushNotificationDeviceManager).createDevice(UUID);
    }


    // Register Endpoint with Coinninja

    @Test
    public void registers_device_endpoint_when_device_created() {
        when(pushNotificationDeviceManager.createDevice(UUID)).thenReturn(true);

        pushNotificationServiceManager.registerDevice(UUID);

        verify(pushNotificationEndpointManager).registersAsEndpoint(CN_DEVICE_ID);
    }

    @Test
    public void do_nothing_when_device_not_created() {
        when(pushNotificationDeviceManager.createDevice(UUID)).thenReturn(false);

        pushNotificationServiceManager.registerDevice(UUID);

        verify(pushNotificationEndpointManager, times(0)).registersAsEndpoint(any(), any());
        verify(pushNotificationEndpointManager, times(0)).registersAsEndpoint(any());
        verify(pushNotificationEndpointManager, times(0)).registersAsEndpoint();
    }


    @Test
    public void register_for_endpoint_when_local_is_absent() {
        pushNotificationServiceManager.registerAsEndpoint();

        verify(pushNotificationEndpointManager).registersAsEndpoint();
    }

    @Test
    public void does_not_registers_endpoints_when_local_and_remotes_match() {
        List<CNDeviceEndpoint> endpoints = new ArrayList<>();
        CNDeviceEndpoint endpoint = new CNDeviceEndpoint();
        endpoint.setId(CN_DEVICE_ENDPOINT_ID);
        endpoints.add(endpoint);
        when(pushNotificationEndpointManager.fetchEndpoints()).thenReturn(endpoints);
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(true);
        when(pushNotificationEndpointManager.getEndpoint()).thenReturn(CN_DEVICE_ENDPOINT_ID);

        pushNotificationServiceManager.registerAsEndpoint();

        verify(pushNotificationEndpointManager).fetchEndpoints();
        verify(pushNotificationEndpointManager, times(0)).registersAsEndpoint();
    }

    @Test
    public void unregisters_remote_endpoints_that_do_not_match_local() {
        String endpointToUnregister = "-- deadend";
        List<CNDeviceEndpoint> endpoints = new ArrayList<>();
        CNDeviceEndpoint endpoint = new CNDeviceEndpoint();
        endpoint.setId(CN_DEVICE_ENDPOINT_ID);
        endpoints.add(endpoint);
        CNDeviceEndpoint endpoint2 = new CNDeviceEndpoint();
        endpoint2.setId(endpointToUnregister);
        endpoints.add(endpoint2);
        when(pushNotificationEndpointManager.fetchEndpoints()).thenReturn(endpoints);
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(true);
        when(pushNotificationEndpointManager.getEndpoint()).thenReturn(CN_DEVICE_ENDPOINT_ID);

        pushNotificationServiceManager.registerAsEndpoint();

        verify(pushNotificationEndpointManager).unRegister(endpointToUnregister);
        verify(pushNotificationEndpointManager, times(0)).unRegister(CN_DEVICE_ENDPOINT_ID);
        verify(pushNotificationEndpointManager, times(0)).registersAsEndpoint();
    }

    @Test
    public void remove_local_endpoint_when_not_matching_remote() {
        String endpointToUnregister = "-- deadend";
        List<CNDeviceEndpoint> endpoints = new ArrayList<>();
        CNDeviceEndpoint endpoint = new CNDeviceEndpoint();
        endpoint.setId("--- deadend 2");
        endpoints.add(endpoint);
        CNDeviceEndpoint endpoint2 = new CNDeviceEndpoint();
        endpoint.setId(endpointToUnregister);
        endpoints.add(endpoint2);
        when(pushNotificationEndpointManager.fetchEndpoints()).thenReturn(endpoints);
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(true);
        when(pushNotificationEndpointManager.getEndpoint()).thenReturn(CN_DEVICE_ENDPOINT_ID);

        pushNotificationServiceManager.registerAsEndpoint();

        verify(pushNotificationEndpointManager).removeEndpoint();
        verify(pushNotificationEndpointManager).registersAsEndpoint();
    }

    @Test
    public void subscribes_to_subscriptions_for_device_endpoint() {
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(true);

        pushNotificationServiceManager.subscribeToChannels();

        verify(pushNotificationSubscriptionManager).subscribeToChannels(CN_DEVICE_ID, CN_DEVICE_ENDPOINT_ID);
    }

    @Test
    public void does_not_subscribes_to_subscriptions_given_no_endpoint() {
        when(pushNotificationEndpointManager.hasEndpoint()).thenReturn(false);

        pushNotificationServiceManager.subscribeToChannels();

        verify(pushNotificationSubscriptionManager, times(0)).subscribeToChannels(CN_DEVICE_ID, CN_DEVICE_ENDPOINT_ID);
    }
}