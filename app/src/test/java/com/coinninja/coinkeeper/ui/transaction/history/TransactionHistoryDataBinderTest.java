package com.coinninja.coinkeeper.ui.transaction.history;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.base.TestableActivity;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.ConfirmationState;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;

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
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
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
        when(transactions.get(0)).thenReturn(transaction);
        TestableActivity activity = Robolectric.setupActivity(TestableActivity.class);
        ViewGroup parent = activity.findViewById(R.id.test_root);
        bindableTransaction = new BindableTransaction(walletHelper);
        when(transactionAdapterUtil.translateTransaction(any(TransactionsInvitesSummary.class))).thenReturn(bindableTransaction);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(1000.00d));
        adapter = new TransactionHistoryDataAdapter(transactionAdapterUtil, defaultCurrencies);
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
        bindableTransaction.setContactName(contactName);
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

        TextView memoView = withId(view, R.id.transaction_memo);
        assertThat(memoView, hasText(""));
        assertThat(memoView, isGone());
    }

    @Test
    public void sets_memo() {
        setupSend();

        bindableTransaction.setMemo("memo");

        adapter.onBindViewHolder(viewHolder, 0);

        TextView memoView = withId(view, R.id.transaction_memo);
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
    public void shows_send_to_self_when_selfs_contact_Name() {
        setupTransfer();
        String contactName = "Joe Blow";
        bindableTransaction.setContactName(contactName);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo(view.getResources().getString(R.string.send_to_self)));
    }

    @Test
    public void shows_send_to_self_when_selfs_contact_phone() {
        setupTransfer();
        String contactPhoneNumber = "+13305551111";
        bindableTransaction.setContactName(null);
        bindableTransaction.setContactPhoneNumber(contactPhoneNumber);


        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo(view.getResources().getString(R.string.send_to_self)));
    }

    @Test
    public void shows_contact_number_over_address() {
        setupSend();
        String contactPhoneNumber = "+13305551111";
        bindableTransaction.setContactName(null);
        bindableTransaction.setContactPhoneNumber(contactPhoneNumber);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo("(330) 555-1111"));
    }

    @Test
    public void shows_contact_name_over_address() {
        setupSend();
        String contactName = "Joe Blow";
        bindableTransaction.setContactName(contactName);

        adapter.onBindViewHolder(viewHolder, 0);

        TextView address = view.findViewById(R.id.address);
        assertThat(address.getText().toString(), equalTo(contactName));
    }

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
    public void zero_time_means() {
        setupReceive();
        bindableTransaction.setTxTime("");

        adapter.onBindViewHolder(viewHolder, 0);

        TextView time = view.findViewById(R.id.blocktime);
        assertThat(time.getText().toString(), equalTo(""));
    }

    @Test
    public void formats_time_to_date() {
        setupReceive();

        adapter.onBindViewHolder(viewHolder, 0);

        TextView time = view.findViewById(R.id.blocktime);
        assertThat(time.getText().toString(), equalTo("April 24, 2018 01:24am"));
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

        String expected = view.getResources().getString(R.string.confirmations_view_stage_5);
        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(), equalTo(expected));
        assertThat(confirmations, isGone());
    }

    @Test
    public void pending_confirmation_2() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.TWO_CONFIRMS);

        adapter.onBindViewHolder(viewHolder, 0);

        String expected = view.getResources().getString(R.string.confirmations_view_stage_5);
        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(), equalTo(expected));
        assertThat(confirmations, isGone());
    }

    @Test
    public void confirmed() {
        setupReceive();
        bindableTransaction.setConfirmationState(ConfirmationState.CONFIRMED);

        adapter.onBindViewHolder(viewHolder, 0);

        String expected = view.getResources().getString(R.string.confirmations_view_stage_5);
        TextView confirmations = view.findViewById(R.id.confirmations);
        assertThat(confirmations.getText().toString(), equalTo(expected));
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
    public void show_phone_number_if_contact_name_is_null_test() {
        bindableTransaction.setContactName(null);
        bindableTransaction.setContactPhoneNumber("(222) 333-4444");

        adapter.onBindViewHolder(viewHolder, 0);

        assertThat(withId(view, R.id.address), hasText("(222) 333-4444"));
    }

    @Test
    @Config(qualifiers = "es-rAU")
    public void show_phone_number_if_contact_name_is_empty_test__international_format() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+12223334444");

        adapter.onBindViewHolder(viewHolder, 0);

        assertThat(withId(view, R.id.address), hasText("+1 222-333-4444"));
    }

    @Test
    public void show_phone_number_if_contact_name_is_empty_test() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+12223334444");

        adapter.onBindViewHolder(viewHolder, 0);

        assertThat(withId(view, R.id.address), hasText("(222) 333-4444"));
    }

    @Test
    public void show_contact_name_if_available_test() {
        bindableTransaction.setContactName("Jeff");
        bindableTransaction.setContactPhoneNumber("(222) 333-4444");

        adapter.onBindViewHolder(viewHolder, 0);

        assertThat(withId(view, R.id.address), hasText("Jeff"));
    }
}