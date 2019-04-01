package com.coinninja.coinkeeper.service.runner;

import android.app.Service;
import android.util.Log;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import java.io.IOException;

import retrofit2.Response;

public class RegisterPhoneNumberRunnable implements Runnable {
    public static final String TAG = RegisterPhoneNumberRunnable.class.getSimpleName();

    private final SignedCoinKeeperApiClient apiClient;
    private final LocalBroadCastUtil localBroadcast;
    private CNPhoneNumber CNPhoneNumber;
    private final CoinKeeperApplication application;
    private ResendPhoneVerificationRunner resendRunner;

    public RegisterPhoneNumberRunnable(Service service) {
        application = (CoinKeeperApplication) service.getApplication();
        apiClient = application.getSecuredClient();
        resendRunner = new ResendPhoneVerificationRunner(service);
        localBroadcast = ((CoinKeeperApplication) service.getApplication()).getLocalBroadCastUtil();
    }

    public void setCNPhoneNumber(CNPhoneNumber CNPhoneNumber) {
        this.CNPhoneNumber = CNPhoneNumber;
        resendRunner.setCNPhoneNumber(CNPhoneNumber);
    }

    public void setResendRunner(ResendPhoneVerificationRunner resendRunner) {

        this.resendRunner = resendRunner;
    }

    @Override
    public void run() {
        if (!hasAccount()) return;

        Response response = apiClient.registerUserAccount(CNPhoneNumber);
        if (response.code() == 201) {
            getWalletHelper().saveAccountRegistration((CNUserAccount) response.body(), CNPhoneNumber);
            localBroadcast.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CODE_SENT);
        } else if (response.code() == 200) {
            getWalletHelper().updateUserID((CNUserAccount) response.body());
            resendRunner.run();
        } else if (response.code() == 424) {
            localBroadcast.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR);
        } else {
            Log.d(TAG, "|---- create user account failed");
            Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
            localBroadcast.sendBroadcast(Intents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
            try {
                Log.d(TAG, "|--------- message: " + response.errorBody().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean hasAccount() {
        return getWalletHelper().hasAccount();
    }

    public WalletHelper getWalletHelper() {
        return application.getUser().getWalletHelper();
    }
}
