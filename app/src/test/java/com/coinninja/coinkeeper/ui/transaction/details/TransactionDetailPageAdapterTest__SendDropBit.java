package com.coinninja.coinkeeper.ui.transaction.details;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.dropbit.DropBitService;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.Intents;
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
import static com.coinninja.matchers.ActivityMatchers.serviceWithIntentStarted;
import static com.coinninja.matchers.ConfirmationViewMatcher.configuredForDropbit;
import static com.coinninja.matchers.ConfirmationViewMatcher.stageIs;
import static com.coinninja.matchers.ImageViewMatchers.hasContentDescription;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.hasTag;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isInvisible;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class TransactionDetailPageAdapterTest__SendDropBit {

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

        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND);
        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(0L);
        when(bindableTransaction.getHistoricalTransactionUSDValue()).thenReturn(0L);
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency());
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency());
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
    public void shows_memo() {
        String memo = "this is a memo";
        when(bindableTransaction.getMemo()).thenReturn(memo);

        adapter.bindTo(page, bindableTransaction);

        TextView memoView = withId(activity, R.id.shared_memo_text_view);

        assertThat(memoView, hasText(memo));
    }

    @Test
    public void does_not_show_null_memo() {
        when(bindableTransaction.getMemo()).thenReturn(null);

        adapter.bindTo(page, bindableTransaction);

        View memoView = withId(activity, R.id.shared_transaction_subview);

        assertThat(memoView, isGone());
    }

    @Test
    public void does_not_show_empty_memo() {
        when(bindableTransaction.getMemo()).thenReturn("");

        adapter.bindTo(page, bindableTransaction);

        View memoView = withId(activity, R.id.shared_transaction_subview);

        assertThat(memoView, isGone());
    }

    @Test
    public void clicking_close_finishes_activity() {
        adapter.bindTo(page, bindableTransaction);

        withId(activity, R.id.ic_close).performClick();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void renders_send_icon() {
        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_send));
    }

    @Test
    public void canceled_dropbits_show_as_canceled() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);

        adapter.bindTo(page, bindableTransaction);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(withId(page, R.id.confirmation_beads), isGone());
        assertThat(withId(page, R.id.confirmations), hasText(getString(activity, R.string.transaction_details_dropbit_canceled)));
    }

    @Test
    public void expired_dropbits_show_as_expired() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);

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
        String contentDescription = getString(page.getContext(), R.string.transaction_detail_cd_send_state__dropbit_sent);
        assertThat(icon, hasContentDescription(contentDescription));
    }

    @Test
    public void transaction_historic_price_with_invite() {
        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(340000l);
        when(bindableTransaction.getHistoricalTransactionUSDValue()).thenReturn(34000l);
        when(bindableTransaction.getTxID()).thenReturn("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(26000000l));
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 when received $88.40 at send"));
    }

    @Test
    public void transaction_historic_price_with_invite_zero_value() {
        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(0l);
        when(bindableTransaction.getHistoricalTransactionUSDValue()).thenReturn(34000l);
        when(bindableTransaction.getTxID()).thenReturn("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(26000000l));
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$88.40 at send"));
    }

    @Test
    public void transaction_historic_price_with_invite_tx_zero_value() {
        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(340000l);
        when(bindableTransaction.getHistoricalTransactionUSDValue()).thenReturn(0l);
        when(bindableTransaction.getTxID()).thenReturn("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(26000000l));
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 when received "));
    }

    @Test
    public void only_invite_historic_price_with_invite_when_no_transaction_canceled() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);
        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(340000l);
        when(bindableTransaction.getHistoricalTransactionUSDValue()).thenReturn(34000l);
        when(bindableTransaction.getTxID()).thenReturn("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(26000000l));

        adapter.bindTo(page, bindableTransaction);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 at send"));
    }


    @Test
    public void only_invite_historic_price_with_invite_when_no_transaction() {
        when(bindableTransaction.getHistoricalInviteUSDValue()).thenReturn(340000l);
        when(bindableTransaction.getValueCurrency()).thenReturn(new BTCCurrency(26000000l));
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 when received"));
    }

    @Test
    public void renders_confirmations__step_1() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_DROPBIT_SENT));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_1)));
    }

    @Test
    public void renders_confirmations__step_2() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);

        adapter.bindTo(page, bindableTransaction);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_ADDRESS_RECEIVED));
        assertThat(confirmations, hasText(getString(confirmations.getContext(), R.string.confirmations_view_stage_2)));
    }

    @Test
    public void renders_confirmations__step_4() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
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
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
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
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_canceled() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("-$500.10"));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_expired() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("-$500.10"));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.primary_value);
        assertThat(value, hasText("-$500.10"));
    }

    @Test
    public void renders_crypto_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_canceled() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.CANCELED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.5001"));
    }

    @Test
    public void renders_crypto_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_expired() {
        when(bindableTransaction.getSendState()).thenReturn(BindableTransaction.SendState.SEND_CANCELED);
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.EXPIRED);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.5001"));
    }

    @Test
    public void renders_value_of_dropbit_in_crypto() {
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency("1000.00"));
        when(bindableTransaction.getTotalTransactionCostCurrency()).thenReturn(new BTCCurrency(50010000L));

        adapter.bindTo(page, bindableTransaction);

        TextView value = withId(page, R.id.secondary_value);
        assertThat(value, hasText("0.5001"));
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
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
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
    public void allows_user_to_cancel_drop_bit_when_pending() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);
        String inviteId = "--- server invite id ---";
        when(bindableTransaction.getServerInviteId()).thenReturn(inviteId);

        adapter.bindTo(page, bindableTransaction);

        Button cancelButton = withId(page, R.id.button_cancel_dropbit);
        assertThat(cancelButton, hasText(Resources.getString(cancelButton.getContext(), R.string.cancel_dropbit)));
        assertThat(cancelButton, isVisible());
        assertThat(cancelButton, hasTag(inviteId));
    }

    @Test
    public void canceling_dropbit_starts_cancel_service() {
        when(bindableTransaction.getInviteState()).thenReturn(BindableTransaction.InviteState.SENT_PENDING);
        String inviteId = "--- server invite id ---";
        when(bindableTransaction.getServerInviteId()).thenReturn(inviteId);
        adapter.bindTo(page, bindableTransaction);

        withId(page, R.id.button_cancel_dropbit).performClick();

        Intent intent = new Intent(activity, DropBitService.class);
        intent.setAction(Intents.ACTION_CANCEL_DROPBIT);
        intent.putExtra(Intents.EXTRA_INVITATION_ID, inviteId);
        assertThat(activity, serviceWithIntentStarted(intent));
    }

    @Test
    public void bind_hides_call_show_details() {
        adapter.bindTo(page, bindableTransaction);

        assertThat(withId(page, R.id.call_to_action), isInvisible());
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