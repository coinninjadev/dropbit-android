package com.coinninja.coinkeeper.cn.wallet;

import android.app.Application;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletBinder;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService;
import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SyncWalletManagerTest {

    @Mock
    private CNWalletBinder cnWalletBinder;

    @Mock
    private CNWalletManager cnWalletManager;

    @Mock
    private JobServiceScheduler jobServiceScheduler;

    @Mock
    private Handler handler;

    @Mock
    private CNWalletServicesInterface cnWalletService;

    private Context context = RuntimeEnvironment.application;

    private SyncWalletManager syncWalletManager;
    private ShadowApplication shadowApplication;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        shadowApplication = shadowOf((Application) context);
        syncWalletManager = new SyncWalletManager(context, cnWalletManager, jobServiceScheduler, handler);
        when(cnWalletManager.hasWallet()).thenReturn(true);
        when(cnWalletBinder.getService()).thenReturn(cnWalletService);
    }

    @After
    public void tearDown() {
        cnWalletBinder = null;
        cnWalletManager = null;
        jobServiceScheduler = null;
        handler = null;
        cnWalletService = null;
        context = null;
        syncWalletManager = null;
        shadowApplication = null;
    }

    @Test
    public void does_not_execute_sync_on_demand_when_no_wallet_exists() {
        when(cnWalletManager.hasWallet()).thenReturn(false);

        syncWalletManager.syncNow();

        verify(jobServiceScheduler, times(0)).enqueueWork(any(), any(), anyInt(), any());
    }

    @Test
    public void performs_sync_when_connection_established() {
        syncWalletManager.onServiceConnected(new ComponentName(context, CNWalletService.class), cnWalletBinder);

        verify(cnWalletService).performSync();
    }

    @Test
    public void performs_sync_when_services_bound() {
        syncWalletManager.onServiceConnected(new ComponentName(context, CNWalletService.class), cnWalletBinder);

        verify(cnWalletService).performSync();
    }

    @Test
    public void performs_sync_when_wallet_exists() {
        syncWalletManager.binder = cnWalletBinder;

        syncWalletManager.syncNow();

        verify(cnWalletService).performSync();
    }

    @Test
    public void does_not_execute_sync_when_not_bound() {
        syncWalletManager.binder = cnWalletBinder;
        syncWalletManager.cancel30SecondSync();

        syncWalletManager.syncNow();

        verify(cnWalletService, times(0)).performSync();
    }

    @Test
    public void shedules_sync_for_30_second_intervals() {
        syncWalletManager.schedule60SecondSync();

        verify(handler).postDelayed(syncWalletManager.timeOutRunnable, 60 * 1000);
    }

    @Test
    public void binds_to_cn_wallet_service_when_sync_is_scheduled() {
        syncWalletManager.schedule60SecondSync();

        List<ServiceConnection> boundServiceConnections = ShadowApplication.getInstance().getBoundServiceConnections();
        assertThat(boundServiceConnections.size(), equalTo(1));
        ServiceConnection serviceConnection = boundServiceConnections.get(0);
        assertThat(serviceConnection, equalTo(syncWalletManager));
    }

    @Test
    public void unbinds_cn_wallet_service_when_30_second_sync_unscheduled() {
        syncWalletManager.onServiceConnected(new ComponentName(context, CNWalletService.class), cnWalletBinder);

        syncWalletManager.cancel30SecondSync();

        List<ServiceConnection> unboundServiceConnections = ShadowApplication.getInstance().getUnboundServiceConnections();
        assertThat(unboundServiceConnections.size(), equalTo(1));
        ServiceConnection serviceConnection = unboundServiceConnections.get(0);
        assertThat(serviceConnection, equalTo(syncWalletManager));
    }

    @Test
    public void cancels_30_second_sync() {
        syncWalletManager.cancel30SecondSync();

        verify(handler).removeCallbacks(syncWalletManager.timeOutRunnable);
    }

    @Test
    public void only_syncs_when_wallet_exists() {
        when(cnWalletManager.hasWallet()).thenReturn(false);

        syncWalletManager.schedule60SecondSync();

        verify(handler, times(0)).postDelayed(any(), anyLong());
    }

    @Test
    public void shedules_sync_for_hourly_intervals() {
        syncWalletManager.scheduleHourlySync();

        verify(jobServiceScheduler).schedule(context, 106, WalletTransactionRetrieverService.class,
                JobInfo.NETWORK_TYPE_ANY,
                60 * 60 * 1000, true);
    }

    @Test
    public void only_syncs_hourly_when_wallet_exists() {
        when(cnWalletManager.hasWallet()).thenReturn(false);

        syncWalletManager.scheduleHourlySync();

        verify(jobServiceScheduler, times(0)).schedule(any(), anyInt(), any(), anyInt(), anyInt(), anyBoolean());
    }

    @Test
    public void canceling_sync_turns_off_services() {
        syncWalletManager.cancelAllScheduledSync();

        Intent intent = shadowApplication.getNextStoppedService();
        assertThat(intent.getComponent().getClassName(), equalTo(WalletTransactionRetrieverService.class.getName()));
        verify(handler).removeCallbacks(syncWalletManager.timeOutRunnable);
        verify(jobServiceScheduler).cancelJob(JobServiceScheduler.SYNC_HOURLY_SERVICE_JOB_ID);
    }
}