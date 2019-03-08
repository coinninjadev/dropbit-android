package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;

import javax.inject.Inject;

import retrofit2.Response;

public class WalletVerificationServiceCheck extends AbstractAuthorizationServiceCheck implements CoinNinjaServiceCheck {

    private final SignedCoinKeeperApiClient apiClient;
    private final WalletHelper walletHelper;
    private final CNLogger logger;
    private DeverifiedCause deverifiedCause = null;
    private String rawMessage;

    @Inject
    public WalletVerificationServiceCheck(SignedCoinKeeperApiClient apiClient, WalletHelper walletHelper,
                                          CNLogger logger) {
        this.apiClient = apiClient;
        this.walletHelper = walletHelper;
        this.logger = logger;
    }

    @Override
    public boolean isVerified() throws CNServiceException {
        Response response = apiClient.verifyWallet();
        boolean isVerified = handleResponse(response);
        deverifiedCause = isVerified ? null : DeverifiedCause.DROPPED;
        return isVerified;
    }

    @Override
    public DeverifiedCause deverificaitonReason() {
        return deverifiedCause;
    }

    @Override
    public void performDeverification() {
        walletHelper.removeCurrentCnRegistration();
    }

    @Override
    public CNLogger getLogger() {
        return logger;
    }

    @Override
    String getTag() {
        return getClass().getName();
    }

    @Override
    void setRaw(String rawMessage) {
        this.rawMessage = rawMessage;
    }

    @Override
    public String getRaw() {
        return rawMessage;
    }
}
