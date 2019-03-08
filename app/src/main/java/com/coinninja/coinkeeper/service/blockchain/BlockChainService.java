package com.coinninja.coinkeeper.service.blockchain;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.coinninja.coinkeeper.di.interfaces.ThreadHandler;
import com.coinninja.coinkeeper.service.runner.CurrentBTCStateRunner;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class BlockChainService extends Service {

    BlockChainService.BlockChainBinder binder;

    @Inject
    CurrentBTCStateRunner currentBTCStateRunner;

    @Inject
    @ThreadHandler
    Handler threadHandler;


    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        binder = new BlockChainBinder();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void fetchCurrentState() {
        threadHandler.post(currentBTCStateRunner);
    }

    public class BlockChainBinder extends Binder {

        public BlockChainService getService() {
            return BlockChainService.this;
        }
    }
}
