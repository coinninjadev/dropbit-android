package com.coinninja.coinkeeper.ui.transaction.history

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import org.greenrobot.greendao.query.LazyList
import javax.inject.Inject

class TransactionHistoryFragment : BaseFragment(), TransactionHistoryDataAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, SyncManagerChangeObserver {

    @Inject
    internal lateinit var localBroadCastUtil: LocalBroadCastUtil
    @Inject
    internal lateinit var bitcoinUtil: BitcoinUtil
    @Inject
    internal lateinit var walletHelper: WalletHelper
    @Inject
    internal lateinit var transactionHistoryDataAdapter: TransactionHistoryDataAdapter
    @Inject
    internal lateinit var defaultCurrencyChangeViewNotifier: DefaultCurrencyChangeViewNotifier
    @Inject
    internal lateinit var currencyPreference: CurrencyPreference
    @Inject
    internal lateinit var activityNavigationUtil: ActivityNavigationUtil
    @Inject
    internal lateinit var syncWalletManager: SyncWalletManager
    @Inject
    internal lateinit var syncManagerViewNotifier: SyncManagerViewNotifier

    internal val intentFilter = IntentFilter(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED)

    internal lateinit var transactions: LazyList<TransactionsInvitesSummary>

    internal var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED == intent.action) {
                refreshTransactions()
            } else if (DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED == intent.action && intent.hasExtra(DropbitIntents.EXTRA_PREFERENCE)) {
                defaultCurrencyChangeViewNotifier.onDefaultCurrencyChanged(intent.getParcelableExtra(DropbitIntents.EXTRA_PREFERENCE))
            }
        }
    }

    override fun onItemClick(view: View, position: Int) {
        activity?.let {
            activityNavigationUtil.showTransactionDetail(it, transactionInviteSummaryID = transactions[position].id)
        }
    }

    override fun onRefresh() {
        syncWalletManager.syncNow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intentFilter.addAction(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_transaction_history, container, false)
    }

    override fun onStart() {
        super.onStart()
        transactions = walletHelper.transactionsLazily
    }

    override fun onResume() {
        super.onResume()
        transactionHistoryDataAdapter.defaultCurrencies = currencyPreference.currenciesPreference
        localBroadCastUtil.registerReceiver(receiver, intentFilter)
        transactionHistoryDataAdapter.setOnItemClickListener(this)
        transactionHistoryDataAdapter.setTransactions(transactions)
        transactionHistoryDataAdapter.setDefaultCurrencyChangeViewNotifier(defaultCurrencyChangeViewNotifier)
        syncManagerViewNotifier.observeSyncManagerChange(this)
        setupHistoryList()
        setupSwipeToRefresh()

    }

    override fun onPause() {
        super.onPause()
        transactionHistoryDataAdapter.setOnItemClickListener(null)
        localBroadCastUtil.unregisterReceiver(receiver)
    }

    //TODO Trigger by ViewModel
    fun onPriceReceived(price: USDCurrency) {
        transactionHistoryDataAdapter.notifyDataSetChanged()
    }

    //TODO Trigger by ViewModel
    fun onWalletSyncComplete() {
        refreshTransactions()
    }

    override fun onStop() {
        super.onStop()
        transactions.close()
    }

    private fun refreshTransactions() {
        transactions.close()
        transactions = walletHelper.transactionsLazily
        transactionHistoryDataAdapter.setTransactions(transactions)
        findViewById<RecyclerView>(R.id.transaction_history)?.let {
            it.adapter = transactionHistoryDataAdapter
        }
    }

    private fun setupHistoryList() {
        val transactionHistory = findViewById<RecyclerView>(R.id.transaction_history)
        transactionHistory?.visibility = View.VISIBLE
        val layoutManager = LinearLayoutManager(context)
        transactionHistory?.layoutManager = layoutManager
        transactionHistory?.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        transactionHistory?.setHasFixedSize(false)
        transactionHistory?.adapter = transactionHistoryDataAdapter
    }

    private fun setupSwipeToRefresh() {
        findViewById<SwipeRefreshLayout>(R.id.pull_to_refresh)?.setOnRefreshListener(this)
    }

    override fun onSyncStatusChanged() {
        view?.findViewById<SwipeRefreshLayout>(R.id.pull_to_refresh)?.isRefreshing = false
        refreshTransactions()
    }

}