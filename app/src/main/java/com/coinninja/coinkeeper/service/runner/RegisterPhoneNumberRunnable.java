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

public class RegisterPhoneNumberRunnable implements Runnable {
    public static final String TAG = RegisterPhoneNumberRunnable.class.getSimpleName();

    private CNPhoneNumber CNPhoneNumber;
    private final WalletHelper walletHelper;
    private final SignedCoinKeeperApiClient apiClient;
    private final LocalBroadCastUtil localBroadCastUtil;
    private ResendPhoneVerificationRunner resendPhoneVerificationRunner;

    @Inject
    RegisterPhoneNumberRunnable(WalletHelper walletHelper, SignedCoinKeeperApiClient apiClient,
                                LocalBroadCastUtil localBroadCastUtil, ResendPhoneVerificationRunner resendPhoneVerificationRunner) {
        this.walletHelper = walletHelper;
        this.apiClient = apiClient;
        this.localBroadCastUtil = localBroadCastUtil;
        this.resendPhoneVerificationRunner = resendPhoneVerificationRunner;
    }

    public void setCNPhoneNumber(CNPhoneNumber CNPhoneNumber) {
        this.CNPhoneNumber = CNPhoneNumber;
        resendPhoneVerificationRunner.setCNPhoneNumber(CNPhoneNumber);
    }

    @Override
    public void run() {
        if (!walletHelper.hasAccount()) return;

        Response response = apiClient.registerUserAccount(CNPhoneNumber);
        if (response.code() == 201) {
            walletHelper.saveAccountRegistration((CNUserAccount) response.body(), CNPhoneNumber);
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT);
        } else if (response.code() == 200) {
            walletHelper.updateUserID((CNUserAccount) response.body());
            resendPhoneVerificationRunner.run();
        } else if (response.code() == 424) {
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR);
        } else {
            Log.d(TAG, "|---- create user account failed");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            localBroadCastUtil.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
            try {
                Log.d(TAG, "|--------- message: " + response.errorBody().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
