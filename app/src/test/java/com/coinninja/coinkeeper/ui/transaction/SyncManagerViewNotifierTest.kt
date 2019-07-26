package com.coinninja.coinkeeper.ui.transaction

import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test
import org.mockito.Mockito.verify


class SyncManagerViewNotifierTest {

    @Test
    fun notifies_observer_of_preference_change() {
        val observer1: SyncManagerChangeObserver = mock()
        var observer2: SyncManagerChangeObserver? = mock()

        val notifier = SyncManagerViewNotifier()
        notifier.observeSyncManagerChange(observer1)
        notifier.observeSyncManagerChange(observer2)
        System.gc()

        observer2 = null

        notifier.onSyncStatusChanged()

        verify(observer1).onSyncStatusChanged()
    }

}
