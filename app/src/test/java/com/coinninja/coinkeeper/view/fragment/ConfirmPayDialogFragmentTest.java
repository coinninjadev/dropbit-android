package com.coinninja.coinkeeper.view.fragment;

import android.content.Intent;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowActivity;

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class ConfirmPayDialogFragmentTest {

    private static final String PHONE_NUMBER_STRING = "+13305551111";
    @Mock
    private PaymentBarCallbacks paymentBarCallbacks;
    @Mock
    private CurrencyPreference currencyPreference;
    @Mock
    private LazyList transactions;
    @Mock
    private DefaultCurrencies defaultCurrencies;
    @Mock
    private WalletHelper walletHelper;
    private TransactionData transactionData;
    private PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    private ConfirmPayDialogFragment dialog;
    private String paymentAddress = "--send-address--";
    private Currency eval = new USDCurrency(5000d);
    private PaymentHolder paymentHolder = new PaymentHolder(eval);
    private ShadowActivity shadowActivity;
    private ActivityScenario<TransactionHistoryActivity> scenario;

    @After
    public void tearDown() {
        walletHelper = null;
        paymentBarCallbacks = null;
        currencyPreference = null;
        defaultCurrencies = null;
        phoneNumber = null;
        transactions = null;
        dialog = null;
        paymentAddress = null;
        eval = null;
        paymentHolder = null;
        shadowActivity = null;
        scenario.close();
        scenario = null;
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication testCoinKeeperApplication = ApplicationProvider.getApplicationContext();
        testCoinKeeperApplication.walletHelper = walletHelper;
        transactionData = new TransactionData(new UnspentTransactionOutput[0],
                1000L,
                100L,
                0L,
                mock(DerivationPath.class),
                paymentAddress
        );
        when(defaultCurrencies.getPrimaryCurrency()).thenReturn(new USDCurrency());
        when(defaultCurrencies.getSecondaryCurrency()).thenReturn(new BTCCurrency());
        when(defaultCurrencies.getFiat()).thenReturn(new USDCurrency());
        when(defaultCurrencies.getCrypto()).thenReturn(new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        paymentHolder.setDefaultCurrencies(defaultCurrencies);
        paymentHolder.setTransactionData(transactionData);
        paymentHolder.updateValue(new USDCurrency("50"));
        paymentHolder.setTransactionData(transactionData);
        paymentHolder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(defaultCurrencies.getPrimaryCurrency()).thenReturn(new BTCCurrency());
        when(defaultCurrencies.getSecondaryCurrency()).thenReturn(new USDCurrency());
        when(transactions.size()).thenReturn(0);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);

        scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    @Test
    public void tracks_view_of_confirmation_screen() {
        show(paymentHolder);

        verify(dialog.analytics).trackEvent(Analytics.EVENT_CONFIRM_SCREEN_LOADED);
        verify(dialog.analytics).flush();
    }

    @Test
    public void successful_authorization_begins_broadcast() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        show(contact, paymentHolder);

        dialog.onHoldCompleteSuccessfully();

        scenario.onActivity(activity -> {
            ShadowActivity.IntentForResult nextStartedActivityForResult = shadowOf(activity).getNextStartedActivityForResult();
            Intent intent = nextStartedActivityForResult.intent;
            assertThat(intent.getComponent().getClassName(), equalTo(AuthorizedActionActivity.class.getName()));
            //assertThat(nextStartedActivityForResult.requestCode, equalTo(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE));
            // bug with Robolectric not forwarding activity for result
        });
    }

    @Test
    public void sends_regular_transactions_with_memo() {
        show(paymentHolder);
        paymentHolder.setMemo("--memo--");
        paymentHolder.setIsSharingMemo(false);
        paymentHolder.setPublicKey(null);
        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        BroadcastTransactionDTO broadcastDTO = new BroadcastTransactionDTO(paymentHolder.getTransactionData(),
                null, false, "--memo--", null);

        Intents.intending(hasComponent(BroadcastActivity.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_BROADCAST_DTO));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastDTO));
    }

    @Test
    public void sends_to_contact_with_address_and_memo() {
        String publicKey = "--public-key--";
        paymentHolder.setPublicKey(publicKey);
        paymentHolder.setMemo("--memo--");
        paymentHolder.setTransactionData(transactionData);
        Contact contact = new Contact(phoneNumber, "Joe", true);
        show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        BroadcastTransactionDTO broadcastDTO = new BroadcastTransactionDTO(paymentHolder.getTransactionData(), contact,
                true, "--memo--", publicKey);

        Intents.intending(hasComponent(BroadcastActivity.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_BROADCAST_DTO));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_BROADCAST_DTO, broadcastDTO));
    }

    @Test
    public void sends_invite_with_memo() {
        String memo = "--memo--";
        paymentHolder.setMemo(memo);
        paymentHolder.setPaymentAddress("");
        paymentHolder.setIsSharingMemo(true);
        Contact contact = new Contact(phoneNumber, "Joe", true);
        show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                memo, true);

        Intents.intending(hasComponent(InviteSendActivity.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_INVITE_DTO));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_INVITE_DTO, inviteActivityDTO));
    }

    // Payment Authorization

    @Test
    public void sends_invite_without_memo() {
        paymentHolder.setPaymentAddress("");
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        paymentHolder.setMemo(null);
        paymentHolder.setIsSharingMemo(false);
        show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                null, false);

        Intents.intending(hasComponent(InviteSendActivity.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_INVITE_DTO));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_INVITE_DTO, inviteActivityDTO));
    }

    @Test
    public void successful_authorizes_contact_sends_without_addresses() {
        paymentHolder.setPaymentAddress("");
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        String memo = "for dinner and drinks";
        paymentHolder.setMemo(memo);
        paymentHolder.setIsSharingMemo(false);
        show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                memo, false);

        Intents.intending(hasComponent(InviteSendActivity.class.getName()));
        Intents.intending(hasExtraWithKey(DropbitIntents.EXTRA_INVITE_DTO));
        Intents.intending(hasExtra(DropbitIntents.EXTRA_INVITE_DTO, inviteActivityDTO));
    }

    @Test
    public void notifies_invoker_that_user_canceled_payment_request_by_close_button() {
        show(paymentHolder);

        dialog.getView().findViewById(R.id.confirm_pay_header_close_btn).performClick();

        verify(paymentBarCallbacks).cancelPayment(dialog);
    }

    @Test
    public void init_without_calculating_fee() {
        show(paymentHolder);
        ConfirmHoldButton confirmHoldButton = dialog.getView().findViewById(R.id.confirm_pay_hold_progress_btn);
        assertNotNull(confirmHoldButton);

        verify(dialog.analytics).trackEvent(Analytics.EVENT_BROADCAST_TO_ADDRESS);
    }

    @Test
    public void sets_btc_address_when_sending_to_address() {
        show(paymentHolder);

        TextView address = dialog.getView().findViewById(R.id.confirm_pay_btc_address);

        assertThat(address.getText().toString(), equalTo(paymentAddress));
    }

    @Test
    public void showing_contact_shows_both_name_and_number() {
        paymentHolder.setPaymentAddress("");
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        show(contact, paymentHolder);

        assertThat(((TextView) dialog.getView().findViewById(R.id.confirm_pay_name)).getText().toString(),
                equalTo("Joe Smoe"));
        assertThat(((TextView) dialog.getView().findViewById(R.id.confirm_pay_btc_address)).getText().toString(),
                equalTo(""));
    }

    @Test
    public void show_invite_phone_number() {
        Contact contact = new Contact(phoneNumber, "", false);
        show(contact, paymentHolder);

        TextView btcContactNameDisplay = dialog.getView().findViewById(R.id.confirm_pay_name);
        TextView btcSendAddressDisplay = dialog.getView().findViewById(R.id.confirm_pay_btc_address);

        assertThat(btcContactNameDisplay.getText().toString(), equalTo("+1 330-555-1111"));
        assertThat(btcSendAddressDisplay.getText().toString(), equalTo(""));
    }

    @Test
    public void show_invite_user() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        show(contact, paymentHolder);

        TextView btcContactNameDisplay = dialog.getView().findViewById(R.id.confirm_pay_name);
        TextView btcSendAddressDisplay = dialog.getView().findViewById(R.id.confirm_pay_btc_address);

        assertThat(btcContactNameDisplay.getText().toString(), equalTo("Joe Smoe"));
        assertThat(btcSendAddressDisplay.getText().toString(), equalTo(""));
    }

    @Test
    public void sends_broadcast_to_address_event() {
        show(paymentHolder);

        verify(dialog.analytics).trackEvent(Analytics.EVENT_BROADCAST_TO_ADDRESS);
    }

    @Test
    public void send_analytics_EVENT_TRANSACTION_CONFIRMED_when_HoldCompleteSuccessfully() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        show(contact, paymentHolder);

        dialog.onHoldCompleteSuccessfully();

        verify(dialog.analytics).trackEvent(Analytics.EVENT_DROPBIT_SEND);
    }

    @Test
    public void shows_memo_when_available() {
        String memo = "dinner and drinks";
        paymentHolder.setMemo(memo);
        show(paymentHolder);

        TextView memoView = withId(dialog.getView(), R.id.shared_memo_text_view);

        assertThat(memoView, hasText(memo));
    }

    private void show(Contact contact, PaymentHolder paymentHolder) {
        dialog = ConfirmPayDialogFragment.newInstance(paymentHolder, paymentBarCallbacks);
        dialog.setPaymentHolder(paymentHolder);
        dialog.setContact(contact);
        scenario.onActivity(activity -> {
            dialog.show(activity.getSupportFragmentManager(), dialog.getTag());
        });
    }

    private void show(PaymentHolder paymentHolder) {
        dialog = ConfirmPayDialogFragment.newInstance(paymentHolder, paymentBarCallbacks);
        dialog.setPaymentHolder(paymentHolder);
        scenario.onActivity(activity -> {
            dialog.show(activity.getSupportFragmentManager(), dialog.getTag());
        });
    }
}