package com.coinninja.coinkeeper.ui.transaction.details;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.base.BalanceBarActivity;
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import static com.coinninja.android.helpers.Views.withId;

public class TransactionDetailsActivity extends BalanceBarActivity {

    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Inject
    TransactionDetailPageAdapter pageAdapter;

    @Inject
    TransactionDetailDialogController transactionDetailDialogController;

    TransactionDetailObserver transactionDetailObserver = transaction -> onTransactionDetailRequested(transaction);

    ViewPager pager;

    IntentFilter intentFilter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intents.ACTION_WALLET_SYNC_COMPLETE.equals(action) ||
                    Intents.ACTION_TRANSACTION_DATA_CHANGED.equals(action)) {
                onTransactionDataChanged();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);
        pager = withId(this, R.id.pager_transaction_details);
        intentFilter = new IntentFilter(Intents.ACTION_TRANSACTION_DATA_CHANGED);
        intentFilter.addAction(Intents.ACTION_WALLET_SYNC_COMPLETE);
        pageAdapter.refreshData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setUpPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        pageAdapter.setShowTransactionDetailRequestObserver(null);
        pageAdapter.tearDown();
        localBroadCastUtil.unregisterReceiver(receiver);
        super.onStop();
    }

    void onTransactionDetailRequested(BindableTransaction transaction) {
        transactionDetailDialogController.showTransaction(this, transaction);
    }

    private void onTransactionDataChanged() {
        long recordId = pageAdapter.getTransactionIdForIndex(pager.getCurrentItem());
        refreshPageData();
        pager.setCurrentItem(pageAdapter.lookupTransactionById(recordId));
    }

    private void refreshPageData() {
        pager.setAdapter(pageAdapter);
        pageAdapter.refreshData();
    }

    private void setUpPager() {
        pager.setAdapter(pageAdapter);
        pager.setClipToPadding(false);
        int gap = (int) getResources().getDimension(R.dimen.horizontal_margin);
        pager.setPadding(gap, 0, gap, 0);
        pageAdapter.setShowTransactionDetailRequestObserver(transactionDetailObserver);
        pager.setPageMargin((int) getResources().getDimension(R.dimen.horizontal_margin_small));
        refreshPageData();

        if (getIntent().hasExtra(Intents.EXTRA_TRANSACTION_ID)) {
            int location = pageAdapter.lookupTransactionBy(getIntent().getStringExtra(Intents.EXTRA_TRANSACTION_ID));
            pager.setCurrentItem(location, true);
            getIntent().removeExtra(Intents.EXTRA_TRANSACTION_ID);
        }

        if (getIntent().hasExtra(Intents.EXTRA_TRANSACTION_RECORD_ID)) {
            int location = pageAdapter.lookupTransactionById(getIntent().getLongExtra(Intents.EXTRA_TRANSACTION_RECORD_ID, 0L));
            pager.setCurrentItem(location, true);
            getIntent().removeExtra(Intents.EXTRA_TRANSACTION_RECORD_ID);
        }
    }
}
