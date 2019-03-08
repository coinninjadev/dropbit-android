package com.coinninja.coinkeeper.ui.transaction.details;

import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction;

public interface TransactionDetailObserver {
    void onTransactionDetailsRequested(BindableTransaction transactionsInvitesSummary);
}
