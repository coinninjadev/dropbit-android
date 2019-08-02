package com.coinninja.coinkeeper.cn.service;

import com.coinninja.coinkeeper.cn.service.exception.base.CNServiceException;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.util.CNLogger;

import javax.inject.Inject;

import retrofit2.Response;

public class UserVerificationServiceCheck extends AbstractAuthorizationServiceCheck implements CoinNinjaServiceCheck {

    static final String DEVICE_UUID_MISMATCH = "device_uuid mismatch";
    private final SignedCoinKeeperApiClient apiClient;
    private final CNWalletManager cnWalletManager;
    private final CNLogger logger;
    private DeverifiedCause deverifiedCause = null;
    private String rawReason;

    @Inject
    public UserVerificationServiceCheck(SignedCoinKeeperApiClient apiClient,
                                        CNWalletManager cnWalletManager,
                                        CNLogger logger) {
        this.apiClient = apiClient;
        this.cnWalletManager = cnWalletManager;
        this.logger = logger;
    }

    @Override
    public boolean isVerified() throws CNServiceException {
        Response response = apiClient.verifyAccount();
        boolean isVerified = handleResponse(response);
        deverifiedCause = isVerified ? null : interpretDeverifiedCause(response);
        return isVerified;
    }

    @Override
    public void performDeverification() {
        cnWalletManager.deVerifyAccount();
    }

    @Override
    public DeverifiedCause deverificaitonReason() {
        return deverifiedCause;
    }

    @Override
    public CNLogger getLogger() {
        return logger;
    }

    @Override
    String getTag() {
        return getClass().getName();
    }

    private DeverifiedCause interpretDeverifiedCause(Response response) {
        try {
            if (getRaw().contains(DEVICE_UUID_MISMATCH)) {
                return DeverifiedCause.MISMATCH;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return DeverifiedCause.DROPPED;
    }

    @Override
    void setRaw(String rawReason) {
        this.rawReason = rawReason;
    }

    @Override
    public String getRaw() {
        return rawReason;
    }
}
