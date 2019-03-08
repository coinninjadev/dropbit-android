package com.coinninja.coinkeeper.cn.wallet.interfaces;

public interface CNWalletServicesInterface {

    void saveSeedWords(String[] seedWords);

    void performSync();
}
