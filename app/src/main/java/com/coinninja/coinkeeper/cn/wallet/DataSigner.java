package com.coinninja.coinkeeper.cn.wallet;

import javax.inject.Inject;

public class DataSigner {

    private final LibBitcoinProvider libBitcoinProvider;

    @Inject
    public DataSigner(LibBitcoinProvider libBitcoinProvider) {
        this.libBitcoinProvider = libBitcoinProvider;
    }

    public String sign(String dataToSign) {
        return libBitcoinProvider.provide().sign(dataToSign);
    }

    public String getCoinNinjaVerificationKey() {
        return libBitcoinProvider.provide().getCoinNinjaVerificationKey();
    }
}
