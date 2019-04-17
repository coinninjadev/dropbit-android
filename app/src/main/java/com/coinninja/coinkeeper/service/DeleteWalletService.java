package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.cn.service.PushNotificationDeviceManager;
import com.coinninja.coinkeeper.cn.service.PushNotificationEndpointManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;

public class DeleteWalletService extends IntentService {

    public static final String TAG = DeleteWalletService.class.getName();
    @Inject
    PushNotificationDeviceManager pushNotificationDeviceManager;
    @Inject
    PushNotificationEndpointManager pushNotificationEndpointManager;
    @Inject
    SignedCoinKeeperApiClient apiClient;
    @Inject
    DaoSessionManager daoSessionManager;
    @Inject
    Analytics analytics;
    @Inject
    UserHelper userHelper;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    CoinKeeperApplication application;
    @Inject
    SyncWalletManager syncWalletManager;

    public DeleteWalletService() {
        this(TAG);
    }

    public DeleteWalletService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        localBroadCastUtil = new LocalBroadCastUtil(this);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        syncWalletManager.cancelAllScheduledSync();
        if (pushNotificationEndpointManager.hasEndpoint()) {
            pushNotificationEndpointManager.unRegister();
            pushNotificationEndpointManager.removeEndpoint();
        }
        pushNotificationDeviceManager.removeCNDevice();
        apiClient.resetWallet();
        daoSessionManager.resetAll();
        userHelper.createFirstUser();
        resetAnalyticsProperties();
        localBroadCastUtil.sendBroadcast(Intents.ACTION_ON_WALLET_DELETED);
    }

    private void resetAnalyticsProperties() {
        analytics.trackEvent(Analytics.EVENT_WALLET_DELETE);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET, false);
        analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, false);
        analytics.setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false);
        analytics.flush();
    }

}
