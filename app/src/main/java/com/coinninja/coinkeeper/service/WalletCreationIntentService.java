package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class WalletCreationIntentService extends IntentService {

    @Inject
    CNWalletManager cnWalletManageer;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    public WalletCreationIntentService(){
        this(WalletCreationIntentService.class.getName());
    }

    public WalletCreationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        cnWalletManageer.skipBackup(cnWalletManageer.generateRecoveryWords());
    }
}
