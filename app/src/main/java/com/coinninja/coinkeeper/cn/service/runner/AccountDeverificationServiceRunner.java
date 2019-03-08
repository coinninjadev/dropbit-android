package com.coinninja.coinkeeper.cn.service.runner;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.service.CoinNinjaServiceCheck;
import com.coinninja.coinkeeper.cn.service.UserVerificationServiceCheck;
import com.coinninja.coinkeeper.cn.service.WalletVerificationServiceCheck;
import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class AccountDeverificationServiceRunner implements Runnable {
    static final String WALLET = "wallet";
    static final String USER = "user";

    private static final String TAG = AccountDeverificationServiceRunner.class.getName();
    private final WalletHelper walletHelper;
    private final UserVerificationServiceCheck userVerificationService;
    private final WalletVerificationServiceCheck walletVerificationService;
    private final NotificationUtil notificationUtil;
    private final Analytics analytics;
    String debugMessage = "Failed to get %s: %s";


    @Inject
    public AccountDeverificationServiceRunner(WalletHelper walletHelper, UserVerificationServiceCheck userVerificationService,
                                              WalletVerificationServiceCheck walletVerificationService,
                                              NotificationUtil notificationUtil, Analytics analytics) {
        this.walletHelper = walletHelper;
        this.userVerificationService = userVerificationService;
        this.walletVerificationService = walletVerificationService;
        this.notificationUtil = notificationUtil;
        this.analytics = analytics;
    }

    @Override
    public void run() {
        if (!walletHelper.hasAccount()) return;

        try {
            verifyAccount();
        } catch (CNServiceException ex) {
            ex.printStackTrace();
        }
    }

    private void verifyAccount() throws CNServiceException {
        if (walletHelper.hasVerifiedAccount()) {
            verifyUser();
        } else {
            verifyWallet();
        }
    }

    private void verifyUser() throws CNServiceException {
        if (userVerificationService.isVerified()) return;
        notifyAccountDeverification();
        userVerificationService.performDeverification();
        verifyWallet();
    }

    private void notifyAccountDeverification() {
        JSONObject json = getJSONToReport(String.format(debugMessage, USER, userVerificationService.getRaw()));
        if (userVerificationService.deverificaitonReason() == CoinNinjaServiceCheck.DeverifiedCause.MISMATCH) {
            notificationUtil.dispatchInternal(R.string.mismatch_401_user_deverifcation_message);
            analytics.trackEvent(Analytics.EVENT_PHONE_AUTO_DEVERIFIED, json);
        } else {
            notificationUtil.dispatchInternal(R.string.default_401_user_deverifcation_message);
            analytics.trackEvent(Analytics.EVENT_PHONE_AUTO_DEVERIFIED, json);
        }
    }

    private void verifyWallet() throws CNServiceException {
        if (walletVerificationService.isVerified()) return;

        String message = String.format(debugMessage, WALLET, walletVerificationService.getRaw());
        walletVerificationService.performDeverification();
        analytics.trackEvent(Analytics.EVENT_PHONE_AUTO_DEVERIFIED, getJSONToReport(message));
    }

    private JSONObject getJSONToReport(String message) {
        JSONObject json = new JSONObject();
        try {
            json.put("debugMessage", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ;
        return json;
    }

}
