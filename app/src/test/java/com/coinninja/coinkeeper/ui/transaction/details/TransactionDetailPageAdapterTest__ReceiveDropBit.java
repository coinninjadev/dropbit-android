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

import static com.coinninja.matchers.ActivityMatchers.noServiceStarted;
import static com.coinninja.matchers.ConfirmationViewMatcher.configuredForDropbit;
import static com.coinninja.matchers.ConfirmationViewMatcher.stageIs;
import static com.coinninja.matchers.ImageViewMatchers.hasContentDescription;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.hasTag;
import static com.coinninja.matchers.ViewMatcher.isGone;
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
public class TransactionDetailPageAdapterTest__ReceiveDropBit {

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
        when(transactions.get(anyInt())).thenReturn(transaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00d));
        bindableTransaction = new BindableTransaction(ApplicationProvider.getApplicationContext(), walletHelper);
        adapter.refreshData();
        adapter.setShowTransactionDetailRequestObserver(observer);

        bindableTransaction.setSendState(BindableTransaction.SendState.RECEIVE);
        bindableTransaction.setValue(0);
        bindableTransaction.setFee(0);
        bindableTransaction.setHistoricalInviteUSDValue(0L);
    }

    @After
    public void tearDown() {
        observer = null;
        activity = null;
        page = null;
        adapterUtil = null;
        bindableTransaction = null;
        walletHelper = null;
        adapter = null;
        transaction = null;
        transactions = null;
    }

    @Test
    public void renders_transfer_in_icon() {
        bindableTransaction.setSendState(BindableTransaction.SendState.UNLOAD_LIGHTNING);
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);

        assertThat(icon, hasTag(R.drawable.ic_transfer_in));

        TextView confirmations = page.findViewById(R.id.confirmations);
        assertThat(confirmations, hasText("Withdraw from Lightning"));
    }

    @Test
    public void renders_receive_icon() {
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);

        assertThat(icon, hasTag(R.drawable.ic_transaction_receive));
    }

    @Test
    public void canceled_dropbits_show_as_canceled() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        bindableTransaction.setSendState(BindableTransaction.SendState.RECEIVE_CANCELED);

        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(page.findViewById(R.id.confirmation_beads), isGone());
        assertThat(page.findViewById(R.id.confirmations), hasText(Resources.INSTANCE.getString(activity, R.string.transaction_details_dropbit_canceled)));
    }

    @Test
    public void expired_dropbits_show_as_expired() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);
        bindableTransaction.setSendState(BindableTransaction.SendState.RECEIVE_CANCELED);

        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(page.findViewById(R.id.confirmation_beads), isGone());
        assertThat(page.findViewById(R.id.confirmations), hasText(Resources.INSTANCE.getString(activity, R.string.transaction_details_dropbit_expired)));
    }

    @Test
    public void sets_content_description_for_icon() {
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = page.findViewById(R.id.ic_send_state);
        String contentDescription = Resources.INSTANCE.getString(page.getContext(), R.string.transaction_detail_cd_send_state__dropbit_received);
        assertThat(icon, hasContentDescription(contentDescription));
    }

    @Test
    public void renders_confirmations__step_1() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_PENDING);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = page.findViewById(R.id.confirmations);
        ConfirmationsView confirmationsView = page.findViewById(R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_DROPBIT_SENT));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_1)));
    }

    @Test
    public void renders_confirmations__step_2() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = page.findViewById(R.id.confirmations);
        ConfirmationsView confirmationsView = page.findViewById(R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_ADDRESS_RECEIVED));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.received_confirmations_view_stage_2)));
    }

    @Test
    public void renders_confirmations__step_4() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = page.findViewById(R.id.confirmations);
        ConfirmationsView confirmationsView = page.findViewById(R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_PENDING));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_4)));
    }

    @Test
    public void renders_confirmations__step_5() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = page.findViewById(R.id.confirmations);
        ConfirmationsView confirmationsView = page.findViewById(R.id.confirmation_beads);

        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmationsView.getContext(), R.string.confirmations_view_stage_5)));
    }

    @Test
    public void transactions_that_have_been_added_to_block_offer_technical_details() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);
        bindableTransaction.setTxID("-- txid --");

        when(adapterUtil.translateTransaction(any())).thenReturn(bindableTransaction);

        adapter.bindTo(page, bindableTransaction, 0);
        Button seeDetails = page.findViewById(R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer).onTransactionDetailsRequested(bindableTransaction);

        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction, 0);
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
    public void renders_crypto_value__receivers_do_not_see_fees_when_canceled() {
        bindableTransaction.setSendState(BindableTransaction.SendState.RECEIVE_CANCELED);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        bindableTransaction.setValue(50000000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = page.findViewById(R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50000L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50000000L));
    }

    @Test
    public void renders_crypto_value__receivers_do_not_see_fees_when_expired() {
        bindableTransaction.setSendState(BindableTransaction.SendState.RECEIVE_CANCELED);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);
        bindableTransaction.setValue(50000000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = page.findViewById(R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50000L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50000000L));
    }


    @Test
    public void renders_value___receivers_do_not_see_fees() {
        bindableTransaction.setFee(10000L);
        bindableTransaction.setValue(50000000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = page.findViewById(R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50000L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50000000L));
    }


    @Test
    public void renders_time_transaction_occurred() {
        bindableTransaction.setTxTime("April 24, 2018 01:24am");

        adapter.bindTo(page, bindableTransaction, 0);

        TextView value = page.findViewById(R.id.transaction_date);
        assertThat(value, hasText("April 24, 2018 01:24am"));
    }

    @Test
    public void does_not_allow_user_to_cancel_dropbit_when_not_pending() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_PENDING);
        adapter.bindTo(page, bindableTransaction, 0);
        TextView pendingDropbit = page.findViewById(R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = page.findViewById(R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = page.findViewById(R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = page.findViewById(R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = page.findViewById(R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());
    }

    @Test
    public void does_not_allow_user_to_cancel_drop_bit_when_pending() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_PENDING);
        String inviteId = "--- server invite id ---";
        bindableTransaction.setServerInviteId(inviteId);

        adapter.bindTo(page, bindableTransaction, 0);

        assertThat(page.findViewById(R.id.button_cancel_dropbit), isInvisible());
    }

    @Test
    public void does_not_allow_receiver_to_cancel_dropbit() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_PENDING);
        String inviteId = "--- server invite id ---";
        bindableTransaction.setServerInviteId(inviteId);
        adapter.bindTo(page, bindableTransaction, 0);

        page.findViewById(R.id.button_cancel_dropbit).performClick();

        assertThat(activity, noServiceStarted());
    }

    @Test
    public void bind_hides_call_show_details() {
        adapter.bindTo(page, bindableTransaction, 0);

        assertThat(page.findViewById(R.id.call_to_action), isInvisible());
        assertThat(page.findViewById(R.id.button_cancel_dropbit), isInvisible());
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