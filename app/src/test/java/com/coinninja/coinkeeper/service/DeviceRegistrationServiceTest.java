package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DeviceRegistrationServiceTest {
    private final String UUID = "-- UUID --";

    @Mock
    PushNotificationServiceManager pushNotificationServiceManager;

    @InjectMocks
    private DeviceRegistrationService service;

    @Before
    public void setUp() {
        service.uuid = UUID;
    }

    @Test
    public void registers_device_with_coinninja() {
        service.onHandleWork(null);

        verify(pushNotificationServiceManager).registerDevice(UUID);
    }

}