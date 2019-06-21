package com.coinninja.coinkeeper.ui.transaction.history;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.util.image.CircleTransform;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.coinninja.android.helpers.Views.withId;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TransactionHistoryDataAdapterTest {


    private DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());

    @Mock
    private TransactionAdapterUtil transactionUtil;
    @Mock
    private TransactionHistoryDataAdapter.OnItemClickListener listener;
    @Mock
    private LazyList transactions;
    @Mock
    WalletHelper walletHelper;
    @Mock
    private Picasso picasso;
    @Mock
    private CircleTransform circleTransform;


    private TransactionHistoryDataAdapter adapter;
    private TestableActivity activity;
    private BindableTransaction bindableTransaction;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bindableTransaction = new BindableTransaction(ApplicationProvider.getApplicationContext(), walletHelper);
        when(transactions.isEmpty()).thenReturn(false);
        when(transactions.isClosed()).thenReturn(false);
        activity = Robolectric.setupActivity(TestableActivity.class);
        activity.appendLayout(R.layout.activity_transaction_history);
        adapter = new TransactionHistoryDataAdapter(transactionUtil, defaultCurrencies, picasso, circleTransform);
        adapter.setTransactions(transactions);
        adapter.setOnItemClickListener(listener);
    }

    @After
    public void tearDown() {
        adapter = null;
        transactions = null;
        activity = null;
        bindableTransaction = null;
        walletHelper = null;
    }

    @Test
    public void adapter_recounts_size_of_data() {
        when(transactions.size()).thenReturn(500);

        assertThat(adapter.getItemCount(), equalTo(500));
    }

    @Test
    public void inflates_view_for_holder() {
        View list = activity.findViewById(R.id.transaction_history);
        ViewGroup parent = new RelativeLayout(list.getContext());

        assertNotNull(adapter.onCreateViewHolder(parent, 0));
    }

    @Test
    public void registers_currency_to_change_observer() {
        DefaultCurrencyChangeViewNotifier notifier = mock(DefaultCurrencyChangeViewNotifier.class);
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND);
        adapter.setDefaultCurrencyChangeViewNotifier(notifier);
        TransactionHistoryDataAdapter.ViewHolder viewHolder = adapter.onCreateViewHolder(withId(activity, R.id.test_root), 0);
        viewHolder.setDefaultCurrencyChangeViewNotifier(notifier);

        viewHolder.bindToTransaction(bindableTransaction, defaultCurrencies, picasso, circleTransform);

        verify(notifier).observeDefaultCurrencyChange(withId(viewHolder.itemView, R.id.default_currency_view));
    }

    @Test
    public void observes_currency_preference_change() {
        DefaultCurrencyChangeViewNotifier notifier = mock(DefaultCurrencyChangeViewNotifier.class);
        DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new BTCCurrency(), new USDCurrency());

        adapter.setDefaultCurrencyChangeViewNotifier(notifier);
        adapter.onDefaultCurrencyChanged(defaultCurrencies);

        verify(notifier).observeDefaultCurrencyChange(adapter);
        assertThat(adapter.getDefaultCurrencies(), equalTo(defaultCurrencies));
    }
}