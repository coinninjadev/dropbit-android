package com.coinninja.coinkeeper.ui.transaction

import android.os.Handler
import android.os.Looper
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@Mockable
@CoinkeeperApplicationScope
class SyncManagerViewNotifier @Inject internal constructor() : SyncManagerChangeObserver {

    private val observers: MutableList<WeakReference<SyncManagerChangeObserver?>>

    @get:Synchronized
    var isSyncing = false
        @Synchronized set(syncing) {
            field = syncing
            val main = Handler(Looper.getMainLooper())
            main.post { this.onSyncStatusChanged() }
        }

    init {
        observers = ArrayList()
    }

    fun observeSyncManagerChange(observer: SyncManagerChangeObserver?) {
        observers.add(WeakReference(observer))
    }

    override fun onSyncStatusChanged() {
        for (observerWeakReference in observers) {
            observerWeakReference.get()?.onSyncStatusChanged()
        }
    }
}
