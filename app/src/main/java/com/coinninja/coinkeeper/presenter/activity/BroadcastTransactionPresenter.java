package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.bindings.TransactionData;
import com.coinninja.coinkeeper.service.runner.BroadcastTransactionRunner;

import javax.inject.Inject;

public class BroadcastTransactionPresenter implements BroadcastTransactionRunner.BroadcastListener {

    private BroadcastTransactionRunner broadcastRunner;

    private View view;

    @Inject
    public BroadcastTransactionPresenter(BroadcastTransactionRunner broadcastRunner) {
        this.broadcastRunner = broadcastRunner;
    }

    public void broadcastTransaction(TransactionData transactionData) {
        broadcastRunner.setBroadcastListener(this);
        broadcastRunner.clone().execute(transactionData);
    }

    @Override
    public void onBroadcastSuccessful(TransactionBroadcastResult transactionBroadcastResult) {

        view.showBroadcastSuccessful(transactionBroadcastResult);
    }

    @Override
    public void onBroadcastProgress(int progress) {
        view.showProgress(progress);
    }

    @Override
    public void onBroadcastError(TransactionBroadcastResult transactionBroadcastResult) {
        view.showBroadcastFail(transactionBroadcastResult);
    }

    public void attachView(View view) {
        this.view = view;
    }

    public interface View {
        void showBroadcastFail(TransactionBroadcastResult transactionBroadcastResult);

        void showBroadcastSuccessful(TransactionBroadcastResult transactionBroadcastResult);

        void showProgress(int progress);
    }
}
