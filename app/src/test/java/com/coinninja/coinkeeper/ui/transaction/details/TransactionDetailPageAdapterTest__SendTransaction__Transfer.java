package com.coinninja.coinkeeper.ui.transaction.details;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.ConfirmationsView;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

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

import androidx.annotation.Nullable;

import static com.coinninja.android.helpers.Resources.getString;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.ConfirmationViewMatcher.configuredForTransaction;
import static com.coinninja.matchers.ConfirmationViewMatcher.stageIs;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.hasTag;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TransactionDetailPageAdapterTest__SendTransaction__Transfer {

    @Mock
    TransactionAdapterUtil adapterUtil;
    @Mock
    LazyList<TransactionsInvitesSummary> transactions;
    @Mock
    TransactionsInvitesSummary transaction;
    @Mock
    TransactionDetailObserver observer;
    private View page;
    @Mock
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
        page = withId(activity, R.id.page);
        when(adapterUtil.translateTransaction(any())).thenReturn(bindableTransaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(0L));
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.get(anyInt())).thenReturn(transaction);
        adapter.refreshData();
        adapter.setShowTransactionDetailRequestObserver(observer);

        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(0L);
        when(bindableTransaction.getHistoricalTransactionUSDValue()).thenReturn(0L);

        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.TRANSFER);
        when(bindableTransaction.getFeeCurrency()).thenReturn(new BTCCurrency());
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency());
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency());
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
    public void renders_send_icon_for_transfer() {
        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_send));
    }

    @Test
    public void renders_confirmations__step_2() {
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.UNCONFIRMED);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForTransaction());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_PENDING));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_4)));
    }

    @Test
    public void renders_confirmations__step_3() {
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.CONFIRMED);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForTransaction());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_COMPLETE));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_5)));
    }

    @Test
    public void transactions_that_have_been_added_to_block_offer_technical_details() {
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.UNCONFIRMED);
        when(bindableTransaction.getTxID()).thenReturn("-- txid --");

        adapter.bindTo(page, bindableTransaction);
        Button seeDetails = withId(page, R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer).onTransactionDetailsRequested(bindableTransaction);

        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction);
        seeDetails = withId(page, R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer, times(2)).onTransactionDetailsRequested(bindableTransaction);
    }

    @Test
    public void renders_receivers_contact_when_sent__name_when_available() {
        String phoneNumber = "(330) 555-1111";
        String name = "Joe Blow";
        when(bindableTransaction.getContactName()).thenReturn(name);
        when(bindableTransaction.getContactPhoneNumber()).thenReturn(phoneNumber);

        adapter.bindTo(page, bindableTransaction);

        TextView contact = withId(page, R.id.contact);
        assertThat(contact, hasText(getString(activity, R.string.send_to_self)));
    }

    @Test
    public void renders_value_of_transaction_in_users_base_currency() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));
        when(bindableTransaction.getFeeCurrency()).thenReturn(new BTCCurrency(10000L));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("-$0.10"));
    }

    @Test
    public void renders_value_of_transaction_in_crypto() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));
        when(bindableTransaction.getFeeCurrency()).thenReturn(new BTCCurrency(10000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.0001"));
    }

    public static class A extends Activity {
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            setTheme(R.style.CoinKeeperTheme_Dark_Toolbar);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.page_transaction_detail);
        }
    }
}