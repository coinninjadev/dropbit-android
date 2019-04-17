package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Response;

public class ResendPhoneVerificationRunner implements Runnable {
    final static String TAG = ResendPhoneVerificationRunner.class.getName();
    private final WalletHelper walletHelper;
    private final SignedCoinKeeperApiClient apiClient;
    private final LocalBroadCastUtil localBroadCastUtil;
    private CNPhoneNumber CNPhoneNumber;


    @Inject
    ResendPhoneVerificationRunner(WalletHelper walletHelper, SignedCoinKeeperApiClient apiClient, LocalBroadCastUtil localBroadCastUtil) {
        this.walletHelper = walletHelper;
        this.apiClient = apiClient;
        this.localBroadCastUtil = localBroadCastUtil;
    }

    @Override
    public void run() {
        if (walletHelper.hasAccount()) {
            resendVerification();
        }
    }

    public void setCNPhoneNumber(CNPhoneNumber CNPhoneNumber) {
        this.CNPhoneNumber = CNPhoneNumber;
    }

    private void resendVerification() {
        Response response = apiClient.resendVerification(CNPhoneNumber);

        if (response.isSuccessful()) {
            walletHelper.saveAccountRegistration((CNUserAccount) response.body(), CNPhoneNumber);
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT);
        } else if (response.code() == 429) {
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR);
        } else if (response.code() == 424) {
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR);
        } else {
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
            logError(response);
        }
    }

    private void logError(Response response) {
        Log.d(TAG, "|---- create user account failed -- Resend Verification Route");
        Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
        try {
            Log.d(TAG, "|--------- message: " + response.errorBody().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
