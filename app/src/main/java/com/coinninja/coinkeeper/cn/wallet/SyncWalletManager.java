package com.coinninja.coinkeeper.cn.wallet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.coinninja.coinkeeper.cn.wallet.service.CNWalletBinder;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler;
import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService;
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class SyncWalletManager implements ServiceConnection {
    public static final int NETWORK_TYPE_ANY = 1;
    public static final long REPEAT_FREQUENCY_60_SECONDS = 60 * 1000;
    public static final long REPEAT_FREQUENCY_1_HOUR = 60 * 60 * 1000;
    CNWalletBinder binder;
    private Context context;
    private CNWalletManager cnWalletManager;
    private JobServiceScheduler jobServiceScheduler;
    private Handler timeoutHandler;
    Runnable timeOutRunnable = () -> {
        syncNow();
        schedule60SecondSync();
    };

    @Inject
    public SyncWalletManager(@ApplicationContext Context context, CNWalletManager cnWalletManager,
                             JobServiceScheduler jobServiceScheduler, @ThreadHandler Handler timeoutHandler) {
        this.context = context;
        this.cnWalletManager = cnWalletManager;
        this.jobServiceScheduler = jobServiceScheduler;
        this.timeoutHandler = timeoutHandler;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        this.binder = (CNWalletBinder) binder;
        syncNow();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        binder = null;
    }

    public void schedule60SecondSync() {
        if (!cnWalletManager.getHasWallet()) return;
        if (null == binder) {
            context.bindService(new Intent(context, CNWalletService.class), this, Context.BIND_AUTO_CREATE);
        }
        timeoutHandler.postDelayed(timeOutRunnable, REPEAT_FREQUENCY_60_SECONDS);
    }

    public void scheduleHourlySync() {
        if (!cnWalletManager.getHasWallet()) return;

        jobServiceScheduler.schedule(context, JobServiceScheduler.SYNC_HOURLY_SERVICE_JOB_ID,
                WalletTransactionRetrieverService.class, NETWORK_TYPE_ANY,
                REPEAT_FREQUENCY_1_HOUR, true);
    }

    public void syncNow() {
        if (!cnWalletManager.getHasWallet() || binder == null) return;

        binder.getService().performSync();
    }

    public void cancel30SecondSync() {
        if (null != binder) {
            context.unbindService(this);
            binder = null;
        }

        timeoutHandler.removeCallbacks(timeOutRunnable);
    }

    public void cancelAllOldSyncJobs() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent serviceIntent = new Intent(context, WalletTransactionRetrieverService.class);

        PendingIntent alarmIntent = PendingIntent.getService(context, 0, serviceIntent, 0);
        alarmManager.cancel(alarmIntent);

        alarmIntent = PendingIntent.getService(context, 30, serviceIntent, 0);
        alarmManager.cancel(alarmIntent);
    }

    public void cancelAllScheduledSync() {
        context.stopService(new Intent(context, WalletTransactionRetrieverService.class));
        cancel30SecondSync();
        jobServiceScheduler.cancelJob(JobServiceScheduler.SYNC_HOURLY_SERVICE_JOB_ID);
    }

}
