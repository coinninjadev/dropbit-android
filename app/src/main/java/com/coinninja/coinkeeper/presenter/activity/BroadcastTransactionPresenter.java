package com.coinninja.coinkeeper.presenter.activity;

import com.coinninja.coinkeeper.bitcoin.BroadcastListener;
import com.coinninja.coinkeeper.bitcoin.BroadcastResult;
import com.coinninja.coinkeeper.service.runner.BroadcastTransactionRunner;

import javax.inject.Inject;

import app.coinninja.cn.libbitcoin.model.TransactionData;

public class BroadcastTransactionPresenter implements BroadcastListener {

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
    public void onBroadcastSuccessful(BroadcastResult broadcastResult) {

        view.showBroadcastSuccessful(broadcastResult);
    }

    @Override
    public void onBroadcastProgress(int progress) {
        view.showProgress(progress);
    }

    @Override
    public void onBroadcastError(BroadcastResult broadcastResult) {
        view.showBroadcastFail(broadcastResult);
    }

    public void attachView(View view) {
        this.view = view;
    }

    public interface View {
        void showBroadcastFail(BroadcastResult broadcastResult);

        void showBroadcastSuccessful(BroadcastResult broadcastResult);

        void showProgress(int progress);
    }
}
