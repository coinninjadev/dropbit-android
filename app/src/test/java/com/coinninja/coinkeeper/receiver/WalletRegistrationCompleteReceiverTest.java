package com.coinninja.coinkeeper.receiver;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService;
import com.coinninja.coinkeeper.service.DeviceRegistrationService;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class WalletRegistrationCompleteReceiverTest {

    private WalletRegistrationCompleteReceiver walletRegistrationCompleteReceiver;

    private TestCoinKeeperApplication application;

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        walletRegistrationCompleteReceiver = new WalletRegistrationCompleteReceiver();
    }

    @After
    public void tearDown() {
        application = null;
        walletRegistrationCompleteReceiver = null;
    }

    @Test
    public void schedules_device_registration_job_when_wallet_registration_has_completeds() {
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);


        walletRegistrationCompleteReceiver.onReceive(application, null);

        verify(application.jobServiceScheduler).enqueueWork(eq(application),
                eq(DeviceRegistrationService.class),
                eq(101), captor.capture());
        Intent intent = captor.getValue();
        Assert.assertThat(intent.getComponent().getClassName(), Matchers.equalTo(DeviceRegistrationService.class.getName()));
    }

    @Test
    public void starts_global_messaging_services() {
        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);

        walletRegistrationCompleteReceiver.onReceive(application, null);

        verify(application.jobServiceScheduler).enqueueWork(eq(application),
                eq(CNGlobalMessagingService.class),
                eq(JobServiceScheduler.GLOBAL_MESSAGING_SERVICE_JOB_ID), captor.capture());
        Intent intent = captor.getValue();
        assertThat(intent.getComponent().getClassName(), equalTo(CNGlobalMessagingService.class.getName()));
    }

}