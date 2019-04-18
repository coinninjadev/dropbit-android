package com.coinninja.coinkeeper.ui.transaction.history;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.TargetStatHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.payment.PaymentBarFragment;
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity;
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity;
import com.coinninja.coinkeeper.ui.transaction.DefaultCurrencyChangeViewNotifier;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.ui.util.OnViewClickListener;
import com.coinninja.coinkeeper.util.CurrencyPreference;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.TrainingActivity;
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;
import com.coinninja.coinkeeper.view.widget.TransactionEmptyStateView;

import org.greenrobot.greendao.query.LazyList;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionHistoryActivity extends BalanceBarActivity implements TransactionHistoryDataAdapter.OnItemClickListener {

    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    BitcoinUtil bitcoinUtil;
    @Inject
    WalletHelper walletHelper;
    @Inject
    TransactionHistoryDataAdapter adapter;
    @Inject
    DefaultCurrencyChangeViewNotifier defaultCurrencyChangeViewNotifier;
    @Inject
    CurrencyPreference currencyPreference;

    PaymentBarFragment fragment;
    IntentFilter intentFilter = new IntentFilter(Intents.ACTION_TRANSACTION_DATA_CHANGED);
    private RecyclerView transactionHistory;
    private TransactionEmptyStateView transactionEmptyStateView;
    private LazyList<TransactionsInvitesSummary> transactions;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intents.ACTION_TRANSACTION_DATA_CHANGED.equals(intent.getAction())) {
                refreshTransactions();
            } else if (Intents.ACTION_CURRENCY_PREFERENCE_CHANGED.equals(intent.getAction())
                    && intent.hasExtra(Intents.EXTRA_PREFERENCE)) {
                defaultCurrencyChangeViewNotifier.onDefaultCurrencyChanged(intent.getParcelableExtra(Intents.EXTRA_PREFERENCE));
            }
        }
    };
    private String bitcoinUriString;

    @Override
    public void onItemClick(View view, int position) {
        if (transactions.isClosed()) return;
        Intent intent = new Intent(this, TransactionDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intents.EXTRA_TRANSACTION_RECORD_ID, transactions.get(position).getId());
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        intentFilter = new IntentFilter(Intents.ACTION_TRANSACTION_DATA_CHANGED);
        intentFilter.addAction(Intents.ACTION_CURRENCY_PREFERENCE_CHANGED);
        fragment = (PaymentBarFragment) getFragmentManager().findFragmentById(R.id.payment_bar_fragment);
        transactionEmptyStateView = findViewById(R.id.empty_state_view);
        clearTitle();
        if (getIntent() != null && getIntent().getData() != null) {
            bitcoinUriString = getIntent().getData().toString();
            launchPayScreenWithBitcoinUriIfNecessary();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        transactions = walletHelper.getTransactionsLazily();
        adapter.setOnItemClickListener(this);
        adapter.setTransactions(transactions);
        adapter.setDefaultCurrencyChangeViewNotifier(defaultCurrencyChangeViewNotifier);
        setupHistoryList();
        setupNewWalletButtons();
    }

    private void setupNewWalletButtons() {
        transactionEmptyStateView.setGetBitcoinButtonClickListener((view) -> {
            analytics.trackEvent(Analytics.EVENT_GET_BITCOIN);
            Intent intent = new Intent(this, BuyBitcoinActivity.class);
            startActivity(intent);
        });

        transactionEmptyStateView.setLearnBitcoinButtonClickListener((view) -> {
            analytics.trackEvent(Analytics.EVENT_LEARN_BITCOIN);
            Intent intent = new Intent(this, TrainingActivity.class);
            startActivity(intent);
        });

        transactionEmptyStateView.setSpendBitcoinButtonClickListener((view) -> {
            analytics.trackEvent(Analytics.EVENT_SPEND_BITCOIN);
            Intent intent = new Intent(this, SpendBitcoinActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.setOnItemClickListener(null);
        localBroadCastUtil.unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presentTransactions();
        showDetailWithInitialIntent();
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPriceReceived(USDCurrency price) {
        super.onPriceReceived(price);
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onWalletSyncComplete() {
        super.onWalletSyncComplete();
        refreshTransactions();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        adapter.setDefaultCurrencies(currencyPreference.getCurrenciesPreference());
    }

    @Override
    protected void onStop() {
        super.onStop();
        transactions.close();
    }

    private void launchPayScreenWithBitcoinUriIfNecessary() {
        if (bitcoinUriString == null) {
            return;
        }

        try {
            BitcoinUri bitcoinUri = bitcoinUtil.parse(bitcoinUriString);
            fragment.showPayDialogWithBitcoinUri(bitcoinUri);
        } catch (UriException e) {
            AlertDialogBuilder.build(getBaseContext(), "Invalid bitcoin request scanned. Please try again").show();
        }
    }

    private void refreshTransactions() {
        transactions.close();
        transactions = walletHelper.getTransactionsLazily();
        adapter.setTransactions(transactions);
        presentTransactions();
    }

    private void presentTransactions() {
        transactionEmptyStateView.setupUIForWallet(walletHelper);
    }

    private void showDetailWithInitialIntent() {
        if (!getIntent().hasExtra(Intents.EXTRA_TRANSACTION_ID)) return;

        Intent intent = new Intent(this, TransactionDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intents.EXTRA_TRANSACTION_ID, getIntent().getStringExtra(Intents.EXTRA_TRANSACTION_ID));
        startActivity(intent);
        getIntent().removeExtra(Intents.EXTRA_TRANSACTION_ID);
    }

    private void setupHistoryList() {
        transactionHistory = findViewById(R.id.transaction_history);
        transactionHistory.setVisibility(View.VISIBLE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        transactionHistory.setLayoutManager(layoutManager);
        transactionHistory.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        transactionHistory.setHasFixedSize(false);
        transactionHistory.setAdapter(adapter);
    }
}
