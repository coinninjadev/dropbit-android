package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.dropbit.DropBitCancellationManager;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import javax.inject.Inject;


public class NegativeBalanceRunner implements Runnable {
    private DropBitCancellationManager cancelationService;
    private final WalletHelper walletHelper;

    @Inject
    public NegativeBalanceRunner(DropBitCancellationManager dropBitCancellationManager, WalletHelper walletHelper) {
        this.cancelationService = dropBitCancellationManager;
        this.walletHelper = walletHelper;
    }

    @Override
    public void run() {
        if (!(walletHelper.buildBalances(false) < 0)) return;

        cancelationService.markUnfulfilledAsCanceled();
    }
}
