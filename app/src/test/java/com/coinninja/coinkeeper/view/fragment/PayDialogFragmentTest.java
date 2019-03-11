package com.coinninja.coinkeeper.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.interactor.PreferenceInteractor;
import com.coinninja.coinkeeper.model.FundedCallback;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.CalculatorActivityPresenter;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.PickContactActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView;
import com.google.i18n.phonenumbers.NumberParseException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.FragmentController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.wallet.data.TestData.EXTERNAL_ADDRESSES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class,
        qualifiers = "en-rUS")
public class PayDialogFragmentTest {

    public static final String PHONE_NUMBER_STRING = "+13305551111";
    PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    private PayDialogFragment dialog = mock(PayDialogFragment.class);
    private ShadowActivity shadowActivity;
    private FragmentController<PayDialogFragment> fragmentController;
    private PaymentHolder paymentHolder;
    AddressLookupResult addressLookupResult;

    @Mock
    private WalletHelper walletHelper;
    @Mock
    private PaymentUtil paymentUtil;
    @Mock
    private ClipboardUtil clipboardUtil;
    @Mock
    private BitcoinUriBuilder bitcoinUriBuilder;
    @Mock
    private PreferenceInteractor preferenceInteractor;
    @Mock
    private CalculatorActivityPresenter.View viewCallback;
    @Mock
    private CNAddressLookupDelegate cnAddressLookupDelegate;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        paymentHolder = new PaymentHolder(new USDCurrency(5000d), new TransactionFee(5, 10, 15));
        paymentHolder.loadPaymentFrom(new USDCurrency(5000d));
        when(paymentUtil.getPaymentHolder()).thenReturn(paymentHolder);
        when(preferenceInteractor.getShouldShowInviteHelp()).thenReturn(true);
        addressLookupResult = new AddressLookupResult();

