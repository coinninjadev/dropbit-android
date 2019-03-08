package com.coinninja.coinkeeper.view.activity.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.blockchain.BlockChainService;
import com.coinninja.coinkeeper.service.client.model.TransactionFee;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;

import javax.inject.Inject;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

import static com.coinninja.coinkeeper.util.Intents.ACTION_BTC_PRICE_UPDATE;
import static com.coinninja.coinkeeper.util.Intents.ACTION_TRANSACTION_FEE_UPDATE;
import static com.coinninja.coinkeeper.util.Intents.ACTION_WALLET_SYNC_COMPLETE;
import static com.coinninja.coinkeeper.util.Intents.EXTRA_BITCOIN_PRICE;
import static com.coinninja.coinkeeper.util.Intents.EXTRA_TRANSACTION_FEE;

public abstract class BalanceBarActivity extends SecuredActivity implements ServiceConnection {
    private USDCurrency currentPrice;
    private BTCCurrency btcBalance;
    private TransactionFee transactionFee;

    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    BlockChainService.BlockChainBinder serviceBinder;
    BroadcastReceiver receiver;
    IntentFilter filter = new IntentFilter(ACTION_WALLET_SYNC_COMPLETE);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        receiver = new Receiver();
        bindService(new Intent(this, BlockChainService.class), this, Context.BIND_AUTO_CREATE);
        WalletHelper walletHelper = ((CoinKeeperApplication) getApplication()).getUser().getWalletHelper();
        transactionFee = walletHelper.getLatestFee();
        currentPrice = walletHelper.getLatestPrice();
        if (currentPrice == null) currentPrice = new USDCurrency();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        findViewById(R.id.balance).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPrimaryBalance();
        filter.addAction(ACTION_BTC_PRICE_UPDATE);
        filter.addAction(ACTION_TRANSACTION_FEE_UPDATE);
        localBroadCastUtil.registerReceiver(receiver, filter);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // when we install the application the first time, it needs to get the price. Waiting 30 seconds is too slow.
        invalidateBalance();
        onPriceReceived(currentPrice);
        setupHistoryButtons();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        currentPrice = price;
        updateCashValue();
    }

    protected void fetchBTCPrice() {
        if (serviceBinder != null && serviceBinder.isBinderAlive())
            serviceBinder.getService().fetchCurrentState();
    }

    protected void showHistoryDetail(View view) {
        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        navigateTo(intent);
    }

    @CallSuper
    protected void onWalletSyncComplete() {
        invalidateBalance();
    }

    @CallSuper
    protected void setupHistoryButtons() {
        findViewById(R.id.balance).setOnClickListener(this::showHistoryDetail);
    }

    protected void updateCashValue() {
        ((TextView) findViewById(R.id.alt_balance)).setText(btcBalance.toUSD(currentPrice).toFormattedCurrency());
    }

    private void invalidateBalance() {
        setPrimaryBalance();
        updateCashValue();
    }

    private void setPrimaryBalance() {
        Wallet primaryWallet = ((CoinKeeperApplication) getApplication()).getUser().getPrimaryWallet();
        primaryWallet.refresh();
        Long balance = primaryWallet.getBalance();
        btcBalance = new BTCCurrency(balance);
        btcBalance.setCurrencyFormat(BTCCurrency.ALT_CURRENCY_FORMAT);

        String formattedCurrency = btcBalance.toFormattedCurrency();
        TextView view = (TextView) findViewById(R.id.primary_balance);
        view.setText(formattedCurrency);
        updateCashValue();
    }

    @CallSuper
    protected void onTransactionFeeUpdated(TransactionFee transactionFee) {
        this.transactionFee = transactionFee;
        Log.d("onTransactionFeeUpdated", "MinFee = " + transactionFee.getMin());
        Log.d("onTransactionFeeUpdated", "AvgFee = " + transactionFee.getAvg());
        Log.d("onTransactionFeeUpdated", "MaxFee = " + transactionFee.getMax());
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BTC_PRICE_UPDATE.equals(intent.getAction())) {
                onPriceReceived(new USDCurrency(intent.getLongExtra(EXTRA_BITCOIN_PRICE, 0L)));
            } else if (ACTION_WALLET_SYNC_COMPLETE.equals(intent.getAction())) {
                onWalletSyncComplete();
            } else if (ACTION_TRANSACTION_FEE_UPDATE.equals(intent.getAction())) {
                onTransactionFeeUpdated(intent.getParcelableExtra(EXTRA_TRANSACTION_FEE));
            }
        }

    }

    public TransactionFee getTransactionFee() {
        return transactionFee;
    }
}
