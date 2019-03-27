package com.coinninja.coinkeeper.receiver;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.service.DropbitServicePatchService;
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class ApplicationStartedReceiverTest {

    TestCoinKeeperApplication application;

    private ApplicationStartedReceiver appStartedReceiver;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        appStartedReceiver = new ApplicationStartedReceiver();
    }

    @After
    public void tearDown() {
        application = null;
        appStartedReceiver = null;
    }

    @Test
    public void schedules_push_notification_job_intent_services() {
        when(application.cnWalletManager.hasWallet()).thenReturn(true);
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);

        appStartedReceiver.onReceive(application, null);

        verify(application.jobServiceScheduler).enqueueWork(eq(application),
                eq(PushNotificationEndpointRegistrationService.class),
                eq(100), captor.capture());
        Intent intent = captor.getValue();
        assertThat(intent.getComponent().getClassName(), equalTo(PushNotificationEndpointRegistrationService.class.getName()));
    }

    @Test
    public void runs_dropbit_state_patch() {
        when(application.cnWalletManager.hasWallet()).thenReturn(true);
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);

        appStartedReceiver.onReceive(application, null);

        verify(application.jobServiceScheduler).enqueueWork(eq(application),
                eq(DropbitServicePatchService.class),
                eq(103), captor.capture());
        Intent intent = captor.getValue();
        assertThat(intent.getComponent().getClassName(), equalTo(DropbitServicePatchService.class.getName()));

    }

    @Test
    public void skip_when_missing_wallet(){
        when(application.cnWalletManager.hasWallet()).thenReturn(false);

        appStartedReceiver.onReceive(application, null);

        verifyZeroInteractions(application.jobServiceScheduler);
        verifyZeroInteractions(application.syncWalletManager);

    }

}
