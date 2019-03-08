package com.coinninja.coinkeeper.cn.wallet.service;

import android.os.Binder;

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;

public class CNWalletBinder extends Binder {
    private final CNWalletServicesInterface cnWalletService;

    public CNWalletBinder(CNWalletServicesInterface cnWalletService) {
        this.cnWalletService = cnWalletService;
    }

    public CNWalletServicesInterface getService() {
        return cnWalletService;
    }
}
