package com.coinninja.coinkeeper.ui.transaction.details;

import android.database.DataSetObserver;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.memo.MemoCreator;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.TwitterUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TransactionDetailPageAdapterTest {

    @Mock
    private LazyList<TransactionsInvitesSummary> transactions;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private TransactionAdapterUtil adapterUtil;

    @Mock
    MemoCreator memoCreator;

    @Mock
    TwitterUtil twitterUtil;

    @Mock
    Analytics analytics;

    private TransactionDetailPageAdapter pageAdapter;
    private TestableActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.page_transaction_detail);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(100);
        pageAdapter = new TransactionDetailPageAdapter(walletHelper, adapterUtil, new DefaultCurrencies(new USDCurrency(), new BTCCurrency()),
                memoCreator, twitterUtil, analytics);
        pageAdapter.refreshData();
    }

    @After
    public void tearDown() {
        transactions = null;
        walletHelper = null;
        adapterUtil = null;
        pageAdapter = null;
        activity = null;
    }

    @Test
    public void returns_count_for_transactions() {
        when(transactions.size()).thenReturn(100);

        assertThat(pageAdapter.getCount(), equalTo(100));
    }

    @Test
    public void refreshes_transaction_data_when_told() {
        LazyList<TransactionsInvitesSummary> t2 = mock(LazyList.class);
        when(t2.size()).thenReturn(101);
        when(walletHelper.getTransactionsLazily()).thenReturn(t2);

        assertThat(pageAdapter.getCount(), equalTo(100));
        DataSetObserver observer = mock(DataSetObserver.class);
        pageAdapter.registerDataSetObserver(observer);

        pageAdapter.refreshData();

        assertThat(pageAdapter.getCount(), equalTo(101));
        verify(observer).onChanged();
    }

    @Test
    public void looks_up_transactions_by_record_id() {
        TransactionsInvitesSummary t = mock(TransactionsInvitesSummary.class);
        TransactionsInvitesSummary f = mock(TransactionsInvitesSummary.class);
        when(f.getId()).thenReturn(3L);
        when(t.getId()).thenReturn(0L);

        when(transactions.get(anyInt())).thenReturn(t);
        when(transactions.get(5)).thenReturn(f);

        assertThat(pageAdapter.lookupTransactionById(3L), equalTo(5));
    }

    @Test
    public void returns_index_of_transaction_with_id() {
        String txid = "-- txid --";
        TransactionsInvitesSummary t = mock(TransactionsInvitesSummary.class);
        TransactionsInvitesSummary summary = mock(TransactionsInvitesSummary.class);
        TransactionSummary tSummary = new TransactionSummary();
        tSummary.setTxid(txid);
        when(summary.getTransactionSummary()).thenReturn(tSummary);

        when(transactions.get(anyInt())).thenReturn(t);
        when(transactions.get(5)).thenReturn(summary);

        pageAdapter.refreshData();

        assertThat(pageAdapter.lookupTransactionBy(txid), equalTo(5));
        assertThat(pageAdapter.lookupTransactionBy(null), equalTo(0));
        assertThat(pageAdapter.lookupTransactionBy("foo"), equalTo(0));
    }

    @Test
    public void tearing_down_closes_cursor() {
        pageAdapter.tearDown();

        verify(transactions).close();
    }

    @Test
    public void binds_value_of_transaction_to_currency_view() {
        DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier = mock(DefaultCurrencyChangeViewNotifier.class);
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());
        pageAdapter.setDefaultCurrencyChangeViewNotifier(defaultCurrencyChangeViewNotifier);
        pageAdapter.onDefaultCurrencyChanged(defaultCurrencies);

        verify(defaultCurrencyChangeViewNotifier).observeDefaultCurrencyChange(pageAdapter);
        assertThat(pageAdapter.getDefaultCurrencies(), equalTo(defaultCurrencies));
    }

    @Test
    public void sets_observer_on_view_when_rendering() {
        DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier = mock(DefaultCurrencyChangeViewNotifier.class);
        pageAdapter.setDefaultCurrencyChangeViewNotifier(defaultCurrencyChangeViewNotifier);
        BindableTransaction bindableTransaction = new BindableTransaction(ApplicationProvider.getApplicationContext(), mock(WalletHelper.class));
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND);
        bindableTransaction.setHistoricalInviteUSDValue(100L);

        pageAdapter.bindTo(withId(activity, R.id.test_root), bindableTransaction, 0);

        verify(defaultCurrencyChangeViewNotifier).observeDefaultCurrencyChange(withId(activity, R.id.default_currency_view));
    }
}