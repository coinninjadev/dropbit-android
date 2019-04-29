package com.coinninja.coinkeeper.receiver;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowApplication;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SyncSchedulerTest {

    private TestCoinKeeperApplication application;

    //TODO have Sync Schedule be a result of a broadcast, this should all live in a broadcast receiver

    @Mock
    CNWalletManager cnWalletManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        when(cnWalletManager.hasWallet()).thenReturn(true);
    }

    @Ignore
    @Test
    public void itSchedulesAnAlarmToSyncAtInterval() {
        ShadowAlarmManager shadowAlarmManager = shadowOf((AlarmManager) application.getSystemService(Context.ALARM_SERVICE));

        Intent intent = new Intent();
        intent.setAction("android.intent.action.BOOT_COMPLETED");
        new DeviceRebootBootCompletedReceiver().onReceive(application, intent);


        ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.peekNextScheduledAlarm();
        assertNotNull(scheduledAlarm);
    }

    @Ignore
    @Test
    public void itKicksOffSyncService() {
        ShadowApplication shadow = shadowOf(application);

        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        new DeviceRebootBootCompletedReceiver().onReceive(application, intent);

        Intent startedService = shadow.getNextStartedService();
        assertThat(startedService.getComponent().getClassName(), equalTo(WalletTransactionRetrieverService.class.getName()));
    }

    @Ignore
    @Test
    public void itWillNotScheduleAlarmOrSyncWhenWordsAreNotBackedUp() {
        ShadowApplication shadow = shadowOf(application);
        ShadowAlarmManager shadowAlarmManager = shadowOf((AlarmManager) application.getSystemService(Context.ALARM_SERVICE));
        when(cnWalletManager.hasWallet()).thenReturn(false);

        Intent intent = new Intent("android.intent.action.BOOT_COMPLETED");
        new DeviceRebootBootCompletedReceiver().onReceive(application, intent);

        assertNull(shadow.getNextStartedService());
        assertNull(shadowAlarmManager.peekNextScheduledAlarm());
    }
}