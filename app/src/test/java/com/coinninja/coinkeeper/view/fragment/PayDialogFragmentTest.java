package com.coinninja.coinkeeper.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
import com.coinninja.coinkeeper.cn.wallet.service.CNAddressLookupDelegate;
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager;
import com.coinninja.coinkeeper.interactor.UserPreferences;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.presenter.activity.PaymentBarCallbacks;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.ui.payment.PaymentInputView;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.ClipboardUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.PickContactActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;
import com.coinninja.coinkeeper.view.subviews.SharedMemoToggleView;
import com.coinninja.coinkeeper.view.widget.PaymentReceiverView;
import com.coinninja.coinkeeper.view.widget.phonenumber.CountryCodeLocale;
import com.coinninja.coinkeeper.view.widget.phonenumber.PhoneNumberInputView;

import org.greenrobot.greendao.query.LazyList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.wallet.data.TestData.EXTERNAL_ADDRESSES;
import static com.coinninja.matchers.TextViewMatcher.hasText;
import static com.coinninja.matchers.ViewMatcher.isGone;
import static com.coinninja.matchers.ViewMatcher.isVisible;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(AndroidJUnit4.class)
public class PayDialogFragmentTest {

    private static final String PHONE_NUMBER_STRING = "+13305551111";
    private PayDialogFragment dialog;

    private DefaultCurrencies defaultCurrencies;
    @Mock
    private CurrencyPreference currencyPreference;
    private PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    private ShadowActivity shadowActivity;
    private PaymentHolder paymentHolder;
    @Mock
    private Analytics analytics;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private ClipboardUtil clipboardUtil;
    @Mock
    private BitcoinUtil bitcoinUtil;
    @Mock
    private UserPreferences userPreferences;
    @Mock
    private PaymentBarCallbacks paymentBarCallbacks;
    @Mock
    private CNAddressLookupDelegate cnAddressLookupDelegate;
    @Mock
    private TransactionFundingManager transactionFundingManger;
    private List<CountryCodeLocale> countryCodeLocales = new ArrayList<>();
    @Mock
    private LazyList transactions;
    private PaymentUtil paymentUtil;

    private TransactionData validTransactionData = new TransactionData(new UnspentTransactionOutput[1],
            10000L, 1000L, 0, mock(DerivationPath.class), "");
    private TransactionData invalidTransactionData = new TransactionData(new UnspentTransactionOutput[0],
            0, 0, 0, mock(DerivationPath.class), "");

