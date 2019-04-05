package com.coinninja.coinkeeper.view.activity.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.DefaultCurrencies;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.CryptoCurrency;
import com.coinninja.coinkeeper.util.currency.FiatCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.widget.DefaultCurrencyDisplayView;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

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
    private DefaultCurrencies defaultCurrencies;
    private DefaultCurrencyDisplayView balance;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        findViewById(R.id.balance).setVisibility(View.VISIBLE);
    }

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
    protected void onStart() {
        super.onStart();
        balance = withId(this, R.id.balance);
        defaultCurrencies = currencyPreference.getCurrenciesPreference();
        balance.setOnClickListener(v -> toggleDefaultCurrency());
    }

    @Override
    protected void onPause() {
        super.onPause();
        localBroadCastUtil.unregisterReceiver(receiver);
        unregisterReceiver(receiver);
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
    protected void onResume() {
        super.onResume();
        localBroadCastUtil.registerReceiver(receiver, filter);
        registerReceiver(receiver, filter);
        invalidateBalance();
    }

    @Override
    protected void onDestroy() {
        if (null != serviceBinder) {
            unbindService(this);
        }
        super.onDestroy();
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
        balance.renderValues(defaultCurrencies, getHoldingsOfCrypto(), getHoldingsOfFiat());
    }

    private CryptoCurrency getHoldingsOfCrypto() {
        return new BTCCurrency(walletHelper.getBalance());
    }

    private FiatCurrency getHoldingsOfFiat() {
        return getHoldingsOfCrypto().toFiat(walletHelper.getLatestPrice());
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
