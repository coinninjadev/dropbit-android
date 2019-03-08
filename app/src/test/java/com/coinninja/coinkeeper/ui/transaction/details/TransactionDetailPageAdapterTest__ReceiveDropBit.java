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
    private View page;
    @Mock
    private BindableTransaction bindableTransaction;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    TransactionDetailObserver observer;
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

        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE);
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency());
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency());
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
    public void renders_receive_icon() {
        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);

        assertThat(icon, hasTag(R.drawable.ic_transaction_receive));
    }

    @Test
    public void canceled_dropbits_show_as_canceled() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE_CANCELED);

        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(withId(page, R.id.confirmation_beads), isGone());
        assertThat(withId(page, R.id.confirmations), hasText(getString(activity, R.string.transaction_details_dropbit_canceled)));
    }

    @Test
    public void expired_dropbits_show_as_expired() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE_CANCELED);

        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(withId(page, R.id.confirmation_beads), isGone());
        assertThat(withId(page, R.id.confirmations), hasText(getString(activity, R.string.transaction_details_dropbit_expired)));
    }

    @Test
    public void sets_content_description_for_icon() {
        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);
        String contentDescription = getString(page.getContext(), R.string.transaction_detail_cd_send_state__dropbit_received);
        assertThat(icon, hasContentDescription(contentDescription));
    }

    @Test
    public void renders_confirmations__step_1() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_PENDING);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_DROPBIT_SENT));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_1)));
    }

    @Test
    public void renders_confirmations__step_2() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_ADDRESS_RECEIVED));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_2)));
    }

    @Test
    public void renders_confirmations__step_4() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.UNCONFIRMED);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_PENDING));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_4)));
    }

    @Test
    public void renders_confirmations__step_5() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        when(bindableTransaction.getConfirmationState()).thenReturn(BindableTransaction.ConfirmationState.CONFIRMED);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_COMPLETE));
        assertThat(confirmations, hasText(getString(confirmationsView.getContext(), R.string.confirmations_view_stage_5)));
    }

    @Test
    public void transactions_that_have_been_added_to_block_offer_technical_details() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
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
    public void renders_receivers_contact_when_sent__phone_when_no_name_available() {
        String phoneNumber = "(330) 555-1111";
        when(bindableTransaction.getContactName()).thenReturn(null);
        when(bindableTransaction.getContactPhoneNumber()).thenReturn(phoneNumber);

        adapter.bindTo(page, bindableTransaction);

        TextView contact = withId(page, R.id.contact);
        assertThat(contact, hasText(phoneNumber));
    }

    @Test
    public void renders_receivers_contact_when_sent__name_when_available() {
        String phoneNumber = "(330) 555-1111";
        String name = "Joe Blow";
        when(bindableTransaction.getContactName()).thenReturn(name);
        when(bindableTransaction.getContactPhoneNumber()).thenReturn(phoneNumber);

        adapter.bindTo(page, bindableTransaction);

        TextView contact = withId(page, R.id.contact);
        assertThat(contact, hasText(name));
    }

    @Test
    public void renders_crypto_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_canceled() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.5"));
    }

    @Test
    public void renders_crypto_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_expired() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.5"));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_canceled() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("$500.00"));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_expired() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.RECEIVE_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("$500.00"));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("$500.00"));
    }

    @Test
    public void renders_value_of_dropbit_in_crypto__receivers_do_not_see_fees() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(50000000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.5"));
    }

    @Test
    public void renders_time_transaction_occurred() {
        when(bindableTransaction.getTxTime()).thenReturn("April 24, 2018 01:24am");

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.transaction_date);
        assertThat(value, hasText("April 24, 2018 01:24am"));
    }

    @Test
    public void does_not_allow_user_to_cancel_dropbit_when_not_pending() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_PENDING);
        adapter.bindTo(page, bindableTransaction);
        TextView pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        adapter.bindTo(page, bindableTransaction);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        adapter.bindTo(page, bindableTransaction);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        adapter.bindTo(page, bindableTransaction);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());
    }

    @Test
    public void does_not_allow_user_to_cancel_drop_bit_when_pending() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_PENDING);
        String inviteId = "--- server invite id ---";
        when(bindableTransaction.getServerInviteId()).thenReturn(inviteId);

        adapter.bindTo(page, bindableTransaction);

        assertThat(withId(page, R.id.button_cancel_dropbit), isInvisible());
    }

    @Test
    public void does_not_allow_receiver_to_cancel_dropbit() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.RECEIVED_PENDING);
        String inviteId = "--- server invite id ---";
        when(bindableTransaction.getServerInviteId()).thenReturn(inviteId);
        adapter.bindTo(page, bindableTransaction);

        withId(page, R.id.button_cancel_dropbit).performClick();

        assertThat(activity, noServiceStarted());
    }

    @Test
    public void bind_hides_call_show_details() {
        adapter.bindTo(page, bindableTransaction);

        assertThat(withId(page, R.id.call_to_action), isInvisible());
        assertThat(withId(page, R.id.button_cancel_dropbit), isInvisible());
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