package com.coinninja.coinkeeper.ui.transaction.details;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.android.helpers.Resources;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.dropbit.DropBitService;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
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

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static com.coinninja.android.helpers.Views.withId;
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
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class TransactionDetailPageAdapterTest__SendDropBit {

    @Mock
    private TransactionAdapterUtil adapterUtil;
    @Mock
    private LazyList<TransactionsInvitesSummary> transactions;
    @Mock
    private TransactionsInvitesSummary transaction;
    @Mock
    private TransactionDetailObserver observer;
    private View page;
    private BindableTransaction bindableTransaction;
    @Mock
    private WalletHelper walletHelper;
    @InjectMocks
    private TransactionDetailPageAdapter adapter;
    private TestableActivity activity;
    private ActivityScenario<TestableActivity> scenario;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        scenario = ActivityScenario.launch(TestableActivity.class);
        scenario.onActivity(a -> {
            activity = a;
            activity.appendLayout(R.layout.page_transaction_detail);
        });

        page = withId(activity, R.id.page);
        when(adapterUtil.translateTransaction(any())).thenReturn(bindableTransaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(0L));
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        when(transactions.get(anyInt())).thenReturn(transaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00d));
        bindableTransaction = new BindableTransaction(ApplicationProvider.getApplicationContext(), walletHelper);
        adapter.refreshData();
        adapter.setShowTransactionDetailRequestObserver(observer);
        adapter.onDefaultCurrencyChanged(new DefaultCurrencies(new USDCurrency(), new BTCCurrency()));

        bindableTransaction.setSendState(BindableTransaction.SendState.SEND);
        bindableTransaction.setHistoricalInviteUSDValue(0L);
        bindableTransaction.setHistoricalTransactionUSDValue(0);
        bindableTransaction.setValue(0);
        bindableTransaction.setFee(0);
    }

    @After
    public void tearDown() {
        page = null;
        adapterUtil = null;
        bindableTransaction = null;
        walletHelper = null;
        adapter = null;
        observer = null;
        activity = null;
        transaction = null;
        transactions = null;
        scenario.close();
        scenario = null;
    }

    @Test
    public void shows_memo() {
        String memo = "this is a memo";
        bindableTransaction.setMemo(memo);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView memoView = withId(activity, R.id.shared_memo_text_view);

        assertThat(memoView, hasText(memo));
    }

    @Test
    public void does_not_show_null_memo() {
        bindableTransaction.setMemo(null);

        adapter.bindTo(page, bindableTransaction, 0);

        View memoView = withId(activity, R.id.shared_transaction_subview);

        assertThat(memoView, isGone());
    }

    @Test
    public void does_not_show_empty_memo() {
        bindableTransaction.setMemo("");

        adapter.bindTo(page, bindableTransaction, 0);

        View memoView = withId(activity, R.id.shared_transaction_subview);

        assertThat(memoView, isGone());
    }

    @Test
    public void clicking_close_finishes_activity() {
        adapter.bindTo(page, bindableTransaction, 0);

        withId(activity, R.id.ic_close).performClick();

        assertTrue(activity.isFinishing());
    }

    @Test
    public void renders_send_icon() {
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_send));
    }

    @Test
    public void canceled_dropbits_show_as_canceled() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND_CANCELED);

        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(withId(page, R.id.confirmation_beads), isGone());
        assertThat(withId(page, R.id.confirmations), hasText(Resources.INSTANCE.getString(activity, R.string.transaction_details_dropbit_canceled)));
    }

    @Test
    public void expired_dropbits_show_as_expired() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND_CANCELED);

        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = withId(page, R.id.ic_send_state);
        assertThat(icon, hasTag(R.drawable.ic_transaction_canceled));
        assertThat(withId(page, R.id.confirmation_beads), isGone());
        assertThat(withId(page, R.id.confirmations), hasText(Resources.INSTANCE.getString(activity, R.string.transaction_details_dropbit_expired)));
    }

    @Test
    public void sets_content_description_for_icon() {
        adapter.bindTo(page, bindableTransaction, 0);

        ImageView icon = withId(page, R.id.ic_send_state);
        String contentDescription = Resources.INSTANCE.getString(page.getContext(), R.string.transaction_detail_cd_send_state__dropbit_sent);
        assertThat(icon, hasContentDescription(contentDescription));
    }

    @Test
    public void transaction_historic_price_with_invite() {
        bindableTransaction.setHistoricalInviteUSDValue(340000L);
        bindableTransaction.setHistoricalTransactionUSDValue(34000L);
        bindableTransaction.setTxID("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        bindableTransaction.setValue(26000000L);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 when received $88.40 at send"));
    }

    @Test
    public void transaction_historic_price_with_invite_zero_value() {
        bindableTransaction.setHistoricalInviteUSDValue(0L);
        bindableTransaction.setHistoricalTransactionUSDValue(34000L);
        bindableTransaction.setTxID("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        bindableTransaction.setValue(26000000L);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$88.40 at send"));
    }

    @Test
    public void transaction_historic_price_with_invite_tx_zero_value() {
        bindableTransaction.setHistoricalInviteUSDValue(340000L);
        bindableTransaction.setHistoricalTransactionUSDValue(0L);
        bindableTransaction.setTxID("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        bindableTransaction.setValue(26000000L);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 when received "));
    }

    @Test
    public void only_invite_historic_price_with_invite_when_no_transaction_canceled() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND_CANCELED);
        bindableTransaction.setHistoricalInviteUSDValue(340000L);
        bindableTransaction.setHistoricalTransactionUSDValue(34000L);
        bindableTransaction.setTxID("dsr98gy35g987whg98w4tw4809w4hjg80w9s");
        bindableTransaction.setValue(26000000L);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 at send"));
    }


    @Test
    public void only_invite_historic_price_with_invite_when_no_transaction() {
        bindableTransaction.setHistoricalInviteUSDValue(340000L);
        bindableTransaction.setValue(26000000L);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView view = withId(page, R.id.value_when_sent);

        assertThat(view, hasText("$884.00 when received "));
    }

    @Test
    public void renders_confirmations__step_1() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_DROPBIT_SENT));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_1)));
    }

    @Test
    public void renders_confirmations__step_2() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_ADDRESS_RECEIVED));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_2)));
    }

    @Test
    public void renders_confirmations__step_4() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_PENDING));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmations.getContext(), R.string.confirmations_view_stage_4)));
    }

    @Test
    public void renders_confirmations__step_5() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView confirmations = withId(page, R.id.confirmations);
        ConfirmationsView confirmationsView = withId(page, R.id.confirmation_beads);

        assertThat(confirmationsView, configuredForDropbit());
        assertThat(confirmationsView, stageIs(ConfirmationsView.STAGE_COMPLETE));
        assertThat(confirmations, hasText(Resources.INSTANCE.getString(confirmationsView.getContext(), R.string.confirmations_view_stage_5)));
    }

    @Test
    public void transactions_that_have_been_added_to_block_offer_technical_details() {
        when(adapterUtil.translateTransaction(any())).thenReturn(bindableTransaction);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.UNCONFIRMED);
        bindableTransaction.setTxID("-- txid --");

        adapter.bindTo(page, bindableTransaction, 0);
        Button seeDetails = withId(page, R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer).onTransactionDetailsRequested(bindableTransaction);

        bindableTransaction.setConfirmationState(BindableTransaction.ConfirmationState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction, 1);
        seeDetails = withId(page, R.id.call_to_action);
        assertThat(seeDetails, isVisible());
        seeDetails.performClick();
        verify(observer, times(2)).onTransactionDetailsRequested(bindableTransaction);
    }


    @Test
    public void renders_receivers_identity() {
        String name = "Joe Blow";
        bindableTransaction.setIdentity(name);

        adapter.bindTo(page, bindableTransaction, 0);

        TextView contact = withId(page, R.id.identity);
        assertThat(contact, hasText(name));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_canceled() {
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND_CANCELED);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        bindableTransaction.setValue(50000000L);
        bindableTransaction.setFee(10000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = withId(page, R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50010L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50010000L));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency__receivers_do_not_see_fees_when_expired() {
        bindableTransaction.setSendState(BindableTransaction.SendState.SEND_CANCELED);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);
        bindableTransaction.setValue(50000000L);
        bindableTransaction.setFee(10000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = withId(page, R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50010L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50010000L));
    }

    @Test
    public void renders_value_of_dropbit_in_users_base_currency() {
        bindableTransaction.setValue(50000000L);
        bindableTransaction.setFee(10000L);

        adapter.bindTo(page, bindableTransaction, 0);

        DefaultCurrencyDisplayView view = withId(page, R.id.default_currency_view);
        assertThat(view.getFiatValue().toLong(), equalTo(50010L));
        assertThat(view.getTotalCrypto().toLong(), equalTo(50010000L));
    }

    @Test
    public void renders_time_transaction_occurred() {
        bindableTransaction.setTxTime("April 24, 2018 01:24am");

        adapter.bindTo(page, bindableTransaction, 0);

        TextView value = withId(page, R.id.transaction_date);
        assertThat(value, hasText("April 24, 2018 01:24am"));
    }

    @Test
    public void does_not_allow_user_to_cancel_dropbit_when_not_pending() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_ADDRESS_PROVIDED);
        adapter.bindTo(page, bindableTransaction, 0);
        TextView pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.RECEIVED_ADDRESS_PROVIDED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());

        bindableTransaction.setInviteState(BindableTransaction.InviteState.CONFIRMED);
        adapter.bindTo(page, bindableTransaction, 0);
        pendingDropbit = withId(page, R.id.button_cancel_dropbit);
        assertThat(pendingDropbit, isInvisible());
    }

    @Test
    public void allows_user_to_cancel_drop_bit_when_pending() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);
        String inviteId = "--- server invite id ---";
        bindableTransaction.setServerInviteId(inviteId);

        adapter.bindTo(page, bindableTransaction, 0);

        Button cancelButton = withId(page, R.id.button_cancel_dropbit);
        assertThat(cancelButton, hasText(Resources.INSTANCE.getString(cancelButton.getContext(), R.string.cancel_dropbit)));
        assertThat(cancelButton, isVisible());
        assertThat(cancelButton, hasTag(inviteId));
    }

    @Test
    public void canceling_dropbit_starts_cancel_service() {
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);
        String inviteId = "--- server invite id ---";
        bindableTransaction.setServerInviteId(inviteId);
        adapter.bindTo(page, bindableTransaction, 0);

        withId(page, R.id.button_cancel_dropbit).performClick();

        Intents.intending(hasAction(DropbitIntents.ACTION_CANCEL_DROPBIT));
        Intents.intending(hasComponent(DropBitService.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_INVITATION_ID));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_INVITATION_ID, inviteId));
    }

    @Test
    public void bind_hides_call_show_details() {
        adapter.bindTo(page, bindableTransaction, 0);

        assertThat(withId(page, R.id.call_to_action), isInvisible());
    }
}