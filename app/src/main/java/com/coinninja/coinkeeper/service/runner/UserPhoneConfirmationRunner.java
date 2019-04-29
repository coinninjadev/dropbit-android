package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.service.client.CNUserAccount;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import retrofit2.Response;

public class UserPhoneConfirmationRunner implements Runnable {

    public static final String TAG = UserPhoneConfirmationRunner.class.getName();
    static final String EXPIRED = "expired";
    static final String VERIFIED = "verified";
    private static final String API_CONFIRM_ACCOUNT_FAILED = "----------API confirmAccount failed";
    private final RemoteAddressCache remoteAddressCache;
    private final Analytics analytics;
    private final CNLogger logger;
    private SignedCoinKeeperApiClient apiClient;
    private LocalBroadCastUtil localBroadCastUtil;
    private Account userAccount;
    private String code;

    @Inject
    public UserPhoneConfirmationRunner(SignedCoinKeeperApiClient apiClient, Account userAccount,
                                       LocalBroadCastUtil localBroadCastUtil, RemoteAddressCache remoteAddressCache,
                                       Analytics analytics, CNLogger logger) {
        this.userAccount = userAccount;
        this.apiClient = apiClient;
        this.localBroadCastUtil = localBroadCastUtil;
        this.remoteAddressCache = remoteAddressCache;
        this.analytics = analytics;
        this.logger = logger;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void run() {
        if (userAccount.getStatus() != Account.Status.PENDING_VERIFICATION) return;

        Response response = apiClient.verifyPhoneCode(code);
        switch (response.code()) {
            case 200:
                updateAccount(response);
                remoteAddressCache.cacheAddresses();
                analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, true);
                analytics.flush();
                break;
            case 400:
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE);
                updateAccount(response);
                logger.logError(TAG, API_CONFIRM_ACCOUNT_FAILED, response);
                break;
            case 409:
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
                updateAccount(response);
                logger.logError(TAG, API_CONFIRM_ACCOUNT_FAILED, response);
                break;
            default:
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
                logger.logError(TAG, API_CONFIRM_ACCOUNT_FAILED, response);

        }
    }

    private void updateAccount(Response response) {
        CNUserAccount account = (CNUserAccount) response.body();
        if (account != null && VERIFIED.equals(account.getStatus())) {
            userAccount.setStatus(Account.Status.VERIFIED);
            userAccount.update();
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS);
        } else if (account != null && EXPIRED.equals(account.getStatus())) {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
            logger.debug(TAG, "----------API confirmAccount failed");
            logger.debug(TAG, "---------------    Code EXPIRED: ");
        }
    }
}