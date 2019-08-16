package com.coinninja.coinkeeper.ui.transaction.history;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
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

import static com.coinninja.android.helpers.Views.withId;
import static com.google.common.truth.Truth.assertThat;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TransactionHistoryDataAdapterTest {

    @Mock
    WalletHelper walletHelper;
    private DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
    @Mock
    private TransactionAdapterUtil transactionUtil;
    @Mock
    private TransactionHistoryDataAdapter.OnItemClickListener listener;
    @Mock
    private LazyList transactions;
    @Mock
    private Picasso picasso;
    @Mock
    private CircleTransform circleTransform;
    @Mock
    private Analytics analytics;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

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
        activity.appendLayout(R.layout.fragment_transaction_history);
        adapter = new TransactionHistoryDataAdapter(transactionUtil, defaultCurrencies, picasso,
                circleTransform, walletHelper, analytics, activityNavigationUtil);
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

        assertThat(adapter.getItemCount()).isEqualTo(500);
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
        assertThat(adapter.getDefaultCurrencies()).isEqualTo(defaultCurrencies);
    }

    @Test
    public void adds_footer_when_0_transactions() {
        when(transactions.size()).thenReturn(0);

        adapter.setTransactions(transactions);

        assertThat(adapter.getItemCount()).isEqualTo(1);
        assertThat(adapter.getItemViewType(0)).isEqualTo(TransactionHistoryDataAdapter.FOOTER_TYPE);
    }

    @Test
    public void adds_footer_when_1_transactions() {
        when(transactions.size()).thenReturn(1);

        adapter.setTransactions(transactions);

        assertThat(adapter.getItemCount()).isEqualTo(2);
        assertThat(adapter.getItemViewType(0)).isEqualTo(TransactionHistoryDataAdapter.STANDARD_TYPE);
        assertThat(adapter.getItemViewType(1)).isEqualTo(TransactionHistoryDataAdapter.FOOTER_TYPE);
    }

    @Test
    public void clicking_on_empty_state_navigates_and_reports() {
        when(transactions.size()).thenReturn(1);
        when(walletHelper.getBalance()).thenReturn(new BTCCurrency(1000));
        adapter.setTransactions(transactions);

        RecyclerView parent = activity.findViewById(R.id.transaction_history);
        parent.setLayoutManager(new LinearLayoutManager(activity));
        TransactionHistoryDataAdapter.ViewHolder holder = adapter.onCreateViewHolder(parent, adapter.getItemViewType(1));
        adapter.onBindViewHolder(holder, 1);

        holder.itemView.findViewById(R.id.get_bitcoin_button).performClick();
        verify(analytics).trackEvent(Analytics.EVENT_GET_BITCOIN);
        verify(activityNavigationUtil).navigateToBuyBitcoin(activity);

        holder.itemView.findViewById(R.id.learn_bitcoin_button).performClick();
        verify(analytics).trackEvent(Analytics.EVENT_GET_BITCOIN);
        verify(activityNavigationUtil).navigateToLearnBitcoin(activity);

        holder.itemView.findViewById(R.id.spend_bitcoin_button).performClick();
        verify(analytics).trackEvent(Analytics.EVENT_GET_BITCOIN);
        verify(activityNavigationUtil).navigateToSpendBitcoin(activity);

    }
}