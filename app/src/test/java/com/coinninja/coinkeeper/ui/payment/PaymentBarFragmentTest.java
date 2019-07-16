package com.coinninja.coinkeeper.ui.payment;

import android.content.Intent;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.ui.payment.request.RequestDialogFragment;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.FeesManager;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.matchers.IntentFilterMatchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PaymentBarFragmentTest {

    @Mock
    WalletHelper walletHelper;
    @Mock
    PaymentUtil paymentUtil;
    @Mock
    ActivityNavigationUtil activityNavigationUtil;
    @Mock
    FeesManager feesManager;

    @Mock
    BitcoinUtil bitcoinUtil;

    @Mock
    CurrencyPreference currencyPreference;

    PaymentHolder paymentHolder = new PaymentHolder();

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    private long initialUSDValue = 10000L;
    private TransactionFee initialFee = new TransactionFee(0, 5, 10);
    private PaymentBarFragment fragment;
    private View requestButton;
    private View sendButton;

    private USDCurrency usdCurrency = new USDCurrency();
    private BTCCurrency btcCurrency = new BTCCurrency();

    private DefaultCurrencies defaultCurrencies;
    private FragmentScenario<PaymentBarFragment> fragmentScenario;

    @Before
    public void setUp() {
        configureDI();
        fragmentScenario = FragmentScenario.launch(PaymentBarFragment.class);
        fragmentScenario.onFragment(frag -> {
            fragment = frag;
            fragment.paymentHolder = paymentHolder;
            fragment.paymentUtil = paymentUtil;
            fragment.feesManager = feesManager;
        });
        when(feesManager.currentFee()).thenReturn(initialFee.getSlow());

        sendButton = withId(fragment.getView(), R.id.send_btn);
        requestButton = withId(fragment.getView(), R.id.request_btn);
    }

    @After
    public void tearDown() {
        requestButton = null;
        sendButton = null;
        activityNavigationUtil = null;
        localBroadCastUtil = null;
        walletHelper = null;
        paymentUtil = null;
        paymentHolder = null;
        initialFee = null;
        bitcoinUtil = null;
        defaultCurrencies = null;
        fragment = null;
        fragmentScenario.moveToState(Lifecycle.State.DESTROYED);
    }

    @Test
    public void observes_wallet_sync_complete() {
        verify(localBroadCastUtil).registerReceiver(fragment.receiver, fragment.intentFilter);

        assertThat(fragment.intentFilter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));
    }

    @Test
    public void updates_spendable_balance_after_wallet_sync() {
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(50000L))
                .thenReturn(new BTCCurrency(10L));

        fragment.receiver.onReceive(fragment.getContext(), new Intent(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE));
        clickOn(sendButton);

        assertThat(paymentHolder.getSpendableBalance().toLong(), equalTo(10L));
    }

    @Test
    public void stops_observing_wallet_sync_when_stopped() {
        fragmentScenario.moveToState(Lifecycle.State.DESTROYED);


        verify(localBroadCastUtil).unregisterReceiver(fragment.receiver);
    }

    @Test
    public void shows_payment_dialog() {
        when(walletHelper.getSpendableBalance()).thenReturn(new BTCCurrency(50000L));
        ArgumentCaptor<PayDialogFragment> argumentCaptor = ArgumentCaptor.forClass(PayDialogFragment.class);

        clickOn(sendButton);

        verify(activityNavigationUtil).showDialogWithTag(eq(fragment.getFragmentManager()),
                argumentCaptor.capture(), eq(PayDialogFragment.class.getSimpleName()));

        PayDialogFragment payDialogFragment = argumentCaptor.getValue();
        assertThat(paymentHolder.getEvaluationCurrency().toLong(), equalTo(initialUSDValue));
        assertThat(paymentHolder.getSpendableBalance().toLong(), equalTo(walletHelper.getSpendableBalance().toLong()));
        verify(paymentUtil).setFee(initialFee.getSlow());
        verify(fragment.paymentUtil).setPaymentHolder(fragment.paymentHolder);
        when(paymentUtil.getPaymentHolder()).thenReturn(fragment.paymentHolder);
        assertThat(payDialogFragment.getPaymentUtil(), equalTo(fragment.paymentUtil));
        assertThat(payDialogFragment.getPaymentUtil().getPaymentHolder(), equalTo(fragment.paymentHolder));
    }

    @Test
    public void shows_request_dialog() {
        ArgumentCaptor<RequestDialogFragment> argumentCaptor = ArgumentCaptor.forClass(RequestDialogFragment.class);

        clickOn(requestButton);

        verify(activityNavigationUtil).showDialogWithTag(eq(fragment.getFragmentManager()),
                argumentCaptor.capture(), eq(RequestDialogFragment.class.getSimpleName()));
    }

    @Test
    public void clears_payment_info_when_payment_canceled() {
        clickOn(sendButton);
        paymentHolder.setPaymentAddress("--address--");

        PayDialogFragment payDialog = mock(PayDialogFragment.class);
        fragment.cancelPayment(payDialog);

        assertThat(fragment.paymentHolder.getPaymentAddress(), equalTo(""));
        verify(payDialog).dismiss();
    }

    private void configureDI() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        application.bitcoinUtil = bitcoinUtil;
        application.localBroadCastUtil = localBroadCastUtil;
        application.currencyPreference = currencyPreference;
        application.walletHelper = walletHelper;
        application.activityNavigationUtil = activityNavigationUtil;
        defaultCurrencies = new DefaultCurrencies(btcCurrency, usdCurrency);
        when(walletHelper.getLatestPrice()).thenReturn(new USDCurrency(initialUSDValue));
        when(walletHelper.getLatestFee()).thenReturn(initialFee);
        when(paymentUtil.getPaymentHolder()).thenReturn(paymentHolder);
        when(currencyPreference.getCurrenciesPreference()).thenReturn(defaultCurrencies);
        when(currencyPreference.getFiat()).thenReturn(usdCurrency);
    }

}