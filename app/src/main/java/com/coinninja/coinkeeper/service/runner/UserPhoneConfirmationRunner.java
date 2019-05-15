package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.account.RemoteAddressCache;
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
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
    private final DropbitAccountHelper dropbitAccountHelper;
    private SignedCoinKeeperApiClient apiClient;
    private LocalBroadCastUtil localBroadCastUtil;
    private String code;

    @Inject
    public UserPhoneConfirmationRunner(SignedCoinKeeperApiClient apiClient, DropbitAccountHelper dropbitAccountHelper,
                                       LocalBroadCastUtil localBroadCastUtil, RemoteAddressCache remoteAddressCache,
                                       Analytics analytics, CNLogger logger) {
        this.apiClient = apiClient;
        this.dropbitAccountHelper = dropbitAccountHelper;
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
        CNUserAccount cnUserAccount = (CNUserAccount) response.body();
        if (cnUserAccount != null && VERIFIED.equals(cnUserAccount.getStatus())) {
            dropbitAccountHelper.updateVerifiedAccount(cnUserAccount);
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS);
            analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true);
        } else if (cnUserAccount != null && EXPIRED.equals(cnUserAccount.getStatus())) {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
            logger.debug(TAG, "----------API confirmAccount failed");
            logger.debug(TAG, "---------------    Code EXPIRED: ");
        }
    }
}