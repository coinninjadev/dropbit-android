package com.coinninja.coinkeeper.ui.lightning.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import app.coinninja.cn.persistance.model.LedgerSettlementDetail
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.ui.transaction.history.SyncManagerChangeObserver
import javax.inject.Inject

class LightningHistoryFragment : BaseFragment() {

    @Inject
    lateinit var syncWalletManager: SyncWalletManager
    @Inject
    lateinit var syncManagerViewNotifier: SyncManagerViewNotifier
    @Inject
    lateinit var lightningHistoryViewModel: LightningHistoryViewModel
    @Inject
    lateinit var lightningHistoryAdapter: LightningHistoryAdapter

    val swipeToRefresh: SwipeRefreshLayout? get() = findViewById(R.id.pull_to_refresh)
    val history: RecyclerView? get() = findViewById(R.id.transaction_history)

    val onRefreshListener: OnRefreshListener = OnRefreshListener {
        syncWalletManager.syncNow()
    }

    val invoiceChangeObserver = Observer<List<LedgerSettlementDetail>> { invoices ->
        history?.let {
            (it.adapter as LightningHistoryAdapter).settlements = invoices
        }
    }

    val syncManagerChangeObserver = SyncManagerChangeObserver {
        if (!syncManagerViewNotifier.isSyncing)
            swipeToRefresh?.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false)
    }

    override fun onResume() {
        super.onResume()
        history?.let {
            val layoutManager = LinearLayoutManager(context)
            it.layoutManager = layoutManager
            it.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
            it.setHasFixedSize(false)
            it.adapter = lightningHistoryAdapter
        }
        swipeToRefresh?.setOnRefreshListener(onRefreshListener)
        syncManagerViewNotifier.observeSyncManagerChange(syncManagerChangeObserver)
        lightningHistoryViewModel.loadInvoices().observe(this, invoiceChangeObserver)
    }
}
