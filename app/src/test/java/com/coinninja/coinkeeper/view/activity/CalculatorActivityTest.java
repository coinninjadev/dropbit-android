package com.coinninja.coinkeeper.view.activity;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.fragment.CalculatorConverstionFragment;
import com.coinninja.coinkeeper.view.fragment.ConfirmPayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.KeyboardFragment;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.google.android.material.tabs.TabLayout;

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


@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CalculatorActivityTest {

    public static final String PHONE_NUMBER_STRING = "+13305555555";
    PhoneNumber phoneNumber = new PhoneNumber(PHONE_NUMBER_STRING);
    CalculatorActivity activity;
    @Mock
    BitcoinUri bitcoinUri;
    @Mock
    BitcoinUtil bitcoinUtil;
    @Mock
    PaymentUtil paymentUtil;
    private ActivityController<CalculatorActivity> activityController;
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
        activity.bitcoinUtil = bitcoinUtil;
        activity.notificationsInteractor = notificationsInteractor;
    }

    @After
    public void tearDown() throws Exception {
        activity = null;
        activityController = null;
        bitcoinUri = null;
        bitcoinUtil = null;
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