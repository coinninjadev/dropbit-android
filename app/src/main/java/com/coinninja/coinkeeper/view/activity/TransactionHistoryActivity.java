package com.coinninja.coinkeeper.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.transaction.details.TransactionDetailsActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity;
import com.coinninja.coinkeeper.view.adapter.TransactionHistoryDataAdapter;
import com.coinninja.coinkeeper.view.adapter.util.TransactionAdapterUtil;

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
    WalletHelper walletHelper;
    @Inject
    TransactionAdapterUtil transactionAdapterUtil;
    BroadcastReceiver receiver;
    private RecyclerView transactionHistory;
    private LazyList<TransactionsInvitesSummary> transactions;
    private TransactionHistoryDataAdapter adapter;
    private USDCurrency valueCurrency;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        receiver = new Receiver();
        valueCurrency = new USDCurrency();
        transactionHistory = findViewById(R.id.transaction_history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        transactionHistory.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, layoutManager.getOrientation());
        transactionHistory.addItemDecoration(dividerItemDecoration);
        transactionHistory.setHasFixedSize(true);
        adapter = new TransactionHistoryDataAdapter(transactions, this, transactionAdapterUtil);

    }

    @Override
    protected void onResume() {
        super.onResume();
        transactions = walletHelper.getTransactionsLazily();
        adapter = new TransactionHistoryDataAdapter(transactions, this, transactionAdapterUtil);
        adapter.setConversionCurrency(valueCurrency);
        transactionHistory.setAdapter(adapter);
        presentTransactions();
        showDetailWithInitialIntent();
        IntentFilter filter = new IntentFilter(Intents.ACTION_TRANSACTION_DATA_CHANGED);
        localBroadCastUtil.registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.setOnItemClickListener(null);
        localBroadCastUtil.unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        transactions.close();
    }

    @Override
    public void onPriceReceived(USDCurrency price) {
        super.onPriceReceived(price);
        valueCurrency = price;
        adapter.setConversionCurrency(valueCurrency);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (transactions.isClosed()) return;
        Intent intent = new Intent(this, TransactionDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intents.EXTRA_TRANSACTION_RECORD_ID, transactions.get(position).getId());
        startActivity(intent);
    }

    @Override
    protected void onWalletSyncComplete() {
        super.onWalletSyncComplete();
        refreshTransactions();
    }

    private void refreshTransactions() {
        transactions.close();
        transactions = walletHelper.getTransactionsLazily();
        ((TransactionHistoryDataAdapter) transactionHistory.getAdapter()).setTransactions(transactions);
        presentTransactions();
    }

    private void presentTransactions() {
        if (transactions.isEmpty()) {
            showEmpty();
        } else {
            showList();
        }
    }

    private void showList() {
        findViewById(R.id.empty_transaction_history).setVisibility(View.GONE);
        RecyclerView list = findViewById(R.id.transaction_history);
        list.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        findViewById(R.id.empty_transaction_history).setVisibility(View.VISIBLE);
        findViewById(R.id.transaction_history).setVisibility(View.GONE);
    }

    private void showDetailWithInitialIntent() {
        if (!getIntent().hasExtra(Intents.EXTRA_TRANSACTION_ID)) return;

        Intent intent = new Intent(this, TransactionDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(Intents.EXTRA_TRANSACTION_ID, getIntent().getStringExtra(Intents.EXTRA_TRANSACTION_ID));
        startActivity(intent);
        getIntent().removeExtra(Intents.EXTRA_TRANSACTION_ID);
    }

    class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intents.ACTION_TRANSACTION_DATA_CHANGED.equals(intent.getAction())) {
                refreshTransactions();
            }
        }
    }
}
