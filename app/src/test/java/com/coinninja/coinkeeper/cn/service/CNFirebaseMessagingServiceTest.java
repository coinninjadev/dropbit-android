package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CNFirebaseMessagingServiceTest {

    @Mock
    PushNotificationServiceManager pushNotificationServiceManager;
    private CNFirebaseMessagingService firebaseMessagingService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        firebaseMessagingService = Robolectric.setupService(CNFirebaseMessagingService.class);

        firebaseMessagingService.pushNotificationServiceManager = pushNotificationServiceManager;
    }

    @After
    public void tearDown() throws Exception {
        pushNotificationServiceManager = null;
        firebaseMessagingService = null;
    }

    @Test
    public void updates_user_token_with_manager_when_change() {
        String token = "-- new token --";
        firebaseMessagingService.onNewToken(token);
        verify(pushNotificationServiceManager).saveToken(token);
    }

}