        fragmentController = Robolectric.buildFragment(PayDialogFragment.class);
        dialog = fragmentController.get();
        fragmentController.create();
        dialog.onAttach(dialog.getActivity());
        dialog.calculatorView = viewCallback;
        dialog.paymentUtil = paymentUtil;
        dialog.paymentHolder = paymentHolder;
        dialog.preferenceInteractor = preferenceInteractor;
        dialog.clipboardUtil = clipboardUtil;
        dialog.bitcoinUriBuilder = bitcoinUriBuilder;
        dialog.walletHelper = walletHelper;
        dialog.memoToggleView = mock(SharedMemoToggleView.class);
        dialog.cnAddressLookupDelegate = cnAddressLookupDelegate;
    }

    @After
    public void tearDown() {
        cnAddressLookupDelegate = null;
        addressLookupResult = null;
        dialog = null;
        shadowActivity = null;
        fragmentController = null;
        paymentHolder = null;
        walletHelper = null;
        clipboardUtil = null;
        paymentUtil = null;
        bitcoinUriBuilder = null;
        preferenceInteractor = null;
        viewCallback = null;
    }

    private void startFragment() {
        fragmentController.resume().start().visible();
        shadowActivity = shadowOf(dialog.getActivity());
    }

    @Test
    public void sending_adds_memo_and_sharing_to_payment_holder() {
        dialog.memoToggleView = mock(SharedMemoToggleView.class);
        when(dialog.memoToggleView.isSharing()).thenReturn(false);
        when(dialog.memoToggleView.getMemo()).thenReturn("--memo--");

        dialog.sendPaymentTo();
        assertThat(paymentHolder.getMemo(), equalTo("--memo--"));
        assertThat(paymentHolder.getIsSharingMemo(), equalTo(false));

        paymentHolder.setMemo("");
        paymentHolder.setIsSharingMemo(true);

        dialog.sendPaymentTo("--address--", new Contact());
        assertThat(paymentHolder.getMemo(), equalTo("--memo--"));
        assertThat(paymentHolder.getIsSharingMemo(), equalTo(false));

        paymentHolder.setMemo("");
        paymentHolder.setIsSharingMemo(true);

        dialog.inviteContact(new Contact());
        assertThat(paymentHolder.getMemo(), equalTo("--memo--"));
        assertThat(paymentHolder.getIsSharingMemo(), equalTo(false));
    }

    @Test
    public void clears_pub_key_from_holder_when_pasting_address() throws UriException {
        startFragment();
        paymentHolder.setPublicKey("--pub-key--");
        when(clipboardUtil.getRaw()).thenReturn("--bitcoin uri--");
        when(bitcoinUriBuilder.parse(anyString())).thenReturn(mock(BitcoinUri.class));
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.ADDRESS);

        dialog.onPasteClicked();

        assertThat(paymentHolder.getPublicKey(), equalTo(""));
    }

    @Test
    public void clears_pub_key_when_scanning() throws UriException {
        paymentHolder.setPublicKey("--pub-key--");
        startFragment();
        Intent data = new Intent();
        data.putExtra(Intents.EXTRA_SCANNED_DATA, "--scanned data--");
        when(bitcoinUriBuilder.parse(anyString())).thenReturn(mock(BitcoinUri.class));
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.ADDRESS);

        dialog.onActivityResult(Intents.REQUEST_QR_FRAGMENT_SCAN, Intents.RESULT_SCAN_OK, data);

        assertThat(paymentHolder.getPublicKey(), equalTo(""));
    }

    @Test
    public void tears_down_delegate_when_dismissed() {
        startFragment();

        dialog.onDismiss(mock(DialogInterface.class));

        verify(cnAddressLookupDelegate).teardown();
    }

    @Test
    public void fetches_address_for_verified_contact_when_user_picks_one() {
        Contact contact = new Contact(phoneNumber, "Joe Dirt", true);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        startFragment();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, Activity.RESULT_OK, intent);

        verify(cnAddressLookupDelegate).fetchAddressFor(eq(contact),
                any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void does_not_fetch_address_for_non_verified_contact_when_user_picks_one() {
        Contact contact = new Contact(phoneNumber, "Joe Dirt", false);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        startFragment();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, Activity.RESULT_OK, intent);

        verify(cnAddressLookupDelegate, times(0)).
                fetchAddressFor(any(Contact.class), any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void allows_user_to_share_memo_for_invite_phone_number_entry__simulate_non_verified_user_lookup() {
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.INVITE);
        Contact contact = new Contact(phoneNumber, "Joe Dirt", false);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        startFragment();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, Activity.RESULT_OK, intent);

        verify(dialog.memoToggleView).showSharedMemoViews();
    }

    @Test
    public void allows_user_to_share_memo_for_invite_phone_number_entry__simulate_phone_number_lookup() {
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.INVITE);

        dialog.onFetchContactComplete(new AddressLookupResult("--phone-hash--", "", ""));

        verify(dialog.memoToggleView).showSharedMemoViews();
    }


    @Test
    public void fetches_address_for_phone_number_when_number_is_valid() throws NumberParseException {
        startFragment();

        dialog.onPhoneNumberValid(phoneNumber.getPhoneNumber());

        verify(cnAddressLookupDelegate).fetchAddressFor(eq(phoneNumber), any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void shows_shared_memos_when_valid_number_returns_with_pub_key() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(paymentUtil.getContact()).thenReturn(contact);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.VERIFIED_CONTACT);
        AddressLookupResult addressLookupResult = new AddressLookupResult(
                "-phone-hash-",
                "-payment-address-",
                "-pub-key-");

        dialog.onFetchContactComplete(addressLookupResult);

        verify(dialog.memoToggleView).showSharedMemoViews();
    }

    @Test
    public void hides_shared_memos_when_valid_number_does_not_return_with_pub_key() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(paymentUtil.getContact()).thenReturn(contact);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.VERIFIED_CONTACT);

        AddressLookupResult addressLookupResult = new AddressLookupResult(
                "-phone-hash-",
                "",
                "");


        dialog.onFetchContactComplete(addressLookupResult);

        verify(dialog.memoToggleView).hideSharedMemoViews();
    }

    @Test
    public void tracks_payment_screen_view() {
        startFragment();

        verify(dialog.analytics).trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED);
        verify(dialog.analytics).flush();
    }

    //TODO acceptance test
    @Test
    public void funded_payments_get_confirmed() {
        String address = "-- expected--";
        when(paymentUtil.getAddress()).thenReturn(address);
        when(paymentUtil.isValid()).thenReturn(true);
        when(paymentUtil.isFunded()).thenReturn(true);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.ADDRESS);
        ArgumentCaptor<FundedCallback> callbackCaptor = ArgumentCaptor.forClass(FundedCallback.class);
        startFragment();

        dialog.getView().findViewById(R.id.pay_footer_send_btn).performClick();

        verify(paymentUtil).checkFunding(callbackCaptor.capture());
        callbackCaptor.getValue().onComplete(mock(FundingUTXOs.class));
        verify(viewCallback).confirmPaymentFor(address);
    }

    @Test
    public void contact_sends_get_confirmed() {
        ArgumentCaptor<FundedCallback> fundedCallbackCaptor = ArgumentCaptor.forClass(FundedCallback.class);
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        when(paymentUtil.getContact()).thenReturn(contact);
        when(paymentUtil.isValid()).thenReturn(true);
        when(paymentUtil.isFunded()).thenReturn(true);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.VERIFIED_CONTACT);
        paymentHolder.setPublicKey("-pub-key-");
        paymentHolder.setPaymentAddress("-pay-address-");
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        startFragment();

        dialog.getView().findViewById(R.id.pay_footer_send_btn).performClick();

        verify(paymentUtil).checkFunding(fundedCallbackCaptor.capture());
        fundedCallbackCaptor.getValue().onComplete(mock(FundingUTXOs.class));
        verify(viewCallback).confirmPaymentFor("-pay-address-", contact);
    }

    @Test
    public void verified_contacts_with_out_addresses_get_invited_without_help_confirmation() {
        when(preferenceInteractor.getShouldShowInviteHelp()).thenReturn(true);
        ArgumentCaptor<FundedCallback> fundedCallbackCaptor = ArgumentCaptor.forClass(FundedCallback.class);
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        when(paymentUtil.getContact()).thenReturn(contact);
        when(paymentUtil.isValid()).thenReturn(true);
        when(paymentUtil.isVerifiedContact()).thenReturn(true);
        when(paymentUtil.isFunded()).thenReturn(true);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.INVITE);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        startFragment();

        dialog.getView().findViewById(R.id.pay_footer_send_btn).performClick();

        verify(paymentUtil).checkFunding(fundedCallbackCaptor.capture());
        fundedCallbackCaptor.getValue().onComplete(mock(FundingUTXOs.class));
        verify(viewCallback).confirmInvite(contact);
    }

    @Test
    public void on_primary_text_clicked_clears_text_when_currency_is_zero() {
        paymentHolder.loadPaymentFrom(new USDCurrency(0d));
        startFragment();

        TextView view = dialog.getView().findViewById(R.id.primary_currency);
        view.requestFocus();

        assertThat(view.getText().toString(), equalTo("$0"));
    }

    @Test
    public void on_primary_text_clicked_does_not_clear_text_when_there_is_a_value() {
        paymentHolder.loadPaymentFrom(new USDCurrency(10d));
        startFragment();

        TextView view = dialog.getView().findViewById(R.id.primary_currency);
        view.requestFocus();

        assertThat(view.getText().toString(), equalTo("$10.00"));
    }

    @Test
    public void notifies_invoker_that_user_canceled_payment_request_by_close_button() {
        startFragment();
        paymentHolder.setPaymentAddress("--address--");
        paymentHolder.setPublicKey("--pub-key--");

        dialog.getView().findViewById(R.id.pay_header_close_btn).performClick();

        verify(viewCallback).cancelPayment(dialog);
        assertThat(paymentHolder.getPublicKey(), equalTo(""));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(""));
    }

    @Test
    public void clears_sender_information_when_dismissed() {
        startFragment();

        fragmentController.pause().stop().destroy();

        verify(paymentUtil).reset();
    }

    @Test
    public void provides_currency_watcher_on_primary_input() {
        startFragment();

        EditText editText = dialog.getView().findViewById(R.id.primary_currency);
        editText.setText("1");

        assertThat(editText.getText().toString(), equalTo("$1"));
    }

    @Test
    public void given_usd_as_primary_pasting_address_with_out_amount_keeps_usd() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0";
        when(clipboardUtil.getRaw()).thenReturn(value);
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUriBuilder.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(0L);
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        TextView primaryCurrency = dialog.getView().findViewById(R.id.primary_currency);
        TextView secondaryCurrency = dialog.getView().findViewById(R.id.secondary_currency);
        assertThat(primaryCurrency.getText().toString(), equalTo(new USDCurrency(5000d).toFormattedCurrency()));
        assertThat(secondaryCurrency.getText().toString(), equalTo(new BTCCurrency("1.0").toFormattedCurrency()));
    }


    @Test
    public void given_btc_as_primary_pasting_address_with_out_amount_keeps_btc() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0";
        when(clipboardUtil.getRaw()).thenReturn(value);
        paymentHolder.loadPaymentFrom(new BTCCurrency("1.0"));
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUriBuilder.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(0L);
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        TextView primaryCurrency = dialog.getView().findViewById(R.id.primary_currency);
        TextView secondaryCurrency = dialog.getView().findViewById(R.id.secondary_currency);
        assertThat(primaryCurrency.getText().toString(), equalTo(new BTCCurrency("1.0").toFormattedCurrency()));
        assertThat(secondaryCurrency.getText().toString(), equalTo(new USDCurrency(5000d).toFormattedCurrency()));
    }

    @Test
    public void given_usd_as_primary_pasting_address_with_amount_toggles_primary_to_btc() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=100000000";
        when(clipboardUtil.getRaw()).thenReturn(value);
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUriBuilder.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(100000000L);
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        TextView primaryCurrency = dialog.getView().findViewById(R.id.primary_currency);
        TextView secondaryCurrency = dialog.getView().findViewById(R.id.secondary_currency);
        assertThat(primaryCurrency.getText().toString(), equalTo(new BTCCurrency("1.0").toFormattedCurrency()));
        assertThat(secondaryCurrency.getText().toString(), equalTo(new USDCurrency(5000d).toFormattedCurrency()));
    }

    @Test
    public void shows_price_when_BTC_primary() {
        USDCurrency usd = new USDCurrency(5000.00D);
        BTCCurrency btc = new BTCCurrency(1.0D);
        paymentHolder.loadPaymentFrom(btc);

        startFragment();

        TextView primaryCurrency = dialog.getView().findViewById(R.id.primary_currency);
        TextView secondaryCurrency = dialog.getView().findViewById(R.id.secondary_currency);
        assertThat(primaryCurrency.getText().toString(), equalTo(btc.toFormattedCurrency()));
        assertThat(secondaryCurrency.getText().toString(), equalTo(usd.toFormattedCurrency()));
    }

    @Test
    public void shows_price_when_USD_primary() {
        USDCurrency usd = new USDCurrency(5000.00D);
        BTCCurrency btc = new BTCCurrency(1.0D);

        startFragment();

        TextView primaryCurrency = dialog.getView().findViewById(R.id.primary_currency);
        TextView secondaryCurrency = dialog.getView().findViewById(R.id.secondary_currency);
        assertThat(primaryCurrency.getText().toString(), equalTo(usd.toFormattedCurrency()));
        assertThat(secondaryCurrency.getText().toString(), equalTo(btc.toFormattedCurrency()));
    }

    @Test
    public void shows_error_when_address_not_valid() {
        startFragment();
        when(paymentUtil.isValid()).thenReturn(false);
        String message = dialog.getActivity().getResources().getString(R.string.pay_error_add_valid_bitcoin_address);
        when(paymentUtil.getErrorMessage()).thenReturn(message);

        dialog.getView().findViewById(R.id.pay_footer_send_btn).performClick();

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getMessage(),
                equalTo(message));
        verify(paymentUtil).clearErrors();
    }

    @Test
    public void updates_address_only__scan_with_address() throws UriException {
        String cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4";
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUriBuilder.parse(cryptoString)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4");

        Intent data = new Intent();
        data.putExtra(Intents.EXTRA_SCANNED_DATA, cryptoString);
        startFragment();

        ((EditText) dialog.getView().findViewById(R.id.primary_currency)).setText("$1.00");

        dialog.onActivityResult(Intents.REQUEST_QR_FRAGMENT_SCAN, Intents.RESULT_SCAN_OK, data);

        assertThat(((TextView) dialog.getView().findViewById(R.id.primary_currency)).getText().toString(),
                equalTo("$1.00"));
        assertThat(((TextView) dialog.getView().findViewById(R.id.send_to_input)).getText().toString(),
                equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"));
    }

    @Test
    public void updates_btc_on_scan_with_amount() throws UriException {
        String cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4?amount=.01542869";
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUriBuilder.parse(cryptoString)).thenReturn(bitcoinUri);
        when(bitcoinUri.getSatoshiAmount()).thenReturn(1542869L);
        when(bitcoinUri.getAddress()).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4");

        Intent data = new Intent();
        data.putExtra(Intents.EXTRA_SCANNED_DATA, cryptoString);
        startFragment();

        dialog.onActivityResult(Intents.REQUEST_QR_FRAGMENT_SCAN, Intents.RESULT_SCAN_OK, data);

        assertThat(((TextView) dialog.getView().findViewById(R.id.primary_currency)).getText().toString(),
                equalTo("\u20BF 0.01542869"));
        assertThat(((TextView) dialog.getView().findViewById(R.id.send_to_input)).getText().toString(),
                equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"));
    }

    @Test
    public void does_not_show_invite_help_when_preference_selected() {
        when(preferenceInteractor.getShouldShowInviteHelp()).thenReturn(false);
        startFragment();

        Contact contact = new Contact(phoneNumber, "Joe Blow", false);

        dialog.startContactInviteFlow(contact);

        verify(viewCallback).confirmInvite(contact);
    }

    @Test
    public void invites_unverified_contact_after_confirming_help_screen() {
        startFragment();
        Contact contact = new Contact(phoneNumber, "Joe Blow", false);

        dialog.onInviteHelpAccepted(contact);

        verify(viewCallback).confirmInvite(contact);
    }

    @Test
    public void nulls_out_contact_on_invalid_phone_number() {
        startFragment();

        String text = "000-000-0000";
        ((TextView) dialog.getView().findViewById(R.id.send_to_input)).setText(text);

        verify(paymentUtil).setContact(null);
    }

    @Test
    public void sets_phone_number_on_payment_util_when_valid_input() {
        ArgumentCaptor<Contact> argumentCaptor = ArgumentCaptor.forClass(Contact.class);
        startFragment();

        ((TextView) dialog.getView().findViewById(R.id.send_to_input)).setText("3305551111");

        verify(paymentUtil).setContact(argumentCaptor.capture());

        Contact contact = argumentCaptor.getValue();
        assertThat(contact.getPhoneNumber(), equalTo(phoneNumber));
    }

    @Test
    public void picks_contact_to_send() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);

        startFragment();
        dialog.getView().findViewById(R.id.contacts_btn).performClick();

        ShadowActivity.IntentForResult intentForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(intentForResult.requestCode, equalTo(PayDialogFragment.PICK_CONTACT_REQUEST));
        assertThat(intentForResult.intent.getComponent().getClassName(), equalTo(PickContactActivity.class.getName()));
    }

    @Test
    public void directs_user_to_verify_phone_when_selecting_contacts() {
        startFragment();

        dialog.getView().findViewById(R.id.contacts_btn).performClick();

        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(VerifyPhoneNumberActivity.class.getName()));
    }

    @Test
    public void shows_address_when_instructed() {
        startFragment();
        String address = EXTERNAL_ADDRESSES[0];

        dialog.onPaymentAddressChange(address);

        assertThat(((TextView) dialog.getView().
                        findViewById(R.id.send_to_input)).
                        getText().toString(),
                equalTo(address));
        verify(paymentUtil).setAddress(address);
    }

    @Test
    public void shows_price_on_launch() {
        startFragment();

        EditText usdView = dialog.getView().findViewById(R.id.primary_currency);
        TextView btcView = dialog.getView().findViewById(R.id.secondary_currency);

        assertThat(usdView.getText().toString(), equalTo(paymentHolder.getPrimaryCurrency().toFormattedCurrency()));
        assertThat(btcView.getText().toString(), equalTo(paymentHolder.getSecondaryCurrency().toFormattedCurrency()));
    }

    @Test
    public void grabClipboardData_good_address() throws UriException {
        String rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo";
        when(clipboardUtil.getRaw()).thenReturn(rawString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUri.getAddress()).thenReturn(rawString);
        when(bitcoinUriBuilder.parse(rawString)).thenReturn(bitcoinUri);
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        verify(paymentUtil).setAddress(rawString);
        TextView btcSendAddressDisplay = dialog.getActivity().findViewById(R.id.send_to_input);
        assertThat(btcSendAddressDisplay.getText().toString(), equalTo(rawString));
        assertThat(btcSendAddressDisplay.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void past_bad_base58_address_test() throws UriException {
        String rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKooere";
        when(clipboardUtil.getRaw()).thenReturn(rawString);
        when(bitcoinUriBuilder.parse(rawString)).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58));
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Address Failed Base 58 check"));
        TextView btcSendAddressDisplay = dialog.getActivity().findViewById(R.id.send_to_input);
        assertThat(btcSendAddressDisplay.getText().toString(), equalTo(""));
    }

    @Test
    public void grabClipboardData_no_btc_address_in_clipboard() throws UriException {
        when(clipboardUtil.getRaw()).thenReturn("");
        when(bitcoinUriBuilder.parse("")).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS));
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Nothing to paste"));
        TextView sendAddress = dialog.getView().findViewById(R.id.send_to_input);
        assertThat(sendAddress.getText().toString(), equalTo(""));
    }

    @Test
    public void canceling_transaction_clears_memo() {
        startFragment();
        paymentHolder.setMemo(":grinning: hi");

        withId(dialog.getView(), R.id.pay_header_close_btn).performClick();

        assertNull(paymentHolder.getMemo());
    }


    @Test
    public void tells_view_types_to_teardown_when_detached() {
        startFragment();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);

        dialog.onDetach();

        verify(dialog.memoToggleView).tearDown();
    }
}