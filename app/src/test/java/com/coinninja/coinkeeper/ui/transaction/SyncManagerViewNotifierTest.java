package com.coinninja.coinkeeper.ui.transaction;

import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class SyncManagerViewNotifierTest{

    @Mock
    SyncManagerChangeObserver observer1;

    @Mock
    SyncManagerChangeObserver observer2;

    SyncManagerViewNotifier notifier = new SyncManagerViewNotifier();

    @After
    public void tearDown() {
        observer1 = null;
        observer2 = null;
        notifier = null;
    }

    @Test
    public void notifies_observer_of_preference_change() {
        notifier.observeSyncManagerChange(observer1);
        notifier.observeSyncManagerChange(observer2);
        System.gc();
        observer2 = null;

        notifier.onSyncStatusChanged();

        verify(observer1).onSyncStatusChanged();
    }

}
