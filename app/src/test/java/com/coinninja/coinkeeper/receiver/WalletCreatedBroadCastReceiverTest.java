package com.coinninja.coinkeeper.receiver;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;

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
public class WalletCreatedBroadCastReceiverTest {


    protected WalletCreatedBroadCastReceiver receiver;
    private TestCoinKeeperApplication app;

    @Before
    public void setUp() {
        app = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        receiver = new WalletCreatedBroadCastReceiver();
        receiver.onReceive(app, new Intent(DropbitIntents.ACTION_WALLET_CREATED));
    }

    @After
    public void tearDown() {
        receiver = null;
        app = null;
    }

    @Test
    public void reports_that_user_has_wallet() {
        verify(app.analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET, true);
    }

    @Test
    public void executes_first_sync() {
        verify(app.syncWalletManager).syncNow();
    }

    @Test
    public void schedules_30_second_sync() {
        verify(app.syncWalletManager).schedule30SecondSync();
    }

    @Test
    public void schedules_hourly_sync() {
        verify(app.syncWalletManager).scheduleHourlySync();
    }

}