package com.coinninja.coinkeeper.view.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.EditText;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.FundedCallback;
import com.coinninja.coinkeeper.model.FundingUTXOs;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.ui.payment.PaymentInputView;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.PickContactActivity;
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView;
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.wallet.data.TestData.EXTERNAL_ADDRESSES;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertTrue;
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
@Config(application = TestCoinKeeperApplication.class, qualifiers = "en-rUS")
public class PayDialogFragmentTest {

    private static final String PHONE_NUMBER_STRING = "+13305551111";
    @Mock
    CurrencyPreference currencyPreference;
    DefaultCurrencies defaultCurrencies;
    private PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    private PayDialogFragment dialog = mock(PayDialogFragment.class);
    private ShadowActivity shadowActivity;
    private FragmentController<PayDialogFragment> fragmentController;
    private PaymentHolder paymentHolder;
    private AddressLookupResult addressLookupResult;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private PaymentUtil paymentUtil;
    @Mock
    private ClipboardUtil clipboardUtil;
    @Mock
    private BitcoinUtil bitcoinUtil;
    @Mock
    private UserPreferences preferenceInteractor;
    @Mock
    private PaymentBarCallbacks paymentBarCallbacks;
    @Mock
    private CNAddressLookupDelegate cnAddressLookupDelegate;
    private List<CountryCodeLocale> countryCodeLocales = new ArrayList<>();

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        paymentHolder = new PaymentHolder(new USDCurrency(5000.00d), new TransactionFee(5, 10, 15));
        paymentHolder.setDefaultCurrencies(defaultCurrencies);
        paymentHolder.updateValue(new USDCurrency(5000.00d));
        when(paymentUtil.getPaymentHolder()).thenReturn(paymentHolder);
        when(preferenceInteractor.getShouldShowInviteHelp()).thenReturn(true);
        addressLookupResult = new AddressLookupResult();
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "US"), 1));

        fragmentController = Robolectric.buildFragment(PayDialogFragment.class);
        dialog = fragmentController.get();
        fragmentController.create();
        dialog.onAttach(dialog.getActivity());
        dialog.paymentBarCallbacks = paymentBarCallbacks;
        dialog.paymentUtil = paymentUtil;
        dialog.paymentHolder = paymentHolder;
        dialog.preferenceInteractor = preferenceInteractor;
        dialog.clipboardUtil = clipboardUtil;
        dialog.bitcoinUtil = bitcoinUtil;
        dialog.walletHelper = walletHelper;
        dialog.memoToggleView = mock(SharedMemoToggleView.class);
        dialog.cnAddressLookupDelegate = cnAddressLookupDelegate;
        dialog.countryCodeLocales = countryCodeLocales;
    }

    @After
    public void tearDown() {
        countryCodeLocales.clear();
        countryCodeLocales = null;
        cnAddressLookupDelegate = null;
        addressLookupResult = null;
        dialog = null;
        shadowActivity = null;
        fragmentController = null;
        paymentHolder = null;
        walletHelper = null;
        clipboardUtil = null;
        paymentUtil = null;
        bitcoinUtil = null;
        preferenceInteractor = null;
        paymentBarCallbacks = null;
    }

    @Test
    public void tracks_payment_screen_view() {
        startFragment();

        verify(dialog.analytics).trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED);
        verify(dialog.analytics).flush();
    }

    // INITIALIZATION WITH VALUES

    @Test
    public void shows_price_on_launch() {
        startFragment();

        dialog.paymentInputView = mock(PaymentInputView.class);

        fragmentController.start().resume();

        verify(dialog.paymentInputView).requestFocus();
    }

    @Test
    public void shows_payment_address_when_initialized_from_scan() {
        String address = EXTERNAL_ADDRESSES[0];
        when(paymentUtil.getAddress()).thenReturn(address);
        startFragment();

        PaymentReceiverView receiverView = withId(dialog.getView(), R.id.payment_receiver);

        assertThat(receiverView.getPaymentAddress(), equalTo(address));
    }

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
        verify(paymentBarCallbacks).confirmPaymentFor(address);
    }

    // CHECK FUNDED

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
        verify(paymentBarCallbacks).confirmPaymentFor("-pay-address-", contact);
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
        verify(paymentBarCallbacks).confirmInvite(contact);
    }


    // PRIMARY / SECONDARY CURRENCIES

    @Test
    public void given_btc_as_primary_pasting_address_with_out_amount_keeps_btc() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0";
        when(clipboardUtil.getRaw()).thenReturn(value);
        paymentHolder.updateValue(new BTCCurrency("1.0"));
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(0L);
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100000000L));
    }

    @Test
    public void given_usd_as_primary_pasting_address_with_amount_toggles_primary_to_btc() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=100000000";
        when(clipboardUtil.getRaw()).thenReturn(value);
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(100000000L);
        startFragment();

        dialog.paymentInputView = mock(PaymentInputView.class);
        clickOn(dialog.getView(), R.id.paste_address_btn);

        assertTrue(paymentHolder.getPrimaryCurrency().isCrypto());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100000000L));
        verify(dialog.paymentInputView).setPaymentHolder(paymentHolder);
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

    // VALIDATION

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


    // SHARED MEMOS

    @Test
    public void canceling_transaction_clears_memo() {
        startFragment();
        paymentHolder.setMemo(":grinning: hi");

        withId(dialog.getView(), R.id.pay_header_close_btn).performClick();

        assertNull(paymentHolder.getMemo());
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

    // PAYMENT RECEIVER CONFIGURATION
    @Test
    public void sets_country_codes_on_payment_receiver_view() {
        startFragment();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);

        assertThat(paymentReceiverView.getCountryCodeLocales(), equalTo(countryCodeLocales));
    }

    @Test
    public void pasting_address_with_valid_address_sets_address_on_payment_receiver_view() throws UriException {
        String rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo";
        mockClipboardWithData(rawString);
        startFragment();

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(rawString));
        verify(paymentUtil).setAddress(rawString);
    }

    // PASTING ADDRESS

    @Test
    public void pasting_invalid_address_shows_error_and_does_not_set_address() throws UriException {
        String rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKooere";
        when(clipboardUtil.getRaw()).thenReturn(rawString);
        when(bitcoinUtil.parse(rawString)).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58));
        startFragment();

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(""));
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Address Failed Base 58 check"));
    }

    @Test
    public void pasting_with_empty_clipboard_does_nothing() throws UriException {
        when(clipboardUtil.getRaw()).thenReturn("");
        when(bitcoinUtil.parse("")).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS));
        startFragment();

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Nothing to paste"));
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(""));
    }

    @Test
    public void clears_pub_key_from_holder_when_pasting_address() throws UriException {
        startFragment();
        paymentHolder.setPublicKey("--pub-key--");
        when(clipboardUtil.getRaw()).thenReturn("--bitcoin uri--");
        BitcoinUri uri = mock(BitcoinUri.class);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(0L);
        when(bitcoinUtil.parse(anyString())).thenReturn(uri);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.ADDRESS);

        dialog.onPasteClicked();

        assertThat(paymentHolder.getPublicKey(), equalTo(""));
    }

    @Test
    public void given_usd_as_primary_pasting_address_with_out_amount_keeps_usd() throws UriException {
        paymentHolder.updateValue(new USDCurrency(1D));
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0";
        when(clipboardUtil.getRaw()).thenReturn(value);
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(0L);
        startFragment();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        assertTrue(paymentHolder.getPrimaryCurrency().isFiat());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100L));
    }

    @Test
    public void pasting_address_over_contact_clears_contact_and_shows_address() throws UriException {
        String address = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo";
        mockClipboardWithData(address);
        Contact contact = new Contact(phoneNumber, "Joe Dirt", true);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        startFragment();
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, Activity.RESULT_OK, intent);

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        assertThat(paymentReceiverView, isVisible());
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo("3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo"));
        assertThat(paymentReceiverView.getPhoneNumber(), equalTo("+1"));
    }

    @Test
    public void scanning_address__valid_address__no_amount() throws UriException {
        String cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4";
        Intent data = new Intent();
        data.putExtra(Intents.EXTRA_SCANNED_DATA, cryptoString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(cryptoString)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4");
        startFragment();
        EditText primaryCurrency = withId(dialog.getView(), R.id.primary_currency);
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        primaryCurrency.setText("$1.00");

        dialog.onActivityResult(Intents.REQUEST_QR_FRAGMENT_SCAN, Intents.RESULT_SCAN_OK, data);

        assertThat(paymentReceiverView.getPaymentAddress(), equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"));
        assertTrue(paymentHolder.getPrimaryCurrency().isFiat());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100L));
    }

    @Test
    public void updates_btc_on_scan_with_amount() throws UriException {
        String cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4?amount=.01542869";
        Intent data = new Intent();
        data.putExtra(Intents.EXTRA_SCANNED_DATA, cryptoString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(cryptoString)).thenReturn(bitcoinUri);
        when(bitcoinUri.getSatoshiAmount()).thenReturn(1542869L);
        when(bitcoinUri.getAddress()).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4");
        startFragment();
        dialog.paymentInputView = mock(PaymentInputView.class);

        dialog.onActivityResult(Intents.REQUEST_QR_FRAGMENT_SCAN, Intents.RESULT_SCAN_OK, data);

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"));
        assertTrue(paymentHolder.getPrimaryCurrency().isCrypto());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(1542869L));
        verify(dialog.paymentInputView).setPaymentHolder(paymentHolder);
    }

    // SCAN PAYMENT ADDRESS

    @Test
    public void clears_pub_key_when_scanning() throws UriException {
        paymentHolder.setPublicKey("--pub-key--");
        startFragment();
        Intent data = new Intent();
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUri.getAddress()).thenReturn("xfdkjvhbw43hfbwkehvbw43jhkf");
        data.putExtra(Intents.EXTRA_SCANNED_DATA, "bitcoin:xfdkjvhbw43hfbwkehvbw43jhkf");
        when(bitcoinUtil.parse(anyString())).thenReturn(bitcoinUri);
        when(paymentUtil.getPaymentMethod()).thenReturn(PaymentUtil.PaymentMethod.ADDRESS);

        dialog.onActivityResult(Intents.REQUEST_QR_FRAGMENT_SCAN, Intents.RESULT_SCAN_OK, data);

        assertThat(paymentHolder.getPublicKey(), equalTo(""));
    }

    @Test
    public void fetches_address_for_phone_number_when_number_is_valid() {
        startFragment();

        dialog.onPhoneNumberValid(phoneNumber.getPhoneNumber());

        verify(cnAddressLookupDelegate).fetchAddressFor(eq(phoneNumber), any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void nulls_out_contact_on_invalid_phone_number() {
        startFragment();

        String text = "0000000000";
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        withId(paymentReceiverView, R.id.show_phone_input).performClick();
        PhoneNumberInputView phoneNumberInputView = withId(paymentReceiverView, R.id.phone_number_input);
        phoneNumberInputView.setText(text);

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
        verify(paymentUtil).setContact(null);
    }

    @Test
    public void sets_phone_number_on_payment_util_when_valid_input() {
        ArgumentCaptor<Contact> argumentCaptor = ArgumentCaptor.forClass(Contact.class);
        startFragment();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        withId(paymentReceiverView, R.id.show_phone_input).performClick();
        PhoneNumberInputView phoneNumberInputView = withId(paymentReceiverView, R.id.phone_number_input);
        phoneNumberInputView.setText("3305551111");

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
    public void picking_contact_hides_payment_receiver_view() {
        Contact contact = new Contact(phoneNumber, "Joe Dirt", true);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        startFragment();
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        paymentReceiverView.setPaymentAddress(EXTERNAL_ADDRESSES[0]);

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, Activity.RESULT_OK, intent);

        assertThat(paymentReceiverView, isGone());
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(""));
        assertThat(paymentReceiverView.getPhoneNumber(), equalTo("+1"));
    }

    @Test
    public void does_not_show_invite_help_when_preference_selected() {
        when(preferenceInteractor.getShouldShowInviteHelp()).thenReturn(false);
        startFragment();

        Contact contact = new Contact(phoneNumber, "Joe Blow", false);

        dialog.startContactInviteFlow(contact);

        verify(paymentBarCallbacks).confirmInvite(contact);
    }

    @Test
    public void fetches_address_for_verified_contact_when_user_picks_one() {
        Contact contact = new Contact(phoneNumber, "Joe Blow", true);
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_CONTACT, contact);
        startFragment();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, Activity.RESULT_OK, intent);

        assertThat(withId(dialog.getView(), R.id.contact_name), hasText("Joe Blow"));
        assertThat(withId(dialog.getView(), R.id.contact_number), hasText(phoneNumber.toInternationalDisplayText()));
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
    public void invites_unverified_contact_after_confirming_help_screen() {
        startFragment();
        Contact contact = new Contact(phoneNumber, "Joe Blow", false);

        dialog.onInviteHelpAccepted(contact);

        verify(paymentBarCallbacks).confirmInvite(contact);
    }

    @Test
    public void directs_user_to_verify_phone_when_selecting_contacts() {
        startFragment();

        dialog.getView().findViewById(R.id.contacts_btn).performClick();

        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(VerifyPhoneNumberActivity.class.getName()));
    }

    @Test
    public void can_cancel_payment_request() {
        startFragment();
        paymentHolder.setPaymentAddress("--address--");
        paymentHolder.setPublicKey("--pub-key--");

        dialog.getView().findViewById(R.id.pay_header_close_btn).performClick();

        verify(paymentBarCallbacks).cancelPayment(dialog);
        assertThat(paymentHolder.getPublicKey(), equalTo(""));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(""));
    }

    @Test
    public void tells_view_types_to_teardown_when_detached() {
        startFragment();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);

        dialog.onDetach();

        verify(dialog.memoToggleView).tearDown();
    }

    @Test
    public void clears_sender_information_when_dismissed() {
        startFragment();

        fragmentController.pause().stop().destroy();

        verify(paymentUtil).reset();
    }

    @Test
    public void tears_down_delegate_when_dismissed() {
        startFragment();

        dialog.onDismiss(mock(DialogInterface.class));

        verify(cnAddressLookupDelegate).teardown();
    }

    private void startFragment() {
        fragmentController.start().resume().visible();
        shadowActivity = shadowOf(dialog.getActivity());
    }

    private void mockClipboardWithData(String rawString) throws UriException {
        when(clipboardUtil.getRaw()).thenReturn(rawString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUri.getAddress()).thenReturn(rawString);
        when(bitcoinUtil.parse(rawString)).thenReturn(bitcoinUri);
    }

}