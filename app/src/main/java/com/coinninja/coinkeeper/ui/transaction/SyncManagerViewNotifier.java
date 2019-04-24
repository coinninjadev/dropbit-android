package com.coinninja.coinkeeper.ui.transaction;

import android.os.Handler;
import android.os.Looper;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

@CoinkeeperApplicationScope
public class SyncManagerViewNotifier implements SyncManagerChangeObserver {
    private List<WeakReference<SyncManagerChangeObserver>> observers;

    private boolean isSyncing = false;

    @Inject
    SyncManagerViewNotifier() {
        observers = new ArrayList<>();
    }

    public void observeSyncManagerChange(SyncManagerChangeObserver observer) {
        observers.add(new WeakReference<>(observer));
    }

    public void onSyncStatusChanged() {
        for (WeakReference<SyncManagerChangeObserver> observerWeakReference : observers) {
            if (observerWeakReference.get() != null) {
                observerWeakReference.get().onSyncStatusChanged();
            }
        }
    }

    synchronized public boolean isSyncing() {
        return isSyncing;
    }

    synchronized public void setSyncing(boolean syncing) {
        isSyncing = syncing;
        Handler main = new Handler(Looper.getMainLooper());
        main.post(() -> {
            onSyncStatusChanged();
        });
    }
}