    private ActivityScenario<TransactionHistoryActivity> scenario;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        setupDI(context);
        dialog = PayDialogFragment.newInstance(paymentUtil, paymentBarCallbacks, false);
    }

    @After
    public void tearDown() {
        dialog.dismiss();
        context = null;
        countryCodeLocales = null;
        cnAddressLookupDelegate = null;
        dialog = null;
        shadowActivity = null;
        paymentHolder = null;
        walletHelper = null;
        clipboardUtil = null;
        paymentUtil = null;
        bitcoinUtil = null;
        userPreferences = null;
        paymentBarCallbacks = null;
        scenario.close();
    }

    @Test
    public void tracks_payment_screen_view() {
        start();
        verify(dialog.analytics).trackEvent(Analytics.EVENT_PAY_SCREEN_LOADED);
        verify(dialog.analytics).flush();
    }

    // INITIALIZATION WITH VALUES

    @Test
    public void shows_payment_address_when_initialized_from_scan() {
        String address = EXTERNAL_ADDRESSES[0];
        paymentUtil.setAddress(address);
        start();

        PaymentReceiverView receiverView = withId(dialog.getView(), R.id.payment_receiver);

        assertThat(receiverView.getPaymentAddress(), equalTo(address));
    }

    @Test
    public void funded_payments_get_confirmed() {
        String address = "-- expected--";
        when(bitcoinUtil.isValidBTCAddress(address)).thenReturn(true);
        paymentHolder.updateValue(new USDCurrency(1.00d));
        paymentUtil.setAddress(address);
        when(transactionFundingManger.buildFundedTransactionData(any(), anyLong()))
                .thenReturn(validTransactionData);
        start();

        clickOn(withId(dialog.getView(), R.id.pay_footer_send_btn));

        verify(paymentBarCallbacks).confirmPaymentFor(paymentHolder);
    }

    @Test
    public void contact_sends_get_confirmed() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        when(transactionFundingManger.buildFundedTransactionData(any(), anyLong())).thenReturn(validTransactionData);
        paymentUtil.setContact(contact);
        paymentHolder.setPublicKey("-pub-key-");
        paymentHolder.setPaymentAddress("-pay-address-");
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        start();

        clickOn(withId(dialog.getView(), R.id.pay_footer_send_btn));

        verify(paymentBarCallbacks).confirmPaymentFor(paymentHolder, contact);
    }

    // CHECK FUNDED

    @Test
    public void verified_contacts_with_out_addresses_get_invited_without_help_confirmation() {
        when(userPreferences.getShouldShowInviteHelp()).thenReturn(true);
        when(transactionFundingManger.buildFundedTransactionData(any(), anyLong())).thenReturn(validTransactionData);
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        paymentHolder.setPaymentAddress("");
        paymentHolder.updateValue(new USDCurrency(1.00d));
        paymentUtil.setContact(contact);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        start();

        clickOn(withId(dialog.getView(), R.id.pay_footer_send_btn));

        verify(paymentBarCallbacks).confirmInvite(contact);
    }

    @Test
    public void given_btc_as_primary_pasting_address_with_out_amount_keeps_btc() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=0";
        when(clipboardUtil.getRaw()).thenReturn(value);
        paymentHolder.updateValue(new BTCCurrency("1.0"));
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(0L);
        start();

        dialog.getView().findViewById(R.id.paste_address_btn).performClick();

        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100000000L));
    }

    // PRIMARY / SECONDARY CURRENCIES

    @Test
    public void given_usd_as_primary_pasting_address_with_amount_toggles_primary_to_btc() throws UriException {
        String value = "bitcoin:34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R?amount=100000000";
        when(clipboardUtil.getRaw()).thenReturn(value);
        BitcoinUri uri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(value)).thenReturn(uri);
        when(uri.getAddress()).thenReturn("34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R");
        when(uri.getSatoshiAmount()).thenReturn(100000000L);
        start();

        clickOn(dialog.getView(), R.id.paste_address_btn);

        scenario.onActivity(activity -> {
            assertThat(dialog.clipboardUtil, equalTo(clipboardUtil));
        });

        assertTrue(paymentHolder.getPrimaryCurrency().isCrypto());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100000000L));
    }

    @Test
    public void shows_error_when_address_not_valid() {
        paymentUtil.setAddress(null);
        String message = context.getResources().getString(R.string.pay_error_add_valid_bitcoin_address);
        start();

        dialog.getView().findViewById(R.id.pay_footer_send_btn).performClick();
        GenericAlertDialog alert = (GenericAlertDialog) dialog.getActivity()
                .getSupportFragmentManager().findFragmentByTag("INVALID_PAYMENT");

        assertThat(alert.getMessage(), equalTo(message));
        assertThat(paymentUtil.getErrorMessage(), equalTo(""));
    }

    @Test
    public void sending_adds_memo_and_sharing_to_payment_holder() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        start();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);
        when(dialog.memoToggleView.isSharing()).thenReturn(false);
        when(dialog.memoToggleView.getMemo()).thenReturn("--memo--");

        paymentUtil.setAddress("--address--");
        paymentHolder.setMemo("");
        dialog.sendPayment();
        assertThat(paymentHolder.getMemo(), equalTo("--memo--"));
        assertThat(paymentHolder.getIsSharingMemo(), equalTo(false));

        paymentHolder.setIsSharingMemo(true);
        paymentHolder.setMemo("");
        paymentHolder.setPaymentAddress("--address--");
        paymentUtil.setContact(new Contact(phoneNumber, "Joe Dirt", true));

        dialog.sendPayment();
        assertThat(paymentHolder.getMemo(), equalTo("--memo--"));
        assertThat(paymentHolder.getIsSharingMemo(), equalTo(false));

        paymentHolder.setMemo("");
        paymentHolder.setIsSharingMemo(true);
        paymentUtil.setContact(new Contact());
        dialog.sendPayment();
        assertThat(paymentHolder.getMemo(), equalTo("--memo--"));
        assertThat(paymentHolder.getIsSharingMemo(), equalTo(false));
    }

    // VALIDATION

    @Test
    public void canceling_transaction_clears_memo() {
        start();
        paymentHolder.setMemo(":grinning: hi");

        withId(dialog.getView(), R.id.pay_header_close_btn).performClick();

        assertNull(paymentHolder.getMemo());
    }


    // SHARED MEMOS

    @Test
    public void allows_user_to_share_memo_for_invite_phone_number_entry__simulate_non_verified_user_lookup() {
        Contact contact = new Contact(phoneNumber, "Joe Dirt", false);
        paymentUtil.setContact(contact);
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_CONTACT, contact);
        start();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent);

        verify(dialog.memoToggleView).showSharedMemoViews();
    }

    @Test
    public void shows_shared_memos_when_valid_number_returns_with_pub_key() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        paymentUtil.setContact(contact);
        AddressLookupResult addressLookupResult = new AddressLookupResult(
                "-phone-hash-",
                "-payment-address-",
                "-pub-key-");
        start();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);

        dialog.onFetchContactComplete(addressLookupResult);

        verify(dialog.memoToggleView).showSharedMemoViews();
    }

    @Test
    public void hides_shared_memos_when_valid_number_does_not_return_with_pub_key() {
        Contact contact = new Contact(phoneNumber, "Joe Smoe", true);
        paymentUtil.setContact(contact);

        AddressLookupResult addressLookupResult = new AddressLookupResult(
                "-phone-hash-",
                "",
                "");
        start();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);


        dialog.onFetchContactComplete(addressLookupResult);

        verify(dialog.memoToggleView).hideSharedMemoViews();
    }

    // PAYMENT RECEIVER CONFIGURATION
    @Test
    public void sets_country_codes_on_payment_receiver_view() {
        start();
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);

        assertThat(paymentReceiverView.getCountryCodeLocales(), equalTo(countryCodeLocales));
    }

    @Test
    public void pasting_address_with_valid_address_sets_address_on_payment_receiver_view() throws UriException {
        String rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo";
        mockClipboardWithData(rawString);
        start();

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(rawString));
        assertThat(paymentUtil.getAddress(), equalTo(rawString));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(rawString));
    }

    @Test
    public void pasting_invalid_address_shows_error_and_does_not_set_address() throws UriException {
        String rawString = "3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKooere";
        when(clipboardUtil.getRaw()).thenReturn(rawString);
        when(bitcoinUtil.parse(rawString)).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58));
        start();

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(""));
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Address Failed Base 58 check"));
    }

    // PASTING ADDRESS

    @Test
    public void pasting_with_empty_clipboard_does_nothing() throws UriException {
        when(clipboardUtil.getRaw()).thenReturn("");
        when(bitcoinUtil.parse("")).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS));
        paymentHolder.setPaymentAddress("--address--");
        start();

        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Nothing to paste"));
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(""));
        assertNull(paymentUtil.getAddress());
        assertThat(paymentHolder.getPaymentAddress(), equalTo(""));
    }


    @Test
    public void clears_pub_key_from_holder_when_pasting_address() throws UriException {
        String address = "34TpJP7AFps9JvoZHKFnFv3dRnYrC8jk8R";
        paymentHolder.setPublicKey("--pub-key--");
        when(clipboardUtil.getRaw()).thenReturn("--bitcoin uri--");
        BitcoinUri uri = mock(BitcoinUri.class);
        when(uri.getAddress()).thenReturn(address);
        when(uri.getSatoshiAmount()).thenReturn(0L);
        when(bitcoinUtil.parse(anyString())).thenReturn(uri);
        start();

        dialog.onPasteClicked();

        assertThat(paymentUtil.getAddress(), equalTo(address));
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
        start();

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
        intent.putExtra(DropbitIntents.EXTRA_CONTACT, contact);
        start();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent);
        withId(dialog.getView(), R.id.paste_address_btn).performClick();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView, isVisible());
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo("3EqhexhZ2cuBCPMq9kPpqj9m3R6aFzCKoo"));
        assertThat(paymentReceiverView.getPhoneNumber(), equalTo("+1"));
    }

    @Test
    public void scanning_address__valid_address__no_amount() throws UriException {
        String cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4";
        Intent data = new Intent();
        data.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, cryptoString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(cryptoString)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4");
        start();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        EditText primaryCurrency = withId(dialog.getView(), R.id.primary_currency);
        primaryCurrency.setText("$1.00");
        dialog.onActivityResult(DropbitIntents.REQUEST_QR_FRAGMENT_SCAN, DropbitIntents.RESULT_SCAN_OK, data);

        assertThat(paymentReceiverView.getPaymentAddress(), equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"));
        assertTrue(paymentHolder.getPrimaryCurrency().isFiat());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(100L));
    }

    @Test
    public void updates_btc_on_scan_with_amount() throws UriException {
        String cryptoString = "bitcoin:38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4?amount=.01542869";
        Intent data = new Intent();
        data.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, cryptoString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(cryptoString)).thenReturn(bitcoinUri);
        when(bitcoinUri.getSatoshiAmount()).thenReturn(1542869L);
        when(bitcoinUri.getAddress()).thenReturn("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4");
        dialog.paymentInputView = mock(PaymentInputView.class);
        start();

        dialog.onActivityResult(DropbitIntents.REQUEST_QR_FRAGMENT_SCAN, DropbitIntents.RESULT_SCAN_OK, data);

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo("38Lo99XoFPTAsWxh65dkvPPdBNCaqXX4C4"));
        assertTrue(paymentHolder.getPrimaryCurrency().isCrypto());
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(1542869L));
    }

    @Test
    public void clears_pub_key_when_scanning() throws UriException {
        paymentHolder.setPublicKey("--pub-key--");
        Intent data = new Intent();
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        String address = "xfdkjvhbw43hfbwkehvbw43jhkf";
        when(bitcoinUri.getAddress()).thenReturn(address);
        data.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, "bitcoin:xfdkjvhbw43hfbwkehvbw43jhkf");
        when(bitcoinUtil.parse(anyString())).thenReturn(bitcoinUri);
        start();

        dialog.onActivityResult(DropbitIntents.REQUEST_QR_FRAGMENT_SCAN, DropbitIntents.RESULT_SCAN_OK, data);

        assertThat(paymentUtil.getAddress(), equalTo(address));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(address));
        assertThat(paymentHolder.getPublicKey(), equalTo(""));
    }

    // SCAN PAYMENT ADDRESS

    @Test
    public void fetches_address_for_phone_number_when_number_is_valid() {
        start();

        dialog.onPhoneNumberValid(phoneNumber.getPhoneNumber());

        verify(cnAddressLookupDelegate).fetchAddressFor(eq(phoneNumber), any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void nulls_out_contact_on_invalid_phone_number() {
        start();

        String text = "0000000000";
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        withId(paymentReceiverView, R.id.show_phone_input).performClick();
        PhoneNumberInputView phoneNumberInputView = withId(paymentReceiverView, R.id.phone_number_input);
        phoneNumberInputView.setText(text);

        assertThat(phoneNumberInputView.getText(), equalTo("+1"));
        assertNull(paymentUtil.getContact());
    }

    @Test
    public void sets_phone_number_on_payment_util_when_valid_input() {
        start();

        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        withId(paymentReceiverView, R.id.show_phone_input).performClick();
        PhoneNumberInputView phoneNumberInputView = withId(paymentReceiverView, R.id.phone_number_input);
        phoneNumberInputView.setText("3305551111");

        assertThat(paymentUtil.getContact().getPhoneNumber(), equalTo(phoneNumber));
    }

    @Test
    public void picks_contact_to_send() {
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        start();

        clickOn(withId(dialog.getView(), R.id.contacts_btn));

        Intents.intending(hasComponent(PickContactActivity.class.getName()));
        //TODO verify request code passed
    }

    @Test
    public void picking_contact_hides_payment_receiver_view() {
        start();
        Contact contact = new Contact(phoneNumber, "Joe Dirt", true);
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_CONTACT, contact);
        PaymentReceiverView paymentReceiverView = withId(dialog.getView(), R.id.payment_receiver);
        paymentReceiverView.setPaymentAddress(EXTERNAL_ADDRESSES[0]);

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent);

        assertThat(paymentReceiverView, isGone());
        assertThat(paymentReceiverView.getPaymentAddress(), equalTo(""));
        assertThat(paymentReceiverView.getPhoneNumber(), equalTo("+1"));
    }

    @Test
    public void does_not_show_invite_help_when_preference_selected() {
        when(userPreferences.getShouldShowInviteHelp()).thenReturn(false);
        start();

        Contact contact = new Contact(phoneNumber, "Joe Blow", false);

        dialog.startContactInviteFlow(contact);

        verify(paymentBarCallbacks).confirmInvite(contact);
    }

    @Test
    public void fetches_address_for_verified_contact_when_user_picks_one() {
        Contact contact = new Contact(phoneNumber, "Joe Blow", true);
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_CONTACT, contact);
        start();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent);

        assertThat(withId(dialog.getView(), R.id.contact_name), hasText("Joe Blow"));
        assertThat(withId(dialog.getView(), R.id.contact_number), hasText(phoneNumber.toInternationalDisplayText()));
        verify(cnAddressLookupDelegate).fetchAddressFor(eq(contact),
                any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void does_not_fetch_address_for_non_verified_contact_when_user_picks_one() {
        Contact contact = new Contact(phoneNumber, "Joe Dirt", false);
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_CONTACT, contact);
        start();

        dialog.onActivityResult(PayDialogFragment.PICK_CONTACT_REQUEST, AppCompatActivity.RESULT_OK, intent);

        verify(cnAddressLookupDelegate, times(0)).
                fetchAddressFor(any(Contact.class), any(CNAddressLookupDelegate.CNAddressLookupCompleteCallback.class));
    }

    @Test
    public void invites_unverified_contact_after_confirming_help_screen() {
        Contact contact = new Contact(phoneNumber, "Joe Blow", false);
        start();

        dialog.onInviteHelpAccepted(contact);

        verify(paymentBarCallbacks).confirmInvite(contact);
    }

    @Test
    public void directs_user_to_verify_phone_when_selecting_contacts() {
        start();

        dialog.getView().findViewById(R.id.contacts_btn).performClick();

        Intent intent = shadowActivity.peekNextStartedActivity();
        assertThat(intent.getComponent().getClassName(), equalTo(VerifyPhoneNumberActivity.class.getName()));
    }

    @Test
    public void can_cancel_payment_request() {
        paymentHolder.setPaymentAddress("--address--");
        paymentHolder.setPublicKey("--pub-key--");
        start();

        dialog.getView().findViewById(R.id.pay_header_close_btn).performClick();

        verify(paymentBarCallbacks).cancelPayment(dialog);
        assertThat(paymentHolder.getPublicKey(), equalTo(""));
        assertThat(paymentHolder.getPaymentAddress(), equalTo(""));
    }

    @Test
    public void tells_view_types_to_teardown_when_detached() {
        start();
        dialog.memoToggleView = mock(SharedMemoToggleView.class);

        dialog.onDetach();

        verify(dialog.memoToggleView).tearDown();
    }

    @Test
    public void observes_sending_max() {
        when(transactionFundingManger.buildFundedTransactionData(any())).thenReturn(validTransactionData);
        start();

        clickOn(withId(dialog.getView(), R.id.send_max));

        assertTrue(paymentHolder.getPrimaryCurrency() instanceof CryptoCurrency);
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(paymentHolder.getTransactionData().getAmount()));
    }

    @Test
    public void observes_send_max_cleared() {
        when(transactionFundingManger.buildFundedTransactionData(any())).thenReturn(validTransactionData);
        start();

        clickOn(withId(dialog.getView(), R.id.send_max));
        TextView view = withId(dialog.getView(), R.id.primary_currency);
        view.setText("0");

        assertTrue(paymentHolder.getPrimaryCurrency() instanceof CryptoCurrency);
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(0L));
        assertThat(paymentHolder.getTransactionData().getAmount(), equalTo(0L));
        assertThat(paymentHolder.getTransactionData().getUtxos().length, equalTo(0));
        assertFalse(paymentUtil.isFunded());
    }

    private void start() {
        scenario = ActivityScenario.launch(TransactionHistoryActivity.class);
        scenario.onActivity(activity -> {
            dialog.show(activity.getSupportFragmentManager(), "tag");
            shadowActivity = shadowOf(activity);
        });
        scenario.moveToState(Lifecycle.State.RESUMED);
    }

    private void setupDI(Context context) {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.cnAddressLookupDelegae = cnAddressLookupDelegate;
        application.analytics = analytics;
        application.walletHelper = walletHelper;
        application.bitcoinUtil = bitcoinUtil;
        application.clipboardUtil = clipboardUtil;
        application.userPreferences = userPreferences;
        application.countryCodeLocales = countryCodeLocales;

        defaultCurrencies = new DefaultCurrencies(new USDCurrency(), new BTCCurrency());
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(userPreferences.getShouldShowInviteHelp()).thenReturn(true);
        when(walletHelper.getTransactionsLazily()).thenReturn(transactions);
        paymentHolder = new PaymentHolder(new USDCurrency(5000.00d));
        paymentHolder.setDefaultCurrencies(defaultCurrencies);
        paymentHolder.updateValue(new USDCurrency(5000.00d));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "GB"), 44));
        countryCodeLocales.add(new CountryCodeLocale(new Locale("en", "US"), 1));
        paymentUtil = new PaymentUtil(context, bitcoinUtil, transactionFundingManger);
        paymentUtil.setPaymentHolder(paymentHolder);
        paymentUtil.setTransactionFee(new TransactionFee(5, 10, 15));
    }

    private void mockClipboardWithData(String rawString) throws UriException {
        when(clipboardUtil.getRaw()).thenReturn(rawString);
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUri.getAddress()).thenReturn(rawString);
        when(bitcoinUtil.parse(rawString)).thenReturn(bitcoinUri);
    }

}