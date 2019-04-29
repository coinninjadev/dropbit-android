package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;
import retrofit2.Response;

public class DeverifyAccountService extends IntentService {
    public static final String TAG = DeverifyAccountService.class.getSimpleName();

    @Inject
    CNLogger logger;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    SignedCoinKeeperApiClient apiClient;
    @Inject
    UserHelper userHelper;
    @Inject
    CNWalletManager cnWalletManager;

    public DeverifyAccountService() {
        this(TAG);
    }

    public DeverifyAccountService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Response response = apiClient.resetWallet();

        if (response.isSuccessful()) {
            cnWalletManager.deverifyAccount();
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED);
        } else {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED);
            logger.logError(getClass().getSimpleName(), "|------------ Failed to deverify phone number", response);
        }

    }
}
