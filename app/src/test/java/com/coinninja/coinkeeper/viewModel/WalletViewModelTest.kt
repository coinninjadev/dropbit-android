package com.coinninja.coinkeeper.viewModel

import androidx.lifecycle.Observer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletViewModelTest {

    private fun createViewModel() = WalletViewModel(mock(), mock(), mock())
    private fun createScenario(): ActivityScenario<TestableActivity> = ActivityScenario.launch(TestableActivity::class.java)

    @Test
    fun invalidates_chain_holdings_when_notified_sync_is_completed() {
        val scenario = createScenario()
        val viewModel = createViewModel()
        whenever(viewModel.syncManagerViewNotifier.isSyncing).thenReturn(true).thenReturn(false)

        val observer = mock<Observer<in Boolean>>()
        scenario.onActivity { activity ->
            viewModel.syncInProgress.observe(activity, observer)
        }

        viewModel.syncChangeObserver.onSyncStatusChanged()
        verify(observer).onChanged(true)

        viewModel.syncChangeObserver.onSyncStatusChanged()
        verify(observer).onChanged(true)

        // registers observer
        verify(viewModel.syncManagerViewNotifier).observeSyncManagerChange(viewModel.syncChangeObserver)
    }
}