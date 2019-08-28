package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.cn.wallet.DataSigner;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.receiver.WalletRegistrationCompleteReceiver;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWallet;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

import retrofit2.Response;

public class WalletRegistrationRunner implements Runnable {

    private static final String TAG = WalletRegistrationRunner.class.getName();
    private final DataSigner dataSigner;
    private final WalletHelper walletHelper;
    private final CNLogger logger;
    private LocalBroadCastUtil localBroadCastUtil;
    private SignedCoinKeeperApiClient apiClient;

    @Inject
    public WalletRegistrationRunner(LocalBroadCastUtil localBroadCastUtil, SignedCoinKeeperApiClient apiClient,
                                    DataSigner dataSigner, WalletHelper walletHelper, CNLogger logger) {
        this.localBroadCastUtil = localBroadCastUtil;
        this.apiClient = apiClient;
        this.dataSigner = dataSigner;
        this.walletHelper = walletHelper;
        this.logger = logger;
    }

    @Override
    public void run() {
        if (walletHelper.hasAccount()) {
            notifiyOfWalletRegistrationCompleted();
            return;
        }

        Response response = apiClient.registerWallet(dataSigner.getCoinNinjaVerificationKey());
        if (response.code() == 200 || response.code() == 201) {
            walletHelper.saveRegistration((CNWallet) response.body());
            notifiyOfWalletRegistrationCompleted();
        } else {
            logger.logError(TAG, "|---- create user failed", response);
        }
    }

    private void notifiyOfWalletRegistrationCompleted() {
        localBroadCastUtil.sendGlobalBroadcast(WalletRegistrationCompleteReceiver.class, DropbitIntents.ACTION_WALLET_REGISTRATION_COMPLETE);
    }
}
