package com.coinninja.coinkeeper.cn.wallet.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;
import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner;
import com.coinninja.coinkeeper.di.component.AppComponent;
import com.coinninja.coinkeeper.di.component.CoinKeeperComponent;
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class CNWalletService extends Service implements CNWalletServicesInterface {
    static final int SYNC_MESSAGE_ID = 25;
    static final int SAVE_WORDS_ID = 35;

    CNWalletBinder cnWalletBinder;

    @Inject
    @ThreadHandler
    Handler workHandler;

    @Inject
    CoinKeeperComponent appComponent;


    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
        cnWalletBinder = new CNWalletBinder(this);
    }

    @Override
    public void saveSeedWords(String[] seedWords) {
        if (workHandler.hasMessages(SAVE_WORDS_ID)) return;

        SaveRecoveryWordsRunner saveRecoveryWordsRunner = appComponent.getSaveRecoveryWordsRunner();
        saveRecoveryWordsRunner.setWords(seedWords);
        Message message = Message.obtain(workHandler, saveRecoveryWordsRunner);
        message.what = SAVE_WORDS_ID;
        workHandler.sendMessage(message);
    }

    @Override
    public void performSync() {
        if (workHandler.hasMessages(SYNC_MESSAGE_ID)) return;

        Message message = Message.obtain(workHandler, appComponent.getFullSyncRunner());
        message.what = SYNC_MESSAGE_ID;
        workHandler.sendMessage(message);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return cnWalletBinder;
    }

    @Override
    public void onDestroy() {
        workHandler.getLooper().quitSafely();
    }
}
