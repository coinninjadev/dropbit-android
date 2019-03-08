package com.coinninja.coinkeeper.view.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class TransactionHistoryDataAdapterTest {

    private TransactionHistoryDataAdapter adapter;

    private LazyList transactions;

    private TransactionHistoryActivity activity;

    @Before
    public void setUp() {
        TransactionAdapterUtil transactionUtil = mock(TransactionAdapterUtil.class);
        TransactionHistoryDataAdapter.OnItemClickListener listener = mock(TransactionHistoryDataAdapter.OnItemClickListener.class);
        CoinKeeperApplication application = (CoinKeeperApplication) RuntimeEnvironment.application;
        transactions = mock(LazyList.class);
        WalletHelper walletHelper = application.getUser().getWalletHelper();
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.isEmpty()).thenReturn(false);
        when(transactions.isEmpty()).thenReturn(false);
        activity = Robolectric.setupActivity(TransactionHistoryActivity.class);
        adapter = new TransactionHistoryDataAdapter(transactions, listener, transactionUtil);
    }

    @After
    public void tearDown() {
        adapter = null;
        transactions = null;
        activity = null;
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
    public void acceptsConversionCurrency() {
        USDCurrency currency = new USDCurrency();
        adapter.setConversionCurrency(currency);
        assertThat(adapter.getConversionCurrency().toFormattedCurrency(), equalTo("$0.00"));

        adapter.setConversionCurrency(new USDCurrency("1.00"));
        assertThat(adapter.getConversionCurrency().toFormattedCurrency(), equalTo("$1.00"));
    }

    @Test
    public void show_phone_number_if_contact_name_is_null_test() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TransactionHistoryDataAdapter.ViewHolder adapterViewHolder = new TransactionHistoryDataAdapter.ViewHolder(mock(View.class), mock(TransactionHistoryDataAdapter.OnItemClickListener.class));

        BindableTransaction bindable = new BindableTransaction();
        bindable.setContactName(null);
        bindable.setContactPhoneNumber("(222) 333-4444");

        adapterViewHolder.bindIdentifyingTarget(textView, bindable);

        assertThat(textView.getText(), equalTo("(222) 333-4444"));
    }

    @Test
    public void show_phone_number_if_contact_name_is_empty_test() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TransactionHistoryDataAdapter.ViewHolder adapterViewHolder = new TransactionHistoryDataAdapter.ViewHolder(mock(View.class), mock(TransactionHistoryDataAdapter.OnItemClickListener.class));

        BindableTransaction bindable = new BindableTransaction();
        bindable.setContactName("");
        bindable.setContactPhoneNumber("+12223334444");

        adapterViewHolder.bindIdentifyingTarget(textView, bindable);

        assertThat(textView.getText(), equalTo("(222) 333-4444"));
    }

    @Test
    public void show_contact_name_if_available_test() {
        TextView textView = new TextView(RuntimeEnvironment.application);
        TransactionHistoryDataAdapter.ViewHolder adapterViewHolder = new TransactionHistoryDataAdapter.ViewHolder(mock(View.class), mock(TransactionHistoryDataAdapter.OnItemClickListener.class));

        BindableTransaction bindable = new BindableTransaction();
        bindable.setContactName("Jeff");
        bindable.setContactPhoneNumber("(222) 333-4444");

        adapterViewHolder.bindIdentifyingTarget(textView, bindable);

        assertThat(textView.getText(), equalTo("Jeff"));
    }
}