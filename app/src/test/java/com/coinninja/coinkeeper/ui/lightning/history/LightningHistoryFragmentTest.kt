package com.coinninja.coinkeeper.ui.lightning.history

import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.persistance.model.LedgerSettlementDetail
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.viewModel.WalletViewModel
import com.coinninja.coinkeeper.viewModel.WalletViewModelProvider
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
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
    fun init__observes_view_models() {
        createFragment().onFragment { fragment ->
            verify(fragment.walletViewModel.isLightningLocked).observe(fragment, fragment.isLightningLockedObserver)
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

    //LOCK
    @Test
    fun lock__shows_lock_when_locked() {
        createFragment().onFragment { fragment ->
            fragment.isLightningLockedObserver.onChanged(true)

            assertThat(fragment.swipeToRefresh?.visibility).isEqualTo(View.GONE)
            assertThat(fragment.lightningLock?.visibility).isEqualTo(View.VISIBLE)
        }
    }

    @Test
    fun lock__shows_transactions_when_not_locked() {
        createFragment().onFragment { fragment ->
            fragment.isLightningLockedObserver.onChanged(false)

            assertThat(fragment.swipeToRefresh?.visibility).isEqualTo(View.VISIBLE)
            assertThat(fragment.lightningLock?.visibility).isEqualTo(View.GONE)
        }
    }

    @Test
    fun checks_lock_status_after_each_sync() {
        createFragment().onFragment { fragment ->
            whenever(fragment.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)

            fragment.syncManagerChangeObserver.onSyncStatusChanged()
            fragment.syncManagerChangeObserver.onSyncStatusChanged()

            verify(fragment.walletViewModel, times(2)).checkLightningLock()
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

        @Provides
        fun provideWalletViewModelProvider(): WalletViewModelProvider {
            val walletViewModelProvider: WalletViewModelProvider = mock()
            val walletViewModel: WalletViewModel = mock()
            whenever(walletViewModelProvider.provide(any<BaseFragment>())).thenReturn(walletViewModel)
            whenever(walletViewModel.isLightningLocked).thenReturn(mock())
            return walletViewModelProvider
        }
    }
}