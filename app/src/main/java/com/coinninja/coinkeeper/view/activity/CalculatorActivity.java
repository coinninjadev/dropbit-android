package com.coinninja.coinkeeper.view.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.InternalNotificationsInteractor;
import com.coinninja.coinkeeper.model.PaymentHolder;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PaymentUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity;
import com.coinninja.coinkeeper.view.fragment.CalculatorConverstionFragment;
import com.coinninja.coinkeeper.view.fragment.KeyboardFragment;
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment;
import com.coinninja.coinkeeper.view.fragment.RequestDialogFragment;
import com.coinninja.coinkeeper.view.widget.PaymentBarView;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class CalculatorActivity extends BalanceBarActivity implements TabLayout.OnTabSelectedListener {

    static final String CONVERSION_FRAGMENT_TAG = "TAG_CONVERSION_FRAGMENT";
    public TabLayout.Tab currentTab;
    CalculatorConverstionFragment calculatorConverstionFragment;

    @Inject
    WalletHelper walletHelper;

    @Inject
    PaymentHolder paymentHolder;

    @Inject
    PaymentUtil paymentUtil;

    //TODO Inject this
    InternalNotificationsInteractor notificationsInteractor;

    @Inject
    BitcoinUtil bitcoinUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_calculator);
        addTabbar(R.layout.tabbar_activity_calculator);
        paymentHolder.loadPaymentFrom(new USDCurrency(0d));
        notificationsInteractor = new InternalNotificationsInteractor(this, (CoinKeeperApplication) getApplication(), new LocalBroadCastUtil(getApplication()));
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }


    @Override
    protected void onResume() {
        notificationsInteractor.startListeningForNotifications(true);
        super.onResume();
        paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
        showTxIfPresent();
        initConversionFactoryWith(paymentHolder.getPrimaryCurrency());
    }


    @Override
    protected void onStart() {
        super.onStart();
        ((TabLayout) findViewById(R.id.id_navigation_tabs)).addOnTabSelectedListener(this);
    }

    @Override
    protected void onPause() {
        notificationsInteractor.stopListeningForNotifications();
        super.onPause();
    }

    @Override
    public void onPriceReceived(USDCurrency price) {
        super.onPriceReceived(price);
        paymentHolder.setEvaluationCurrency(price);
        if (calculatorConverstionFragment != null) {
            calculatorConverstionFragment.onPriceRecieved(price);
        }
    }

    @Override
    protected void onWalletSyncComplete() {
        super.onWalletSyncComplete();
        paymentHolder.setSpendableBalance(walletHelper.getSpendableBalance());
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        currentTab = tab;
        Currency currency = getCurrentCurrencyState();
        if (tab.getText().equals(getResources().getText(R.string.tab_calculator_usd))) {
            paymentHolder.loadPaymentFrom(currency.toUSD(paymentHolder.getEvaluationCurrency()));
        } else {
            paymentHolder.loadPaymentFrom(currency.toBTC(paymentHolder.getEvaluationCurrency()));
        }

        initConversionFactoryWith(paymentHolder.getPrimaryCurrency());
    }

    Currency getCurrentCurrencyState() {
        return calculatorConverstionFragment.getCurrentCurrencyState();
    }

    void initConversionFactoryWith(Currency currency) {
        calculatorConverstionFragment = CalculatorConverstionFragment.newInstance(currency);
        calculatorConverstionFragment.onPriceRecieved(paymentHolder.getEvaluationCurrency());
        attachConversionFragment(calculatorConverstionFragment);
    }

    private void attachConversionFragment(CalculatorConverstionFragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragmentManager.findFragmentByTag(CONVERSION_FRAGMENT_TAG) == null) {
            fragmentTransaction.add(R.id.content, fragment, CONVERSION_FRAGMENT_TAG);
        } else {
            fragmentTransaction.replace(R.id.content, fragment, CONVERSION_FRAGMENT_TAG);
        }

        fragmentTransaction.commit();
        KeyboardFragment keyboardFragment = (KeyboardFragment) fragmentManager.findFragmentById(R.id.keyboard);
        keyboardFragment.setOnKeyListener(fragment);
    }


    private void selectDefaultTab() {
        TabLayout tabLayout = findViewById(R.id.id_navigation_tabs);
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        tab.select();
    }

    private void showTxIfPresent() {
        if (!getIntent().hasExtra(Intents.EXTRA_TRANSACTION_ID)) return;

        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        intent.putExtra(Intents.EXTRA_TRANSACTION_ID,
                getIntent().getStringExtra(Intents.EXTRA_TRANSACTION_ID));
        startActivity(intent);
        getIntent().removeExtra(Intents.EXTRA_TRANSACTION_ID);
    }

    @Deprecated
    public void setNotifications(InternalNotificationsInteractor internalNotificationsInteractor) {
        notificationsInteractor = internalNotificationsInteractor;
    }
}
