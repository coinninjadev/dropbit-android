package com.coinninja.coinkeeper.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.widget.TextView;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.presenter.activity.CalculatorActivityPresenter;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.service.runner.FundingRunnable;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
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
import com.google.i18n.phonenumbers.Phonenumber;

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
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class ConfirmPayDialogFragmentTest {

    @Mock
    private HDWallet hdWallet;

    @Mock
    private CalculatorActivityPresenter.View viewCallback;

    @Mock
    private AccountManager accountManager;

    @Mock
    FundingRunnable fundingRunnable;

    @Mock
    private UnspentTransactionHolder transactionHolder;

    private PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();

    public static final String PHONE_NUMBER_STRING = "+13305551111";
    PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);

    private ConfirmPayDialogFragment dialog;
    private TransactionFee transactionFee;
    private FragmentController<ConfirmPayDialogFragment> fragmentController;
    private String sendAddress = "1CmACXcAQBch7ewpVRrA9Qbb6zc4UiWjJP";
    private Currency eval = new USDCurrency(5000d);
    private PaymentHolder paymentHolder = new PaymentHolder(eval, transactionFee);
    private ShadowActivity shadowActivity;

    @Before
    public void setUp() throws Exception {
        transactionHolder = new UnspentTransactionHolder(1100L,
                new UnspentTransactionOutput[0],
                1000L,
                100L,
                0L,
                mock(DerivationPath.class),
                "--pay to address"
        );
        MockitoAnnotations.initMocks(this);
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
        transactionFee = new TransactionFee(parcel);
        paymentHolder.loadPaymentFrom(new USDCurrency("50"));
        when(hdWallet.getFeeForTransaction(any(), eq(3))).thenReturn(new BTCCurrency("0.5"));
        fragmentController.create();
        dialog.onAttach(dialog.getActivity());
        dialog.accountManager = accountManager;
        dialog.hdWallet = hdWallet;
        dialog.calculatorView = viewCallback;
        dialog.fundingRunnable = fundingRunnable;
        dialog.phoneNumberUtil = phoneNumberUtil;

        // HACk To force quick execution of async task -- there is apparently a real async task running here
        FundingRunnable.FundedHolder fundholder = mock(FundingRunnable.FundedHolder.class);
        when(fundingRunnable.evaluateFundingUTXOs(any(FundingUTXOs.class))).thenReturn(fundholder);
        when(fundholder.getUnspentTransactionHolder()).thenReturn(transactionHolder);
    }

    private void show() {
        fragmentController.start().resume().visible();
        shadowActivity = shadowOf(dialog.getActivity());
    }

    private Activity show(Contact contact, String address, PaymentHolder paymentHolder) {
        dialog.setContact(contact);
        dialog.setSendAddress(address);
        dialog.setPaymentHolder(paymentHolder);
        show();
        return dialog.getActivity();
    }

    private Activity show(String address, PaymentHolder paymentHolder) {
        dialog.setSendAddress(address);
        dialog.setPaymentHolder(paymentHolder);
        show();
        return dialog.getActivity();
    }

    private Activity show(Contact phoneNumber, PaymentHolder paymentHolder) {
        dialog.setContact(phoneNumber);
        dialog.setPaymentHolder(paymentHolder);
        show();
        return dialog.getActivity();
    }

    @Test
    public void tracks_view_of_confirmation_screen() {
        verify(dialog.analytics).trackEvent(Analytics.EVENT_CONFIRM_SCREEN_LOADED);
        verify(dialog.analytics).flush();
    }

    // Payment Authorization

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
        Activity activity = show("--send address--", paymentHolder);
        paymentHolder.setMemo("--memo--");
        paymentHolder.setIsSharingMemo(false);
        paymentHolder.setPublicKey(null);

        dialog.onFundingSuccessful(transactionHolder, 100L);
        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        BroadcastTransactionDTO broadcastDTO = new BroadcastTransactionDTO(transactionHolder,
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
        Contact contact = new Contact(phoneNumber, "Joe", true);
        Activity activity = show(contact, "--send address--", paymentHolder);

        dialog.onFundingSuccessful(transactionHolder, 100L);
        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        BroadcastTransactionDTO broadcastDTO = new BroadcastTransactionDTO(transactionHolder, contact,
                true, "--memo--", publicKey);
        Intent intent = new Intent(activity, BroadcastActivity.class);
        intent.putExtra(Intents.EXTRA_BROADCAST_DTO, broadcastDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void sends_invite_with_memo() {
        String memo = "--memo--";
        paymentHolder.setMemo(memo);
        paymentHolder.setIsSharingMemo(true)/**/;
        Contact contact = new Contact(phoneNumber, "Joe", true);
        Activity activity = show(contact, paymentHolder);

        dialog.onFundingSuccessful(transactionHolder, 100L);
        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                transactionHolder.satoshisRequestingToSpend,
                transactionHolder.satoshisFeeAmount,
                memo, true);
        Intent intent = new Intent(activity, InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteActivityDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void sends_invite_without_memo() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", false);
        paymentHolder.setMemo(null);
        paymentHolder.setIsSharingMemo(false);
        Activity activity = show(contact, paymentHolder);

        dialog.onFundingSuccessful(transactionHolder, 100L);
        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                transactionHolder.satoshisRequestingToSpend,
                transactionHolder.satoshisFeeAmount,
                null, false);
        Intent intent = new Intent(activity, InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteActivityDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void successful_authorizes_contact_sends_without_addresses() {
        dialog.unspentTransactionHolder = transactionHolder;
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        String memo = "for dinner and drinks";
        paymentHolder.setMemo(memo);
        paymentHolder.setIsSharingMemo(false);
        Activity activity = show(contact, paymentHolder);

        dialog.onActivityResult(ConfirmPayDialogFragment.AUTHORIZE_PAYMENT_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        PendingInviteDTO inviteActivityDTO = new PendingInviteDTO(contact,
                paymentHolder.getEvaluationCurrency().toLong(),
                transactionHolder.satoshisRequestingToSpend,
                transactionHolder.satoshisFeeAmount,
                memo, false);
        Intent intent = new Intent(activity, InviteSendActivity.class);
        intent.putExtra(Intents.EXTRA_INVITE_DTO, inviteActivityDTO);
        assertThat(activity, ActivityMatchers.activityWithIntentStarted(intent));
    }

    @Test
    public void notifies_invoker_that_user_canceled_payment_request_by_close_button() {
        show(sendAddress, paymentHolder);

        dialog.getView().findViewById(R.id.confirm_pay_header_close_btn).performClick();

        verify(viewCallback).cancelPayment(dialog);
    }

    @Test
    public void init_without_calculating_fee() {
        show(sendAddress, paymentHolder);
        ConfirmHoldButton confirmHoldButton = dialog.getView().findViewById(R.id.confirm_pay_hold_progress_btn);
        assertNotNull(confirmHoldButton);

        verify(dialog.analytics).trackEvent(Analytics.EVENT_BROADCAST_TO_ADDRESS);
    }

    @Test
    public void confirm_payment_enabled_when_funded() {
        show(sendAddress, paymentHolder);

        ConfirmHoldButton confirmHoldButton = dialog.getView().findViewById(R.id.confirm_pay_hold_progress_btn);

        dialog.onFundingSuccessful(mock(UnspentTransactionHolder.class), 32);

        assertTrue(confirmHoldButton.isEnabled());
    }

    @Test
    public void sets_btc_address_when_sending_to_address() {
        show(sendAddress, paymentHolder);

        TextView address = dialog.getView().findViewById(R.id.confirm_pay_btc_address);

        assertThat(address.getText().toString(), equalTo(sendAddress));
    }

    @Test
    public void showing_contact_shows_both_name_and_number() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        show(contact, paymentHolder);

        assertThat(((TextView) dialog.getView().findViewById(R.id.confirm_pay_name)).getText().toString(),
                equalTo("Joe Smoe"));
        assertThat(((TextView) dialog.getView().findViewById(R.id.confirm_pay_btc_address)).getText().toString(),
                equalTo(""));
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
        show(sendAddress, paymentHolder);

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

        show(sendAddress, paymentHolder);
        TextView memoView = withId(dialog.getView(), R.id.shared_memo_text_view);

        assertThat(memoView, hasText(memo));
    }
}