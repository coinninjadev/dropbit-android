package com.coinninja.coinkeeper.view.activity.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

import static com.coinninja.android.helpers.Resources.scaleValue;
import static com.coinninja.android.helpers.Views.withId;
import static com.coinninja.coinkeeper.util.Intents.ACTION_BTC_PRICE_UPDATE;
import static com.coinninja.coinkeeper.util.Intents.ACTION_WALLET_SYNC_COMPLETE;
import static com.coinninja.coinkeeper.util.Intents.EXTRA_BITCOIN_PRICE;

public abstract class BalanceBarActivity extends SecuredActivity implements ServiceConnection {

    @Inject
    CurrencyPreference currencyPreference;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    WalletHelper walletHelper;

    BlockChainService.BlockChainBinder serviceBinder;
    BroadcastReceiver receiver;
    IntentFilter filter;
    private USDCurrency currentPrice;
    private BTCCurrency btcBalance;
    private TextView primaryBalance;
    private TextView secondaryBalance;
    private DefaultCurrencies defaultCurrencies;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        receiver = new Receiver();
        filter = new IntentFilter(ACTION_WALLET_SYNC_COMPLETE);
        filter.addAction(ACTION_BTC_PRICE_UPDATE);
        bindService(new Intent(this, BlockChainService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findViewById(R.id.balance).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        defaultCurrencies = currencyPreference.getCurrenciesPreference();
        withId(this, R.id.balance).setOnClickListener(v -> toggleDefaultCurrency());
        primaryBalance = withId(this, R.id.primary_balance);
        secondaryBalance = withId(this, R.id.alt_balance);
        primaryBalance.setCompoundDrawablePadding(10);
        secondaryBalance.setCompoundDrawablePadding(10);
    }

    @Override
    protected void onResume() {
        super.onResume();
        localBroadCastUtil.registerReceiver(receiver, filter);
        registerReceiver(receiver, filter);
        invalidateBalance();
    }

    @Override
    protected void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        if (null != serviceBinder) {
            unbindService(this);
        }
        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        serviceBinder = (BlockChainService.BlockChainBinder) binder;
        fetchBTCPrice();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        serviceBinder = null;
    }

    @Override
    protected void onPriceReceived(USDCurrency price) {
        super.onPriceReceived(price);
        invalidateBalance();
    }

    protected void fetchBTCPrice() {
        if (serviceBinder != null && serviceBinder.isBinderAlive())
            serviceBinder.getService().fetchCurrentState();
    }

    @CallSuper
    protected void onWalletSyncComplete() {
        invalidateBalance();
    }

    private void toggleDefaultCurrency() {
        defaultCurrencies = currencyPreference.toggleDefault();
        invalidateBalance();
    }

    private void invalidateBalance() {
        currentPrice = walletHelper.getLatestPrice();
        btcBalance = new BTCCurrency(walletHelper.getBalance());
        invalidatePrimary();
        invalidateSecondary();
        invalidateSymbol();
    }

    private void invalidatePrimary() {
        String value = "";

        if (defaultCurrencies.getPrimaryCurrency().isCrypto()) {
            value = getHoldingsOfCrypto();
        } else {
            value = getHoldingsOfFiat();
        }

        primaryBalance.setText(value);
    }

    private void invalidateSecondary() {
        String value = "";

        if (defaultCurrencies.getPrimaryCurrency().isCrypto()) {
            value = getHoldingsOfFiat();
        } else {
            value = getHoldingsOfCrypto();
        }

        secondaryBalance.setText(value);
    }

    private void invalidateSymbol() {
        if (defaultCurrencies.getPrimaryCurrency().isCrypto()) {
            Drawable drawable = getDrawableFor((CryptoCurrency) defaultCurrencies.getPrimaryCurrency());
            drawable.setBounds(0, 0,
                    (int) scaleValue(this, TypedValue.COMPLEX_UNIT_DIP, 20F),
                    (int) scaleValue(this, TypedValue.COMPLEX_UNIT_DIP, 21F));
            primaryBalance.setCompoundDrawables(drawable, null, null, null);
            secondaryBalance.setCompoundDrawables(null, null, null, null);
        } else {
            Drawable drawable = getDrawableFor((CryptoCurrency) defaultCurrencies.getSecondaryCurrency());
            drawable.setBounds(0, 0,
                    (int) (scaleValue(this, TypedValue.COMPLEX_UNIT_DIP, 20F) * .8),
                    (int) (scaleValue(this, TypedValue.COMPLEX_UNIT_DIP, 21F) * .8));
            secondaryBalance.setCompoundDrawables(drawable, null, null, null);
            primaryBalance.setCompoundDrawables(null, null, null, null);
        }
    }

    private Drawable getDrawableFor(CryptoCurrency currency) {
        return currency.getSymbolDrawable(this);
    }

    private String getHoldingsOfCrypto() {
        btcBalance = new BTCCurrency(walletHelper.getBalance());
        btcBalance.setCurrencyFormat(BTCCurrency.NO_SYMBOL_FORMAT);
        return btcBalance.toFormattedCurrency();
    }

    private String getHoldingsOfFiat() {
        return btcBalance.toUSD(currentPrice).toFormattedCurrency();
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BTC_PRICE_UPDATE.equals(intent.getAction())) {
                onPriceReceived(new USDCurrency(intent.getLongExtra(EXTRA_BITCOIN_PRICE, 0L)));
            } else if (ACTION_WALLET_SYNC_COMPLETE.equals(intent.getAction())) {
                onWalletSyncComplete();
            }
        }

    }
}
