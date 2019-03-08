package com.coinninja.coinkeeper.view.activity;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.fragment.CalculatorConverstionFragment;
import com.coinninja.coinkeeper.view.fragment.ConfirmPayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.KeyboardFragment;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

import static com.coinninja.matchers.ActivityMatchers.activityWithIntentStarted;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CalculatorActivityTest {

    public static final String PHONE_NUMBER_STRING = "+13305555555";
    PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    CalculatorActivity activity;
    private ActivityController<CalculatorActivity> activityController;

    @Mock
    BitcoinUri bitcoinUri;

    @Mock
    BitcoinUriBuilder bitcoinUriBuilder;

    @Mock
    PaymentUtil paymentUtil;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private InternalNotificationsInteractor notificationsInteractor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(CalculatorActivity.class);
        activityController.create();
        activity = activityController.get();
        activity.walletHelper = walletHelper;
        activity.paymentUtil = paymentUtil;
        activity.paymentHolder = new PaymentHolder();
        activity.paymentHolder.loadPaymentFrom(new USDCurrency(0));
        activity.paymentHolder.setEvaluationCurrency(new USDCurrency(0D));
        activity.bitcoinUriBuilder = bitcoinUriBuilder;
        activity.notificationsInteractor = notificationsInteractor;
    }

    @After
    public void tearDown() throws Exception {
        activity = null;
        activityController = null;
        bitcoinUri = null;
        bitcoinUriBuilder = null;
        paymentUtil = null;
        walletHelper = null;
        notificationsInteractor = null;
    }

    void start() {
        activityController.resume().start().visible();
    }

    @Test
    public void shows_transaction_history_when_started_with_txid_in_starting_intent() {
        Intent startingIntent = new Intent();
        startingIntent.putExtra(Intents.EXTRA_TRANSACTION_ID, "--txid--");
        activity.setIntent(startingIntent);
        start();

        Intent intent = new Intent(activity, TransactionHistoryActivity.class);
        intent.putExtra(Intents.EXTRA_TRANSACTION_ID, "--txid--");
        assertThat(activity, activityWithIntentStarted(intent));
        assertFalse(activity.getIntent().hasExtra(Intents.EXTRA_TRANSACTION_ID));
    }

    @Test
    public void pay_dialog_is_not_cancelable() {
        String address = "---- btc address";
        when(paymentUtil.getAddress()).thenReturn(address);
        start();
        ((TabLayout) activity.findViewById(R.id.id_navigation_tabs)).getTabAt(1).select();
        activity.findViewById(R.id.one).performClick();

        activity.findViewById(R.id.send_btn).performClick();

        PayDialogFragment fragment = (PayDialogFragment)
                activity.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());
        assertNotNull(fragment);
        assertFalse(fragment.isCancelable());
    }

    @Ignore // TODO move this to instrumentation
    @Test
    public void dismiss_confirm_when_started_with_address() {
        start();
        String address = "--- btc address";

        activity.confirmPaymentFor(address);

        Fragment fragment = activity.getFragmentManager().findFragmentByTag(ConfirmPayDialogFragment.class.getSimpleName());
        assertNotNull(fragment);

        fragment.getView().findViewById(R.id.confirm_pay_header_close_btn).performClick();

        assertNull(activity.getFragmentManager().findFragmentByTag(ConfirmPayDialogFragment.class.getSimpleName()));
    }

    @Test
    public void dismisses_pay_dialog_when_user_request_close() {
        String address = "---- btc address";
        when(paymentUtil.getAddress()).thenReturn(address);
        start();
        ((TabLayout) activity.findViewById(R.id.id_navigation_tabs)).getTabAt(1).select();
        activity.findViewById(R.id.one);
        DialogFragment fragment = mock(DialogFragment.class);
        activity.cancelPayment(fragment);

        verify(paymentUtil).setAddress(null);
        verify(fragment).dismiss();
        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(new USDCurrency(0d).toFormattedCurrency()));
        assertThat(activity.currentTab.getText(), equalTo(activity.getText(R.string.tab_calculator_usd)));
    }

    @Test
    public void confirms_invite_for_contact() {
        Contact contact = new Contact(phoneNumber, "Joe Blow", false);
        activity.paymentHolder.loadPaymentFrom(new BTCCurrency(1d));

        activity.confirmInvite(contact);

        ConfirmPayDialogFragment fragment = (ConfirmPayDialogFragment) activity.getFragmentManager().findFragmentByTag(ConfirmPayDialogFragment.class.getSimpleName());
        assertNotNull(fragment);
        assertThat(fragment.getContact(), equalTo(contact));
        assertThat(fragment.getPriceHolder(), equalTo(activity.paymentHolder));
        assertFalse(fragment.isCancelable());
    }

    @Test
    public void confirms_payment_for_given_address() {
        String address = "--addr--";
        activity.paymentHolder.loadPaymentFrom(new BTCCurrency(1d));

        activity.confirmPaymentFor(address);

        ConfirmPayDialogFragment fragment = (ConfirmPayDialogFragment) activity.getFragmentManager().findFragmentByTag(ConfirmPayDialogFragment.class.getSimpleName());
        assertNotNull(fragment);
        assertThat(fragment.getPriceHolder(), equalTo(activity.paymentHolder));
        assertThat(fragment.getSendAddress(), equalTo(address));
        assertFalse(fragment.isCancelable());
    }

    @Test
    public void confirms_payment_for_given_address_and_contact() {
        Contact contact = new Contact(phoneNumber, "Joe Blow", true);
        String address = "--addr--";
        activity.paymentHolder.loadPaymentFrom(new BTCCurrency(1d));

        activity.confirmPaymentFor(address, contact);

        ConfirmPayDialogFragment fragment = (ConfirmPayDialogFragment) activity.getFragmentManager().findFragmentByTag(ConfirmPayDialogFragment.class.getSimpleName());
        assertNotNull(fragment);
        assertThat(fragment.getContact(), equalTo(contact));
        assertThat(fragment.getPriceHolder(), equalTo(activity.paymentHolder));
        assertThat(fragment.getSendAddress(), equalTo(address));
        assertFalse(fragment.isCancelable());
    }

    @Test
    public void sets_payment_on_request_dialog() {
        start();
        PaymentHolder holder = mock(PaymentHolder.class);
        activity.paymentHolder = holder;

        activity.findViewById(R.id.request_btn).performClick();

        RequestDialogFragment fragment = (RequestDialogFragment)
                activity.getFragmentManager().findFragmentByTag(RequestDialogFragment.class.getSimpleName());

        assertThat(fragment.getPaymentHolder(), equalTo(holder));
    }

    @Test
    public void sets_currently_inputted_price_when_requesting() {
        start();
        PaymentHolder holder = mock(PaymentHolder.class);
        activity.paymentHolder = holder;

        activity.findViewById(R.id.request_btn).performClick();

        verify(holder).loadPaymentFrom(any(Currency.class));
    }

    @Test
    public void sets_currently_inputted_price_when_sending() {
        start();
        PaymentHolder holder = mock(PaymentHolder.class);
        activity.paymentHolder = holder;

        activity.findViewById(R.id.send_btn).performClick();

        PayDialogFragment fragment = (PayDialogFragment)
                activity.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());

        verify(holder).loadPaymentFrom(any(Currency.class));
        verify(paymentUtil).setPaymentHolder(activity.paymentHolder);
        assertNotNull(fragment);
    }

    @Test
    public void initializes_calculator_fragment() {
        start();
        assertNotNull(activity.getFragmentManager().
                findFragmentByTag(CalculatorActivity.CONVERSION_FRAGMENT_TAG));
    }

    @Test
    public void changing_tab__from_btc_to_usd_loads_usd_as_primary() {
        BTCCurrency currency = new BTCCurrency(100000L);
        TabLayout.Tab tab = mock(TabLayout.Tab.class);
        when(tab.getText()).thenReturn(activity.getText(R.string.tab_calculator_usd));
        activity.paymentHolder.loadPaymentFrom(currency);
        start();
        activity.onPriceReceived(new USDCurrency(6000d));

        activity.onTabSelected(tab);

        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo("$6.00"));
    }

    @Test
    public void changing_tab__from_btc_to_btc_retains_btc() {
        TabLayout.Tab tab = mock(TabLayout.Tab.class);
        when(tab.getText()).thenReturn(activity.getText(R.string.tab_calculator_btc));
        BTCCurrency currency = new BTCCurrency(1.0d);
        activity.paymentHolder.loadPaymentFrom(currency);
        start();
        activity.onPriceReceived(new USDCurrency(6000d));

        activity.onTabSelected(tab);

        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(currency.toFormattedCurrency()));

    }

    @Test
    public void changing_tab__from_usd_to_btc_loads_btc_as_primary() {
        TabLayout.Tab tab = mock(TabLayout.Tab.class);
        when(tab.getText()).thenReturn(activity.getText(R.string.tab_calculator_btc));
        activity.paymentHolder.loadPaymentFrom(new USDCurrency(6000d));
        start();
        activity.onPriceReceived(new USDCurrency(6000d));

        activity.onTabSelected(tab);

        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo(new BTCCurrency(1d).toFormattedCurrency()));
    }

    @Test
    public void changing_tab__from_usd_to_usd_retains_usd() {
        TabLayout.Tab tab = mock(TabLayout.Tab.class);
        when(tab.getText()).thenReturn(activity.getText(R.string.tab_calculator_usd));
        activity.paymentHolder.loadPaymentFrom(new USDCurrency(6000d));
        start();
        activity.onPriceReceived(new USDCurrency(6000d));

        activity.onTabSelected(tab);

        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo("$6,000.00")
        );
    }

    @Test
    public void initalizes_with_usd_currency_as_primary() {
        start();
        assertTrue(activity.getCurrentCurrencyState() instanceof USDCurrency);
        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo("$0.00"));
    }

    @Test
    public void initializes_with_available_balance() {
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(10L));
        start();
        assertThat(activity.paymentHolder.getSpendableBalance().toSatoshis(), equalTo(10L));
    }

    @Test
    public void updates_spendable_balance_when_wallet_sync_completes() {
        long balance = 1000000L;
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(0)).
                thenReturn(new BTCCurrency(balance));
        start();

        activity.onWalletSyncComplete();

        assertThat(activity.paymentHolder.getSpendableBalance().toSatoshis(), equalTo(balance));
    }

    @Test
    public void updates_holder_with_value_of_crypto_changes() {
        start();
        activity.paymentHolder.setEvaluationCurrency(new USDCurrency(500d));

        activity.onPriceReceived(new USDCurrency(6500d));

        assertThat(activity.paymentHolder.getEvaluationCurrency().toFormattedCurrency(),
                equalTo("$6,500.00"));
    }

    @Test
    public void updates_payment_holder_when_fees_update() {
        start();
        activity.paymentHolder.setTransactionFee(new TransactionFee(0, 0, 0));

        activity.onTransactionFeeUpdated(new TransactionFee(5, 10, 15));

        assertThat(activity.paymentHolder.getTransactionFee().getMin(), equalTo(5d));
    }

    @Test
    public void itHasAContentView() {
        start();
        assertNotNull(activity.findViewById(R.id.keyboard));
    }

    @Test
    public void itContainsACalculatorKeyboard() {
        start();
        assertNotNull(activity.getFragmentManager().findFragmentById(R.id.keyboard));
    }

    @Test
    public void addsConverstionFragmentToScreenWhenCreated() {
        start();
        assertNotNull(activity.getFragmentManager().findFragmentByTag("TAG_CONVERSION_FRAGMENT"));
    }

    @Test
    public void attachesOnKeyListenerToKeyboardFragment() {
        start();
        KeyboardFragment keyboard = (KeyboardFragment) activity.getFragmentManager().
                findFragmentById(R.id.keyboard);

        CalculatorConverstionFragment convesionFragment = (CalculatorConverstionFragment)
                activity.getFragmentManager().findFragmentByTag(CalculatorActivity.CONVERSION_FRAGMENT_TAG);

        assertThat(keyboard.getOnKeyListener(), equalTo(convesionFragment));
    }

    @Test
    public void forwardsChangesInBTCprice() {
        start();
        CalculatorConverstionFragment convesionFragment = (CalculatorConverstionFragment)
                activity.getFragmentManager().
                        findFragmentByTag(CalculatorActivity.CONVERSION_FRAGMENT_TAG);


        activity.onPriceReceived(new USDCurrency(600d));

        assertThat(convesionFragment.getEvaluationCurrency().toFormattedCurrency(), equalTo("$600.00"));

    }

    @Test
    public void qrscan_good_Bitcoin_Address_and_amount() throws UriException {
        start();
        String scannedData = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=10.00000000";
        when(bitcoinUriBuilder.parse(scannedData)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        when(bitcoinUri.getSatoshiAmount()).thenReturn(1000000000L);

        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        activity.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);
        PayDialogFragment payDialogFragment = (PayDialogFragment) activity.getFragmentManager().findFragmentByTag("PayDialogFragment");

        assertNotNull(payDialogFragment);
        verify(paymentUtil).setAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        assertThat(activity.paymentHolder.getPrimaryCurrency().toFormattedCurrency(),
                equalTo("\u20BF 10"));
    }

    @Test
    public void qrscan_good_Bitcoin_Address() throws UriException {
        start();
        String scannedData = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        when(bitcoinUriBuilder.parse(scannedData)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        activity.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);

        PayDialogFragment payDialogFragment = (PayDialogFragment) activity.getFragmentManager().findFragmentByTag("PayDialogFragment");
        assertNotNull(payDialogFragment);
        verify(paymentUtil).setAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
    }

    @Test
    public void qrscan_good_Bitcoin_Address_amount_from_calculator_page() throws UriException {
        start();
        PaymentHolder holder = mock(PaymentHolder.class);
        activity.paymentHolder = holder;

        String scannedData = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        when(bitcoinUriBuilder.parse(scannedData)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        activity.onQrScanPressed();
        activity.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);

        PayDialogFragment payDialogFragment = (PayDialogFragment) activity.getFragmentManager().findFragmentByTag("PayDialogFragment");
        assertNotNull(payDialogFragment);
        verify(paymentUtil).setAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");

        verify(holder).loadPaymentFrom(any(Currency.class));
    }

    @Test
    public void shows_error_when_failing_to_parse_btc_uri() throws UriException {
        start();
        String scannedData = "jibberish";
        String expectedMessage = activity.getResources().getString(R.string.invalid_bitcoin_address_description);
        when(bitcoinUriBuilder.parse(scannedData)).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN));
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        activity.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);

        assertThat(shadowAlertDialog.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void qrscan_RESULT_SCAN_ERROR_test() {
        start();
        String scannedData = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        String expectedMessage = activity.getResources().getString(R.string.invalid_bitcoin_address_description);

        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        activity.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_ERROR, intent);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);

        assertThat(shadowAlertDialog.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void check_for_notifications_on_start() {
        start();
        verify(notificationsInteractor).startListeningForNotifications(anyBoolean());
    }

    @Test
    public void check_for_removing_notifications_on_pause() {
        start();
        activityController.pause();
        verify(notificationsInteractor).stopListeningForNotifications();
    }
}