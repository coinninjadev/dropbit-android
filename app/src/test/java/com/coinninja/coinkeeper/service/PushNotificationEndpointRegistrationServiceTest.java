package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationEndpointRegistrationServiceTest {

    @Mock
    PushNotificationServiceManager pushNotificationServiceManager;


    private PushNotificationEndpointRegistrationService service;

    @Before
    public void setUp() {
        service = new PushNotificationEndpointRegistrationService();
        service.pushNotificationServiceManager = pushNotificationServiceManager;

    }

    @Test
    public void registers_push_notifications_endpoint() {

        service.onHandleWork(null);

        verify(pushNotificationServiceManager).registerAsEndpoint();
    }

    @Test
    public void subscribes_to_all_topics() {

        service.onHandleWork(null);

        verify(pushNotificationServiceManager).subscribeToChannels();
    }
}