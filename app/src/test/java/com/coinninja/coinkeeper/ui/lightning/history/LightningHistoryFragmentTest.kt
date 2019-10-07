package com.coinninja.coinkeeper.ui.lightning.history

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.persistance.model.LedgerSettlementDetail
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningHistoryFragmentTest {

    private fun createFragment(): FragmentScenario<LightningHistoryFragment> {
        return FragmentScenario.launchInContainer(LightningHistoryFragment::class.java)
    }

    // Lightning settlements

    @Test
    fun observes_lighting_invoice_changes() {
        createFragment().onFragment { fragment ->
            verify(fragment.lightningHistoryViewModel.loadInvoices()).observe(fragment, fragment.invoiceChangeObserver)
        }
    }

    @Test
    fun forwards_lightning_invoice_changes_to_adapter_when_observed() {
        createFragment().onFragment { fragment ->
            val settlements: List<LedgerSettlementDetail> = emptyList()

            fragment.invoiceChangeObserver.onChanged(settlements)

            verify(fragment.lightningHistoryAdapter).settlements = settlements
        }
    }

    // Sync

    @Test
    fun registers_to_observe_sync_changes() {
        createFragment().onFragment { fragment ->
            fragment.syncManagerViewNotifier.observeSyncManagerChange(fragment.syncManagerChangeObserver)
        }
    }

    @Test
    fun pull_to_refresh_executes_sync() {
        createFragment().onFragment { fragment ->
            fragment.onRefreshListener.onRefresh()

            verify(fragment.syncWalletManager).syncNow()
        }
    }

    @Test
    fun observing_sync_change_complete_resets_view() {
        createFragment().onFragment { fragment ->
            fragment.swipeToRefresh!!.isRefreshing = true
            whenever(fragment.syncManagerViewNotifier.isSyncing).thenReturn(false)

            assertThat(fragment.swipeToRefresh!!.isRefreshing).isTrue()
            fragment.syncManagerChangeObserver.onSyncStatusChanged()

            assertThat(fragment.swipeToRefresh!!.isRefreshing).isFalse()
        }
    }

    @Module
    class FragmentModule {

        @Provides
        fun provideHistoryAdapter(): LightningHistoryAdapter = mock()

        @Provides
        fun provideLightningHistoryViewModel(): LightningHistoryViewModel {
            val viewModel = mock<LightningHistoryViewModel>()
            whenever(viewModel.loadInvoices()).thenReturn(mock())
            return viewModel
        }
    }
}