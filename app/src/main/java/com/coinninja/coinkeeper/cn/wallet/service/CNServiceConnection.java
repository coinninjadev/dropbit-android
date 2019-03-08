package com.coinninja.coinkeeper.cn.wallet.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

public class CNServiceConnection implements ServiceConnection {
    private CNWalletServicesInterface cnWalletServicesInterface;
    private LocalBroadCastUtil localBroadCastUtil;
    private boolean isBounded;

    @Inject
    public CNServiceConnection(LocalBroadCastUtil localBroadCastUtil) {
        this.localBroadCastUtil = localBroadCastUtil;
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        CNWalletBinder binder = (CNWalletBinder) service;
        cnWalletServicesInterface = binder.getService();
        isBounded = true;
        localBroadCastUtil.sendBroadcast(Intents.ACTION_ON_SERVICE_CONNECTION_BOUNDED);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        isBounded = false;
    }

    public CNWalletServicesInterface getCNWalletServicesInterface() {
        return cnWalletServicesInterface;
    }

    public void setBounded(boolean bounded) {
        isBounded = bounded;
    }

    public boolean isBounded() {
        return isBounded;
    }
}
