package com.coinninja.coinkeeper.ui.transaction.details;

import android.database.DataSetObserver;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.memo.MemoCreator;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.TwitterUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
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

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.USDCurrency;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TransactionDetailPageAdapterTest {

    @Mock
    MemoCreator memoCreator;
    @Mock
    TwitterUtil twitterUtil;
    @Mock
    Analytics analytics;
    @Mock
    private LazyList<TransactionsInvitesSummary> transactions;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private TransactionAdapterUtil adapterUtil;
    private TransactionDetailPageAdapter pageAdapter;
    private TestableActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.page_transaction_detail);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.size()).thenReturn(100);
        pageAdapter = new TransactionDetailPageAdapter(walletHelper, adapterUtil,
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
}