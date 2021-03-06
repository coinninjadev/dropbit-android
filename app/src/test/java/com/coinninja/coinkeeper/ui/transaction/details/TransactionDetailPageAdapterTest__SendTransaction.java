package com.coinninja.coinkeeper.ui.transaction.details;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ApplicationProvider;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.view.ConfirmationsView;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import app.dropbit.commons.currency.USDCurrency;

import static com.coinninja.matchers.ConfirmationViewMatcher.configuredForTransaction;
import static com.coinninja.matchers.ConfirmationViewMatcher.stageIs;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.hasTag;
import static com.coinninja.matchers.ViewMatcher.isInvisible;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TransactionDetailPageAdapterTest__SendTransaction {

    @Mock
    TransactionAdapterUtil adapterUtil;
    @Mock
    LazyList<TransactionsInvitesSummary> transactions;
    @Mock
    TransactionsInvitesSummary transaction;
    @Mock
    TransactionDetailObserver observer;
    private View page;
    private BindableTransaction bindableTransaction;
    @Mock
    private WalletHelper walletHelper;
    @InjectMocks
    private TransactionDetailPageAdapter adapter;
    private A activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(A.class);
        page = activity.findViewById(R.id.page);
        when(adapterUtil.translateTransaction(any())).thenReturn(bindableTransaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(0L));
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00d));
        bindableTransaction = new BindableTransaction(ApplicationProvider.getApplicationContext(), walletHelper);
        bindableTransaction.setHistoricalInviteUSDValue(0L);
        bindableTransaction.setHistoricalTransactionUSDValue(0L);

        when(transactions.get(anyInt())).thenReturn(transaction);
        adapter.refreshData();
        adapter.setShowTransactionDetailRequestObserver(observer);

        bindableTransaction.setSendState(BindableTransaction.SendState.SEND);
        bindableTransaction.setValue(0L);
        bindableTransaction.setFee(0L);
    }

    @After
    public void tearDown() {
        page = null;
        adapterUtil = null;
        bindableTransaction = null;
        walletHelper = null;
        adapter = null;
        transaction = null;
        transactions = null;
    }

    @Test
    public void renders_canceled_icon_for_transaction_issues() {
        bindableTransaction.setSendState(BindableTransaction.SendState.FAILED_TO_BROADCAST_SEND);
        adapter.bindTo(page, bindableTransaction, 0);
        ImageView icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));

        bindableTransaction.setSendState(BindableTransaction.SendState.FAILED_TO_BROADCAST_TRANSFER);
        adapter.bindTo(page, bindableTransaction, 0);
        icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));

        bindableTransaction.setSendState(BindableTransaction.SendState.DOUBLESPEND_SEND);
        adapter.bindTo(page, bindableTransaction, 0);
        icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));

    }

    @Test
    public void load_lightning_send_icon() {
        bindableTransaction.setSendState(BindableTransaction.SendState.LOAD_LIGHTNING);
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transfer_out));

        TextView confirmations = page.findViewById(R.id.confirmations);
        assertThat(confirmations, hasText("Load Lightning"));
    }

    @Test
    public void renders_send_icon() {
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_send));
    }

    @Test
    public void renders_confirmations__step_2() {
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = page.findViewById(R.id.confirmations);
        ConfirmationsView confirmationsView = page.findViewById(R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForTransaction());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_PENDING));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_4)));
    }

    @Test
    public void renders_confirmations__step_3() {
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = page.findViewById(R.id.confirmations);

        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_5)));
    }

    @Test
    public void transactions_that_have_been_added_to_block_offer_technical_details() {
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);
        bindableTransaction.setTxID("-- txid --");

        when(adapterUtil.translateTransaction(any())).thenReturn(bindableTransaction);

        adapter.bindTo(page, bindableTransaction, 0);
        Button seeDetails = page.findViewById(R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer).onTransactionDetailsRequested(bindableTransaction);

        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction, 1);
        seeDetails = page.findViewById(R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer, times(2)).onTransactionDetailsRequested(bindableTransaction);
    }

    @Test
    public void renders_receivers_identity() {
        String name = "Joe Blow";
        bindableTransaction.setIdentity(name);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView contact = page.findViewById(R.id.identity);
        assertThat(contact, hasText(name));
    }

    @Test
    public void renders_value_of_transaction() {
        bindableTransaction.setValue(50000000L);
        bindableTransaction.setFee(10000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = page.findViewById(R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50010L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50010000L));
    }

    @Test
    public void renders_time_transaction_occurred() {
        bindableTransaction.setTxTime("April 24, 2018 01:24am");

        adapter.bindTo(page, bindableTransaction, 0);

        TextView value = page.findViewById(R.id.transaction_date);
        assertThat(value, hasText("April 24, 2018 01:24am"));
    }

    @Test
    public void bind_hides_call_show_details() {
        adapter.bindTo(page, bindableTransaction, 0);

        assertThat(page.findViewById(R.id.call_to_action), isInvisible());
    }

    public static class A extends AppCompatActivity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            setTheme(R.style.CoinKeeperTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.page_transaction_detail);
        }
    }
}