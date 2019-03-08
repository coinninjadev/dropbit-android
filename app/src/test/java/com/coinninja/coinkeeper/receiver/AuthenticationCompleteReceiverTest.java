package com.coinninja.coinkeeper.receiver;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class AuthenticationCompleteReceiverTest {

    private TestCoinKeeperApplication application;
    private AuthenticationCompleteReceiver receiver;

    @Before
    public void setUp() throws Exception {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        receiver = new AuthenticationCompleteReceiver();
    }

    @Test
    public void on_receive_start_cn_messaging_services_test() {
        receiver.onReceive(application, null);

        ShadowApplication shadowApp = shadowOf(application);

        Intent intent = shadowApp.getNextStartedService();
        assertThat(intent.getComponent().getClassName(), equalTo(CNGlobalMessagingService.class.getName()));
    }
}