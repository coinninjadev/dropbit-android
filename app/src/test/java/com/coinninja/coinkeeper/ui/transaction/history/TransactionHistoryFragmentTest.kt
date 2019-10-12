package com.coinninja.coinkeeper.ui.transaction.history

import android.content.Intent
import android.view.View
import androidx.fragment.app.testing.FragmentScenario
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil
import com.coinninja.matchers.IntentFilterMatchers
import com.nhaarman.mockitokotlin2.whenever
import org.greenrobot.greendao.query.LazyList
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@Suppress("UNCHECKED_CAST")
@RunWith(AndroidJUnit4::class)
class TransactionHistoryFragmentTest {

    private val conversionCurrency = USDCurrency(1000.0)
    private val application get() = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()

    private fun setupFragment(numTransactions: Int = 12): FragmentScenario<TransactionHistoryFragment> {
        setupDI(numTransactions)
        val scenario = FragmentScenario.launchInContainer(TransactionHistoryFragment::class.java, null)

        scenario.moveToState(Lifecycle.State.RESUMED)
        return scenario
    }

    private fun setupDI(numTransactions: Int) {
        val transaction: TransactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)
        val transactions: LazyList<TransactionsInvitesSummary> = mock(LazyList::class.java) as LazyList<TransactionsInvitesSummary>
        val walletHelper = mock(WalletHelper::class.java)
        whenever(transactions.size).thenReturn(numTransactions)
        whenever(transactions.isEmpty()).thenReturn(numTransactions == 0)
        whenever(transactions[ArgumentMatchers.anyInt()]).thenReturn(transaction)
        whenever(walletHelper.transactionsLazily).thenReturn(transactions)
        whenever(walletHelper.latestPrice).thenReturn(conversionCurrency)
        whenever(walletHelper.balance).thenReturn(BTCCurrency(1000))
        val defaultCurrencies = DefaultCurrencies(BTCCurrency(), USDCurrency())
        application.walletHelper = walletHelper
        application.localBroadCastUtil = mock(LocalBroadCastUtil::class.java)
        application.bitcoinUtil = mock(BitcoinUtil::class.java)
        application.currencyPreference = mock(CurrencyPreference::class.java)
        application.activityNavigationUtil = mock(ActivityNavigationUtil::class.java)
        application.syncWalletManager = mock(SyncWalletManager::class.java)
        application.defaultCurrencyChangeViewNotifier = mock(DefaultCurrencyChangeViewNotifier::class.java)
        application.transactionHistoryDataAdapter = mock(TransactionHistoryDataAdapter::class.java)
        application.syncManagerViewNotifier = mock(SyncManagerViewNotifier::class.java)
        whenever(application.currencyPreference.toggleDefault()).thenReturn(defaultCurrencies)
        whenever(application.currencyPreference.currenciesPreference).thenReturn(defaultCurrencies)
    }

    @Test
    fun adds_default_currency_change_notifier_to_adapter() {
        val scenario = setupFragment()

        scenario.onFragment { fragment ->
            verify(fragment.transactionHistoryDataAdapter).setDefaultCurrencyChangeViewNotifier(fragment.defaultCurrencyChangeViewNotifier)
        }
    }

    @Test
    fun provides_adapter_default_currency_when_restarted() {
        val scenario = setupFragment()

        scenario.onFragment { fragment ->
            verify(fragment.transactionHistoryDataAdapter).setDefaultCurrencyChangeViewNotifier(fragment.defaultCurrencyChangeViewNotifier)
        }
    }

    @Test
    fun observes_item_selection() {
        val scenario = setupFragment()


        scenario.onFragment { fragment ->
            verify(fragment.transactionHistoryDataAdapter).setOnItemClickListener(fragment)
        }
    }

    @Test
    fun notifies_of_currency_preference_change() {
        val scenario = setupFragment()
        val defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        val intent = Intent(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED)
        intent.putExtra(DropbitIntents.EXTRA_PREFERENCE, defaultCurrencies)

        scenario.onFragment { fragment ->
            fragment.receiver.onReceive(fragment.context!!, intent)
            verify(fragment.defaultCurrencyChangeViewNotifier).onDefaultCurrencyChanged(ArgumentMatchers.any(DefaultCurrencies::class.java))
        }
    }

    @Test
    fun observes_local_events() {
        val scenario = setupFragment()

        scenario.onFragment { fragment ->
            verify(fragment.localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter)
            assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED))
            assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED))
        }
    }

    @Test
    fun stops_observing_actions_when_stopped() {
        val scenario = setupFragment()

        scenario.onFragment { fragment ->
            val receiver = fragment.receiver
            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(application.localBroadCastUtil).unregisterReceiver(receiver)
        }
    }

    @Test
    fun shows_transaction_detail_when_history_item_selected() {
        val summary = mock(TransactionsInvitesSummary::class.java)
        whenever(summary.id).thenReturn(4L)
        val scenario = setupFragment()
        val transactions = application.walletHelper.transactionsLazily
        whenever(transactions[3]).thenReturn(summary)

        scenario.onFragment { fragment ->
            fragment.onItemClick(mock(View::class.java), 3)


            verify(application.activityNavigationUtil).showTransactionDetail(fragment.activity!!, 4L)
        }
    }

    @Test
    fun shows_transactions_when_syncing_no_transactions_to_some_transactions() {
        val scenario = setupFragment(1)
        val updatedTransactions: LazyList<TransactionsInvitesSummary> = mock(LazyList::class.java) as LazyList<TransactionsInvitesSummary>
        whenever(updatedTransactions.size).thenReturn(2)
        whenever(updatedTransactions[ArgumentMatchers.anyInt()]).thenReturn(mock(TransactionsInvitesSummary::class.java))

        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            val list = fragment.findViewById<RecyclerView>(R.id.transaction_history)
            whenever(fragment.walletHelper.transactionsLazily).thenReturn(updatedTransactions)

            fragment.onWalletSyncComplete()

            verify(fragment.transactionHistoryDataAdapter).setTransactions(updatedTransactions)
            assertThat(list?.visibility, equalTo(View.VISIBLE))
        }

    }

    @Test
    fun refreshes_transactions_when_sync_completed() {
        val scenario = setupFragment(12)
        val updatedTransactions: LazyList<TransactionsInvitesSummary> = mock(LazyList::class.java) as LazyList<TransactionsInvitesSummary>
        whenever(updatedTransactions.size).thenReturn(13)
        whenever(updatedTransactions[ArgumentMatchers.anyInt()]).thenReturn(mock(TransactionsInvitesSummary::class.java))

        scenario.onFragment { fragment ->
            val transactions = fragment.walletHelper.transactionsLazily
            whenever(fragment.walletHelper.transactionsLazily).thenReturn(updatedTransactions)

            fragment.onWalletSyncComplete()

            verify(fragment.transactionHistoryDataAdapter).setTransactions(transactions)
            verify(fragment.transactionHistoryDataAdapter).setTransactions(updatedTransactions)
            verify(fragment.walletHelper, times(3)).transactionsLazily

        }
    }

    @Test
    fun prepares_recycle_view() {
        val scenario = setupFragment()
        scenario.onFragment {
            val recycleView = it.findViewById<RecyclerView>(R.id.transaction_history)!!
            assertFalse(recycleView.hasFixedSize())
            assertNotNull(recycleView.adapter)
        }
    }

    @Test
    fun closes_cursor_when_stopped() {
        val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        val scenario = setupFragment()

        scenario.moveToState(Lifecycle.State.DESTROYED)

        verify(application.walletHelper.transactionsLazily).close()
    }

    @Test
    fun refreshes_transactions_on_transaction_data_change() {
        val scenario = setupFragment(12)
        val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        val transactions = application.walletHelper.transactionsLazily
        val updatedTransactions: LazyList<TransactionsInvitesSummary> = mock(LazyList::class.java) as LazyList<TransactionsInvitesSummary>
        whenever(updatedTransactions.size).thenReturn(13)
        whenever(application.walletHelper.transactionsLazily).thenReturn(transactions).thenReturn(updatedTransactions)
        whenever(updatedTransactions[ArgumentMatchers.anyInt()]).thenReturn(mock(TransactionsInvitesSummary::class.java))
        scenario.moveToState(Lifecycle.State.RESUMED)

        scenario.onFragment { fragment ->
            fragment.receiver.onReceive(fragment.context!!, Intent(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED))

            verify(fragment.transactionHistoryDataAdapter, times(2)).setTransactions(transactions)
            verify(fragment.walletHelper, times(3)).transactionsLazily
        }
    }

    @Test
    fun subscribes_to_transaction_data_change() {
        val scenario = setupFragment()

        scenario.onFragment { fragment ->
            verify(fragment.localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter)
            assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED))
        }
    }

    @Test
    fun unsubscribe_from_transaction_data_changed_when_paused() {
        val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        val scenario = setupFragment()

        scenario.onFragment { fragment ->
            val receiver = fragment.receiver

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(application.localBroadCastUtil).unregisterReceiver(receiver)
        }
    }
}