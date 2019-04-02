package com.coinninja.coinkeeper.ui.payment;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.QrScanActivity;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.coinninja.matchers.IntentFilterMatchers;

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
import org.robolectric.shadows.ShadowAlertDialog;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class PaymentBarFragmentTest {

    @Mock
    WalletHelper walletHelper;
    @Mock
    PaymentUtil paymentUtil;

    @Mock
    BitcoinUtil bitcoinUtil;

    @Mock
    CurrencyPreference currencyPreference;

    PaymentHolder paymentHolder = new PaymentHolder();

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    private long initialUSDValue = 10000L;
    private TransactionFee initialFee = new TransactionFee(0, 5, 10);
    private FragmentController<PaymentBarFragment> fragmentController;
    private PaymentBarFragment fragment;
    private View requestButton;
    private View sendButton;

    private USDCurrency usdCurrency = new USDCurrency();
    private BTCCurrency btcCurrency = new BTCCurrency();

    private DefaultCurrencies defaultCurrencies;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        fragmentController = Robolectric.buildFragment(PaymentBarFragment.class).create();
        fragment = fragmentController.get();
        fragment.paymentHolder = paymentHolder;
        fragment.paymentUtil = paymentUtil;
        fragment.walletHelper = walletHelper;
        fragment.bitcoinUtil = bitcoinUtil;
        fragment.currencyPreference = currencyPreference;
        fragment.localBroadcastUtil = localBroadCastUtil;
        defaultCurrencies = new DefaultCurrencies(btcCurrency, usdCurrency);

        sendButton = withId(fragment.getView(), R.id.send_btn);
        requestButton = withId(fragment.getView(), R.id.request_btn);

        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(initialUSDValue));
        when(walletHelper.getLatestFee()).thenReturn(initialFee);
        when(paymentUtil.getPaymentHolder()).thenReturn(paymentHolder);
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(currencyPreference.getFiat()).thenReturn(usdCurrency);
    }

    @After
    public void tearDown() {
        requestButton = null;
        sendButton = null;
        localBroadCastUtil = null;
        walletHelper = null;
        paymentUtil = null;
        paymentHolder = null;
        initialFee = null;
        fragmentController = null;
        fragment = null;
        bitcoinUtil = null;
        defaultCurrencies = null;
    }

    private void start() {
        fragmentController.start().resume().visible();
    }

    @Test
    public void observes_wallet_sync_complete() {
        start();

        verify(localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter);
        assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(Intents.ACTION_WALLET_SYNC_COMPLETE));
    }

    @Test
    public void updates_spendable_balance_after_wallet_sync() {
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(50000L)).thenReturn(new BTCCurrency(10L));
        start();

        fragment.receiver.onReceive(fragment.getContext(), new Intent(Intents.ACTION_WALLET_SYNC_COMPLETE));

        assertThat(paymentHolder.getSpendableBalance().toLong(), equalTo(10L));
    }

    @Test
    public void stops_observing_wallet_sync_when_stopped() {
        start();

        fragmentController.pause().stop();

        verify(localBroadCastUtil).unregisterReceiver(fragment.receiver);
    }

    @Test
    public void shows_payment_dialog() {
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(50000L));
        start();

        clickOn(sendButton);

        assertThat(paymentHolder.getSpendableBalance().toLong(), equalTo(walletHelper.getSpendableBalance().toLong()));
        assertThat(paymentHolder.getEvaluationCurrency().toLong(), equalTo(initialUSDValue));
        assertThat(paymentHolder.getTransactionFee(), equalTo(initialFee));
        verify(fragment.paymentUtil).setPaymentHolder(fragment.paymentHolder);
        when(paymentUtil.getPaymentHolder()).thenReturn(fragment.paymentHolder);
        PayDialogFragment payDialog = (PayDialogFragment) fragment.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());
        assertNotNull(payDialog);
        assertThat(payDialog.getPaymentUtil(), equalTo(fragment.paymentUtil));
        assertThat(payDialog.getPaymentUtil().getPaymentHolder(), equalTo(fragment.paymentHolder));
    }

    @Test
    public void shows_request_dialog() {
        start();

        clickOn(requestButton);

        RequestDialogFragment requestDialogFragment = (RequestDialogFragment) fragment.getFragmentManager().findFragmentByTag(RequestDialogFragment.class.getSimpleName());
        assertNotNull(requestDialogFragment);

        PaymentHolder paymentHolder = requestDialogFragment.getPaymentHolder();
        assertThat(paymentHolder.getCryptoCurrency().toLong(), equalTo(0L));
        assertThat(paymentHolder.getFiat().toLong(), equalTo(0L));
        assertThat(paymentHolder.getEvaluationCurrency().toLong(), equalTo(initialUSDValue));
    }

    @Test
    public void clears_payment_info_when_payment_canceled() {
        start();

        clickOn(sendButton);
        paymentHolder.setPaymentAddress("--address--");

        PayDialogFragment payDialog = (PayDialogFragment) fragment.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());
        fragment.cancelPayment(payDialog);

        assertThat(fragment.paymentHolder.getPaymentAddress(), equalTo(""));
    }

    @Test
    public void clears_payment_info_when_processing_new_payment() {
        start();
        paymentHolder.setPaymentAddress("--address--");

        clickOn(requestButton);

        assertThat(fragment.paymentHolder.getPaymentAddress(), equalTo(""));
    }
}