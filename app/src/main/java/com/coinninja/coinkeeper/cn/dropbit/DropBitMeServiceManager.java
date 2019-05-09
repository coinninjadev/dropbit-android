package com.coinninja.coinkeeper.cn.dropbit;

import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNUserPatch;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import retrofit2.Response;

public class DropBitMeServiceManager {
    private final String TAG = DropBitMeServiceManager.class.getName();

    private final DropbitAccountHelper dropbitAccountHelper;
    private final SignedCoinKeeperApiClient apiClient;
    private final LocalBroadCastUtil localBroadCastUtil;
    private final Analytics analytics;
    private final CNLogger cnLogger;

    @Inject
    public DropBitMeServiceManager(DropbitAccountHelper dropbitAccountHelper,
                                   SignedCoinKeeperApiClient apiClient,
                                   LocalBroadCastUtil localBroadCastUtil, Analytics analytics,
                                   CNLogger cnLogger) {
        this.dropbitAccountHelper = dropbitAccountHelper;
        this.apiClient = apiClient;
        this.localBroadCastUtil = localBroadCastUtil;
        this.analytics = analytics;
        this.cnLogger = cnLogger;
    }

    public void enableAccount() {
        Response response = apiClient.enableDropBitMeAccount();

        if (response.isSuccessful()) {
            updateUserAccount(response);
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED);
            analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true);
            analytics.trackEvent(Analytics.EVENT_DROPBIT_ME_ENABLED);
        } else {
            cnLogger.logError(TAG, "-- Failed to enable account", response);
        }
    }

    public void disableAccount() {
        Response response = apiClient.disableDropBitMeAccount();

        if (response.isSuccessful()) {
            updateUserAccount(response);
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED);
            analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false);
            analytics.trackEvent(Analytics.EVENT_DROPBIT_ME_DISABLED);
        } else {
            cnLogger.logError(TAG, "-- Failed to disable account", response);
        }
    }

    private void updateUserAccount(Response response) {
        CNUserPatch userPatch = (CNUserPatch) response.body();
        dropbitAccountHelper.updateUserAccount(userPatch);
    }
}
