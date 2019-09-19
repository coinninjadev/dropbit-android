package com.coinninja.coinkeeper.cn.wallet

import android.app.job.JobInfo
import android.content.ComponentName
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService
import com.coinninja.coinkeeper.service.WalletTransactionRetrieverService
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class SyncWalletManagerTest {
    private fun createManager(): SyncWalletManager {
        val manager = SyncWalletManager(mock(), mock(), mock(), mock(), mock())
        manager.binder = mock()
        whenever(manager.cnWalletManager.hasWallet).thenReturn(true)
        whenever(manager.binder!!.service).thenReturn(mock())
        return manager
    }

    @Test
    fun does_not_execute_sync_on_demand_when_no_wallet_exists() {
        val manager = createManager()
        whenever(manager.cnWalletManager.hasWallet).thenReturn(false)

        manager.syncNow()

        verify(manager.jobServiceScheduler, times(0)).enqueueWork(any(), any(), any(), any())
    }

    @Test
    fun performs_sync_when_connection_established() {
        val manager = createManager()

        manager.onServiceConnected(ComponentName(manager.context, CNWalletService::class.java), manager.binder!!)

        verify(manager.binder!!.service).performSync()
    }

    @Test
    fun performs_sync_when_services_bound() {
        val manager = createManager()
        manager.onServiceConnected(ComponentName(manager.context, CNWalletService::class.java), manager.binder!!)
        verify(manager.binder!!.service).performSync()
    }

    @Test
    fun performs_sync_when_wallet_exists() {
        val manager = createManager()
        manager.syncNow()
        verify(manager.binder!!.service).performSync()
    }

    @Test
    fun schedules_sync_for_30_second_intervals() {
        val manager = createManager()

        manager.schedule60SecondSync()

        verify(manager.timeoutHandler).postDelayed(manager.timeOutRunnable, 60 * 1000.toLong())
    }

    @Test
    fun binds_to_cn_wallet_service_when_sync_is_scheduled() {
        val manager = createManager()
        manager.binder = null

        manager.schedule60SecondSync()

        verify(manager.serviceWorkUtil).bindToCNWalletService(manager)
    }

    @Test
    fun binds_to_service_when_syncing_and_unbound() {
        val manager = createManager()
        manager.binder = null

        manager.syncNow()

        verify(manager.serviceWorkUtil).bindToCNWalletService(manager)
    }

    @Test
    fun cancels_30_second_sync() {
        val manager = createManager()
        manager.cancel60SecondSync()
        verify(manager.timeoutHandler).removeCallbacks(manager.timeOutRunnable)
    }

    @Test
    fun only_syncs_when_wallet_exists() {
        val manager = createManager()
        whenever(manager.cnWalletManager.hasWallet).thenReturn(false)

        manager.schedule60SecondSync()

        verify(manager.timeoutHandler, times(0)).postDelayed(any(), any())
    }

    @Test
    fun schedules_sync_for_hourly_intervals() {
        val manager = createManager()

        manager.scheduleHourlySync()

        verify(manager.jobServiceScheduler).schedule(
                manager.context, 106,
                WalletTransactionRetrieverService::class.java,
                JobInfo.NETWORK_TYPE_ANY,
                60 * 60 * 1000.toLong(),
                true
        )
    }

    @Test
    fun only_syncs_hourly_when_wallet_exists() {
        val manager = createManager()
        whenever(manager.cnWalletManager.hasWallet).thenReturn(false)

        manager.scheduleHourlySync()

        verify(manager.jobServiceScheduler, times(0)).schedule(any(), any(), any(), any(), any(), any())
    }
}