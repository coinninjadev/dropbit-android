package com.coinninja.coinkeeper.ui.transaction.history;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.image.CircleTransform;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.ConfirmationState;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;
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

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.USDCurrency;

import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class, qualifiers = "en-rUS")
public class TransactionHistoryDataBinderTest {
    @Mock
    WalletHelper walletHelper;
    @Mock
    private LazyList transactions;
    @Mock
    private TransactionsInvitesSummary transaction;
    @Mock
    private TransactionHistoryDataAdapter.OnItemClickListener onClickListener;
    @Mock
    private TransactionAdapterUtil transactionAdapterUtil;
    @Mock
    private Picasso picasso;
    @Mock
    private CircleTransform circleTransform;
    @Mock
    private Analytics analytics;
    @Mock
    private ActivityNavigationUtil activityNavigationUtil;

    private View view;
    private TransactionHistoryDataAdapter.ViewHolder viewHolder;
    private TransactionHistoryDataAdapter adapter;
    private DefaultCurrencies defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
    private BindableTransaction bindableTransaction;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(transactions.isEmpty()).thenReturn(false);
        when(transactions.isClosed()).thenReturn(false);
        when(transactions.size()).thenReturn(2);
        when(transactions.get(0)).thenReturn(transaction);
        TestableActivity activity = Robolectric.setupActivity(TestableActivity.class);
        ViewGroup parent = activity.findViewById(R.id.test_root);
        bindableTransaction = new BindableTransaction(ApplicationProvider.getApplicationContext(), walletHelper);
        when(transactionAdapterUtil.translateTransaction(any(TransactionsInvitesSummary.class))).thenReturn(bindableTransaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00d));
        adapter = new TransactionHistoryDataAdapter(transactionAdapterUtil, defaultCurrencies,
                picasso, circleTransform, walletHelper, analytics, activityNavigationUtil);
        adapter.setOnItemClickListener(onClickListener);
        adapter.setTransactions(transactions);
        bindableTransaction.setSendState(SendState.SEND);
        viewHolder = adapter.onCreateViewHolder(parent, 0);
        view = viewHolder.getItemView();
    }

    @After
    public void tearDown() {
        view = null;
        transactions = null;
        transaction = null;
        bindableTransaction = null;
        transactionAdapterUtil = null;
        defaultCurrencies = null;
    }

    // Transfer
    public void setupTransfer() {
        setupReceive();
        bindableTransaction.setSendState(SendState.TRANSFER);
    }

    // Invite
    public void setupInviteSend() {
        String contactName = "Joe Blow";
        when(transactionAdapterUtil.translateTransaction(any(TransactionsInvitesSummary.class))).thenReturn(bindableTransaction);
        bindableTransaction.setSendState(SendState.SEND);
        bindableTransaction.setIdentity(contactName);
        bindableTransaction.setInviteState(BindableTransaction.InviteState.SENT_PENDING);
        bindableTransaction.setValue(449893L);
        bindableTransaction.setFee(167L);

    }

    // Send
    public void setupSend() {
        setupReceive();
        bindableTransaction.setSendState(SendState.SEND);
    }

    // Receive
    public void setupReceive() {
        bindableTransaction.setSendState(SendState.RECEIVE);
        bindableTransaction.setConfirmationState(ConfirmationState.CONFIRMED);
        bindableTransaction.setTxTime("April 24, 2018 01:24am");
        bindableTransaction.setFundingAddress("1PyWmpjXkPftUGSbz9nrHbK4EoCPhKi6pg");
        bindableTransaction.setTargetAddress("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS");
        bindableTransaction.setValue(449893L);
        bindableTransaction.setFee(167L);
    }

    @Test
    public void does_not_set_empty_memo() {
        setupSend();

        adapter.onBindViewHolder(viewHolder, 0);

        TextView memoView = view.findViewById(R.id.transaction_memo);
        assertThat(memoView, hasText(""));
        assertThat(memoView, isGone());
    }

    @Test
    public void sets_memo() {
        setupSend();

        bindableTransaction.setMemo("memo");

        adapter.onBindViewHolder(viewHolder, 0);

        TextView memoView = view.findViewById(R.id.transaction_memo);
        assertThat(memoView, hasText("memo"));
        assertThat(memoView, isVisible());
    }

    // Failed to broadcast
    @Test
    public void receivng_failing_to_broadcast_updates_confirmations_to_failed_to_broadcast() {
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(),
                equalTo(view.getResources().getString(R.string.history_failed_to_broadcast)));

    }

    @Test
    public void receivng_failing_to_broadcast_shows_failed_icon() {
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);

        adapter.onBindViewHolder(viewHolder, 0);

        ImageView icon = view.findViewById(R.id.icon);
        assertThat(icon.getTag(), equalTo(R.drawable.ic_transaction_canceled));
    }

    @Test
    public void sending_failing_to_broadcast_updates_confirmations_to_failed_to_broadcast() {
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(),
                equalTo(view.getResources().getString(R.string.history_failed_to_broadcast)));

    }

    @Test
    public void sending_failing_to_broadcast_shows_failed_icon() {
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);

        adapter.onBindViewHolder(viewHolder, 0);

        ImageView icon = view.findViewById(R.id.icon);
        assertThat(icon.getTag(), equalTo(R.drawable.ic_transaction_canceled));
    }

    // Invites
    @Test
    public void sets_value_of_currency_sent_canceled_invites() {
        setupInviteSend();
        bindableTransaction.setSendState(SendState.SEND_CANCELED);

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_prices_on_display_view() {
        setupInviteSend();

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void canceled_transactions_show_target() {
        setupInviteSend();
        bindableTransaction.setSendState(SendState.SEND_CANCELED);

        adapter.onBindViewHolder(viewHolder, 0);

        assertThat(((TextView) view.findViewById(R.id.address)).getText().toString(),
                equalTo("Joe Blow"));
    }

    @Test
    public void canceled_transactions_get_canceled_icon() {
        setupInviteSend();
        bindableTransaction.setSendState(SendState.SEND_CANCELED);

        adapter.onBindViewHolder(viewHolder, 0);

        ImageView icon = view.findViewById(R.id.icon);
        assertThat(icon.getTag(), equalTo(R.drawable.ic_transaction_canceled));
    }

    @Test
    public void canceled_invitations_state_canceled() {
        setupInviteSend();
        bindableTransaction.setInviteState(BindableTransaction.InviteState.CANCELED);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(),
                equalTo(view.getResources().getString(R.string.history_invite_canceled)));
    }

    @Test
    public void expired_invitations_state_expired() {
        setupInviteSend();
        bindableTransaction.setInviteState(BindableTransaction.InviteState.EXPIRED);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(),
                equalTo(view.getResources().getString(R.string.history_invite_expired)));
    }

    // Send To Contact
    @Test
    public void sets_send_image_for_icon() {
        setupSend();

        adapter.onBindViewHolder(viewHolder, 0);

        ImageView icon = view.findViewById(R.id.icon);
        assertThat(icon.getTag(), equalTo(R.drawable.ic_transaction_send));
    }

    @Test
    public void sets_receive_image_for_icon() {
        setupReceive();
        adapter.onBindViewHolder(viewHolder, 0);

        ImageView icon = view.findViewById(R.id.icon);
        assertThat(icon.getTag(), equalTo(R.drawable.ic_transaction_receive));
    }

    @Test
    public void when_receiving_it_populates_with_senders_address() {
        setupReceive();

        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS"));
    }

    @Test
    public void populates_with_receivers_address() {
        setupSend();

        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo("3FVXXXLtej9x7kzHgB51WGXpPXkdfEUBjS"));
    }


    @Test
    public void recieves_from_self_identifies_as_such() {
        setupTransfer();
        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo(view.getResources().getString(R.string.send_to_self)));
    }

    @Test
    public void sends_to_self_identifies_as_such() {
        setupTransfer();

        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo(view.getResources().getString(R.string.send_to_self)));
    }

    @Test
    public void sends_to_self_shows_fee_value() {
        setupTransfer();

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_value_of_currency_sent_failed_to_send() {
        setupSend();
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_value_of_currency_received_failed_to_send() {
        setupReceive();
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_value_of_currency_sent() {
        setupSend();

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_value_of_currency_received() {
        setupReceive();

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_converted_price_of_sent_failed_to_send() {
        setupSend();
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_converted_price_of_receive_failed_to_send() {
        setupReceive();
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_converted_price_of_sent() {
        setupSend();

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void sets_converted_price_of_receive() {
        setupReceive();

        adapter.onBindViewHolder(viewHolder, 0);

        DefaultCurrencyDisplayView displayView = view.findViewById(R.id.default_currency_view);
        assertThat(displayView.getTotalCrypto().toLong(), equalTo(bindableTransaction.totalCryptoForSendState().toLong()));
        assertThat(displayView.getFiatValue().toLong(), equalTo(bindableTransaction.totalFiatForSendState().toLong()));
    }

    @Test
    public void pending_confirmation_0() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.UNCONFIRMED);

        adapter.onBindViewHolder(viewHolder, 0);

        String expected = view.getResources().getString(R.string.confirmations_view_stage_4);
        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(), equalTo(expected));
    }

    @Test
    public void pending_confirmation_1() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.ONE_CONFIRM);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations, isGone());
    }

    @Test
    public void pending_confirmation_2() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.TWO_CONFIRMS);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations, isGone());
    }

    @Test
    public void confirmed() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.CONFIRMED);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations, isGone());
    }

    @Test
    public void confirmed_failed_to_receive() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.CONFIRMED);
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_RECEIVE);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(),
                equalTo(view.getResources().getString(R.string.history_failed_to_broadcast)));
    }

    @Test
    public void confirmed_failed_to_send() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.CONFIRMED);
        bindableTransaction.setSendState(SendState.FAILED_TO_BROADCAST_SEND);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(),
                equalTo(view.getResources().getString(R.string.history_failed_to_broadcast)));
    }

    @Test
    public void show_contact_name_if_available_test() {
        bindableTransaction.setIdentity("Jeff");

        adapter.onBindViewHolder(viewHolder, 0);

        assertThat(view.findViewById(R.id.address), hasText("Jeff"));
    }
}