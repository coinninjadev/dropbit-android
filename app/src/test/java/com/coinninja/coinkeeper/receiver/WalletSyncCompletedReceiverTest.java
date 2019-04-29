package com.coinninja.coinkeeper.receiver;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.service.BtcBroadcastNotificationService;
import com.coinninja.coinkeeper.service.ContactLookupService;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class WalletSyncCompletedReceiverTest {

    private WalletSyncCompletedReceiver receiver;
    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        application.cnWalletManager = mock(CNWalletManager.class);
        receiver = new WalletSyncCompletedReceiver();
    }

    @After
    public void tearDown() {
        application = null;
        receiver = null;
    }

    @Test
    public void reports_that_user_has_balance() {
        when(application.cnWalletManager.hasBalance()).thenReturn(true);

        receiver.onReceive(application, null);

        verify(application.analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, true);
    }

    @Test
    public void sends_local_broadcast() {
        Intent intent = new Intent(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE);
        receiver.onReceive(application, intent);

        verify(application.localBroadCastUtil).sendBroadcast(intent);
    }

    @Test
    public void starts_contact_lookup() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        receiver.onReceive(application, null);
        verify(application.jobServiceScheduler).enqueueWork(eq(application),
                eq(ContactLookupService.class),
                eq(108),
                argumentCaptor.capture()
        );

        Intent intent = argumentCaptor.getValue();
        assertThat(intent.getComponent().getClassName(),
                equalTo(ContactLookupService.class.getName()));
    }

    @Test
    public void queues_job_to_execute() {
        ArgumentCaptor<Intent> argumentCaptor = ArgumentCaptor.forClass(Intent.class);
        receiver.onReceive(application, null);
        verify(application.jobServiceScheduler).enqueueWork(eq(application),
                eq(BtcBroadcastNotificationService.class),
                eq(107),
                argumentCaptor.capture()
        );

        Intent intent = argumentCaptor.getValue();
        assertThat(intent.getComponent().getClassName(),
                equalTo(BtcBroadcastNotificationService.class.getName()));
    }


}