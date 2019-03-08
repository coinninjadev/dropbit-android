package com.coinninja.coinkeeper.receiver;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class DeviceRebootBootCompletedReceiverTest {

    private TestCoinKeeperApplication application;
    private DeviceRebootBootCompletedReceiver receiver;

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        receiver = new DeviceRebootBootCompletedReceiver();
    }

    @After
    public void tearDown() {
        application = null;
        receiver = null;
    }

    @Test
    public void schedules_hourly_sync() {
        receiver.onReceive(application, null);

        verify(receiver.syncWalletManager).scheduleHourlySync();
    }
}