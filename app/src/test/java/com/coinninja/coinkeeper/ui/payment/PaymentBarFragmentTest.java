package com.coinninja.coinkeeper.ui.payment;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
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
import org.junit.Ignore;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    PaymentHolder paymentHolder = new PaymentHolder();

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    private long initialUSDValue = 10000L;
    private TransactionFee initialFee = new TransactionFee(0, 5, 10);
    private FragmentController<PaymentBarFragment> fragmentController;
    private PaymentBarFragment fragment;
    private View requestButton;
    private View sendButton;
    private View scanButton;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        fragmentController = Robolectric.buildFragment(PaymentBarFragment.class).create();
        fragment = fragmentController.get();
        fragment.paymentHolder = paymentHolder;
        fragment.paymentUtil = paymentUtil;
        fragment.walletHelper = walletHelper;
        fragment.bitcoinUtil = bitcoinUtil;
        fragment.localBroadcastUtil = localBroadCastUtil;

        scanButton = withId(fragment.getView(), R.id.scan_btn);
        sendButton = withId(fragment.getView(), R.id.send_btn);
        requestButton = withId(fragment.getView(), R.id.request_btn);

        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(initialUSDValue));
        when(walletHelper.getLatestFee()).thenReturn(initialFee);
        when(paymentUtil.getPaymentHolder()).thenReturn(paymentHolder);
    }

    @After
    public void tearDown() {
        requestButton = null;
        sendButton = null;
        localBroadCastUtil = null;
        scanButton = null;
        walletHelper = null;
        paymentUtil = null;
        paymentHolder = null;
        initialFee = null;
        fragmentController = null;
        fragment = null;
        bitcoinUtil = null;
    }

    private void start() {
        fragmentController.start().resume().visible();
    }

    @Test
    public void initializes_with_primary_currency() {
        start();

        assertThat(paymentHolder.getPrimaryCurrency().toFormattedCurrency(), equalTo(new BTCCurrency().toFormattedCurrency()));
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
        PayDialogFragment payDialog = (PayDialogFragment) fragment.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());
        assertNotNull(payDialog);
        assertThat(payDialog.getPaymentUtil(), equalTo(fragment.paymentUtil));
    }

    @Test
    public void shows_request_dialog() {
        start();

        clickOn(requestButton);

        RequestDialogFragment requestDialogFragment = (RequestDialogFragment) fragment.getFragmentManager().findFragmentByTag(RequestDialogFragment.class.getSimpleName());
        assertNotNull(requestDialogFragment);
        assertThat(requestDialogFragment.getPaymentHolder(), equalTo(paymentHolder));
    }

    @Test
    public void shows_request_scan() {
        start();

        clickOn(scanButton);

        ShadowActivity shadowActivity = shadowOf(fragment.getActivity());
        ShadowActivity.IntentForResult forResult = shadowActivity.getNextStartedActivityForResult();

        assertNotNull(forResult);
        assertThat(forResult.requestCode, equalTo(Intents.REQUEST_QR_ACTIVITY_SCAN));
        assertThat(forResult.intent.getComponent().getClassName(), equalTo(QrScanActivity.class.getName()));
    }

    @Test
    public void handles_invalid_scan() {
        String scannedData = "--- does not matter - mocking behavior ---";
        String expectedMessage = fragment.getResources().getString(R.string.invalid_bitcoin_address_description);
        start();

        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);
        fragment.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_ERROR, intent);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void handles_invalid_scan__fail_to_parse() throws UriException {
        String scannedData = "--- does not matter - mocking behavior ---";
        String expectedMessage = fragment.getResources().getString(R.string.invalid_bitcoin_address_description);
        when(bitcoinUtil.parse(scannedData)).thenThrow(new UriException(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN));
        start();

        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);
        fragment.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);

        AlertDialog alert = ShadowAlertDialog.getLatestAlertDialog();
        ShadowAlertDialog shadowAlertDialog = shadowOf(alert);
        assertThat(shadowAlertDialog.getMessage(), equalTo(expectedMessage));
    }

    @Test
    public void handles_good_scan___address_only() throws UriException {
        start();
        String scannedData = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        when(bitcoinUtil.parse(scannedData)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        fragment.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);

        PayDialogFragment payDialog = (PayDialogFragment) fragment.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());

        assertNotNull(payDialog);
        verify(paymentUtil).setAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
    }

    @Test
    public void handles_good_scan___address_with_amount() throws UriException {
        start();
        BitcoinUri bitcoinUri = mock(BitcoinUri.class);
        String scannedData = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=10.00000000";
        when(bitcoinUtil.parse(scannedData)).thenReturn(bitcoinUri);
        when(bitcoinUri.getAddress()).thenReturn("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        when(bitcoinUri.getSatoshiAmount()).thenReturn(1000000000L);

        Intent intent = new Intent();
        intent.putExtra(Intents.EXTRA_SCANNED_DATA, scannedData);

        fragment.onActivityResult(Intents.REQUEST_QR_ACTIVITY_SCAN, Intents.RESULT_SCAN_OK, intent);
        PayDialogFragment payDialog = (PayDialogFragment) fragment.getFragmentManager().findFragmentByTag(PayDialogFragment.class.getSimpleName());

        assertNotNull(payDialog);
        verify(paymentUtil).setAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa");
        assertThat(paymentHolder.getPrimaryCurrency().toLong(), equalTo(1000000000L));
    }

}