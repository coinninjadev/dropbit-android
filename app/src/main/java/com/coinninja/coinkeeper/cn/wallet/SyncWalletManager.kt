package com.coinninja.coinkeeper.cn.wallet

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletBinder
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler
import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import javax.inject.Inject

@Mockable
@CoinkeeperApplicationScope
class SyncWalletManager @Inject constructor(
        @ApplicationContext internal val context: Context,
        internal val cnWalletManager: CNWalletManager,
        internal val jobServiceScheduler: JobServiceScheduler,
        internal val serviceWorkUtil: ServiceWorkUtil,
        @ThreadHandler internal val timeoutHandler: Handler
) : ServiceConnection {

    internal var binder: CNWalletBinder? = null
    internal var timeOutRunnable = Runnable {
        syncNow()
        schedule30SecondSync()
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        this.binder = binder as CNWalletBinder
        syncNow()
    }

    override fun onServiceDisconnected(name: ComponentName) {
        binder = null
    }

    fun schedule30SecondSync() {
        if (null == binder) serviceWorkUtil.bindToCNWalletService(this)

        if (cnWalletManager.hasWallet) {
            timeoutHandler.postDelayed(timeOutRunnable, REPEAT_FREQUENCY_30_SECONDS)
        }
    }

    fun scheduleHourlySync() {
        if (cnWalletManager.hasWallet) {
            jobServiceScheduler.schedule(context, JobServiceScheduler.SYNC_HOURLY_SERVICE_JOB_ID,
                    WalletTransactionRetrieverService::class.java, NETWORK_TYPE_ANY, REPEAT_FREQUENCY_1_HOUR, true)
        }
    }

    fun syncNow() {
        if (null == binder) serviceWorkUtil.bindToCNWalletService(this)

        if (cnWalletManager.hasWallet) {
            binder?.service?.performSync()
        }
    }

    fun cancel30SecondSync() {
        timeoutHandler.removeCallbacks(timeOutRunnable)
    }

    fun cancelAllScheduledSync() {
        context.stopService(Intent(context, WalletTransactionRetrieverService::class.java))
        cancel30SecondSync()
        jobServiceScheduler.cancelJob(JobServiceScheduler.SYNC_HOURLY_SERVICE_JOB_ID)
    }

    companion object {
        const val NETWORK_TYPE_ANY = 1
        const val REPEAT_FREQUENCY_30_SECONDS = 30 * 1000.toLong()
        const val REPEAT_FREQUENCY_1_HOUR = 60 * 60 * 1000.toLong()
    }

}