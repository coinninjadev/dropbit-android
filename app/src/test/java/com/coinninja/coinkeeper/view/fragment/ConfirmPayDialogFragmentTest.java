package com.coinninja.coinkeeper.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.widget.TextView;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.TransactionData;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BroadcastActivity;
import com.coinninja.coinkeeper.view.activity.InviteSendActivity;
import com.coinninja.coinkeeper.view.button.ConfirmHoldButton;
import com.coinninja.matchers.ActivityMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class ConfirmPayDialogFragmentTest {

    private static final String PHONE_NUMBER_STRING = "+13305551111";
    @Mock
    private PaymentBarCallbacks paymentBarCallbacks;
    @Mock
    private CurrencyPreference currencyPreference;
    @Mock
    private DefaultCurrencies defaultCurrencies;
    private TransactionData transactionData;
    private PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    private ConfirmPayDialogFragment dialog;
    private FragmentController<ConfirmPayDialogFragment> fragmentController;
    private String paymentAddress = "--send-address--";
    private Currency eval = new USDCurrency(5000d);
    private PaymentHolder paymentHolder = new PaymentHolder(eval);
    private ShadowActivity shadowActivity;

    @After
    public void tearDown() {
        paymentBarCallbacks = null;
        currencyPreference = null;
        defaultCurrencies = null;
        phoneNumber = null;
        dialog = null;
        fragmentController = null;
        paymentAddress = null;
        eval = null;
        paymentHolder = null;
        shadowActivity = null;
    }

    @Before
    public void setUp() throws Exception {
        transactionData = new TransactionData(new UnspentTransactionOutput[0],
                1000L,
                100L,
                0L,
                mock(DerivationPath.class),
                paymentAddress
        );
        MockitoAnnotations.initMocks(this);
        when(defaultCurrencies.getPrimaryCurrency()).thenReturn(new USDCurrency());
        when(defaultCurrencies.getSecondaryCurrency()).thenReturn(new BTCCurrency());
        when(defaultCurrencies.getFiat()).thenReturn(new USDCurrency());
        when(defaultCurrencies.getCrypto()).thenReturn(new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        paymentHolder.setDefaultCurrencies(defaultCurrencies);
        paymentHolder.setTransactionData(transactionData);
        fragmentController = Robolectric.buildFragment(ConfirmPayDialogFragment.class);
        dialog = fragmentController.get();

        Parcel parcel = Parcel.obtain();
        double minFee = 300;
        parcel.writeDouble(minFee);
        double avgFee = 300;
        parcel.writeDouble(avgFee);
        double maxFee = 300;
        parcel.writeDouble(maxFee);
        parcel.setDataPosition(0);
        paymentHolder.updateValue(new USDCurrency("50"));
        paymentHolder.setTransactionData(transactionData);
        paymentHolder.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
        fragmentController.create();
        dialog.onAttach(dialog.getActivity());
        dialog.paymentBarCallbacks = paymentBarCallbacks;


        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(defaultCurrencies.getPrimaryCurrency()).thenReturn(new BTCCurrency());
        when(defaultCurrencies.getSecondaryCurrency()).thenReturn(new USDCurrency());
    }

    @Test
    public void tracks_view_of_confirmation_screen() {
        verify(dialog.analytics).trackEvent(Analytics.EVENT_CONFIRM_SCREEN_LOADED);
        verify(dialog.analytics).flush();
    }

    @Test
    public void successful_authorization_begins_broadcast() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        show(contact, paymentHolder);

        dialog.onHoldCompleteSuccessfully();

        ShadowActivity.IntentForResult nextStartedActivityForResult = shadowActivity.getNextStartedActivityForResult();
        Intent intent = nextStartedActivityForResult.intent;
        assertThat(nextStartedActivityForResult.requestCode, equalTo(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE));
        assertThat(intent.getComponent().getClassName(), equalTo(AuthorizedActionActivity.class.getName()));
    }

    @Test
    public void sends_regular_transactions_with_memo() {
        Activity activity = show(
                paymentHolder);
        paymentHolder.setMemo("--memo--");
        paymentHolder.setIsSharingMemo(false);
        paymentHolder.setPublicKey(null);
        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        BroadcastTransactionDTO broadcastDTO = new BroadcastTransactionDTO(paymentHolder.getTransactionData(),
                null, false, "--memo--", null);
        Intent intent = new Intent(activity, BroadcastActivity.class);
        intent.putExtra(Intents.EXTRA_BROADCAST_DTO, broadcastDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void sends_to_contact_with_address_and_memo() {
        String publicKey = "--public-key--";
        paymentHolder.setPublicKey(publicKey);
        paymentHolder.setMemo("--memo--");
        paymentHolder.setTransactionData(transactionData);
        Contact contact = new Contact(phoneNumber, "Joe", true);
        Activity activity = show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        BroadcastTransactionDTO broadcastDTO = new BroadcastTransactionDTO(paymentHolder.getTransactionData(), contact,
                true, "--memo--", publicKey);
        Intent intent = new Intent(activity, BroadcastActivity.class);
        intent.putExtra(Intents.EXTRA_BROADCAST_DTO, broadcastDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void sends_invite_with_memo() {
        String memo = "--memo--";
        paymentHolder.setMemo(memo);
        paymentHolder.setPaymentAddress("");
        paymentHolder.setIsSharingMemo(true);
        Contact contact = new Contact(phoneNumber, "Joe", true);
        Activity activity = show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                memo, true);
        Intent intent = new Intent(activity, InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteActivityDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    // Payment Authorization

    @Test
    public void sends_invite_without_memo() {
        paymentHolder.setPaymentAddress("");
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        paymentHolder.setMemo(null);
        paymentHolder.setIsSharingMemo(false);
        Activity activity = show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                null, false);
        Intent intent = new Intent(activity, InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteActivityDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void successful_authorizes_contact_sends_without_addresses() {
        paymentHolder.setPaymentAddress("");
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        String memo = "for dinner and drinks";
        paymentHolder.setMemo(memo);
        paymentHolder.setIsSharingMemo(false);
        Activity activity = show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                paymentHolder.getTransactionData().getAmount(),
                paymentHolder.getTransactionData().getFeeAmount(),
                memo, false);
        Intent intent = new Intent(activity, InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteActivityDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
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

    private void show() {
        fragmentController.start().resume().visible();
        shadowActivity = shadowOf(dialog.getActivity());
    }

    private Activity show(Contact contact, PaymentHolder paymentHolder) {
        dialog.setContact(contact);
        dialog.setPaymentHolder(paymentHolder);
        show();
        return dialog.getActivity();
    }

    private Activity show(PaymentHolder paymentHolder) {
        dialog.setPaymentHolder(paymentHolder);
        show();
        return dialog.getActivity();
    }
}