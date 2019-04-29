package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.util.android.PreferencesUtil;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PushNotificationTokenManagerTest {

    @Mock
    PreferencesUtil preferencesUtil;


    @InjectMocks
    PushNotificationTokenManager pushNotificationTokenManager;

    @After
    public void tearDown() {
        preferencesUtil = null;
        pushNotificationTokenManager = null;
    }

    @Test
    public void saves_token_locally() {
        String token = "token";

        pushNotificationTokenManager.saveToken(token);

        verify(preferencesUtil).
                savePreference(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, token);
    }

    @Test
    public void provides_access_to_push_token() {
        String token = "-- token";
        when(preferencesUtil
                .getString(PushNotificationTokenManager.PUSH_NOTIFICATION_DEVICE_TOKEN, null))
                .thenReturn(token);

        assertThat(pushNotificationTokenManager.getToken(), equalTo(token));
    }
}