package com.coinninja.coinkeeper.service.runner;

import android.content.Intent;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

public class CnUserLocalDeverificationRunner implements Runnable {

    private final WalletHelper walletHelper;
    private final LocalBroadCastUtil broadcastUtil;

    public CnUserLocalDeverificationRunner(LocalBroadCastUtil broadcastUtil, WalletHelper walletHelper) {
        this.walletHelper = walletHelper;
        this.broadcastUtil = broadcastUtil;
    }

    @Override
    public void run() {
        walletHelper.removeCurrentCnRegistration();
        broadcastUtil.sendBroadcast(new Intent(Intents.ACTION_CN_USER_ACCOUNT_UPDATED));
    }
}
