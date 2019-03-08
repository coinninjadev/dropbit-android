package com.coinninja.coinkeeper.cn.account;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.di.interfaces.NumAddressesToCache;
import com.coinninja.coinkeeper.model.helpers.AddressHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;

import javax.inject.Inject;

public class AddressCache {

    private final HDWallet hdWallet;
    private final AddressHelper addressHelper;
    private final WalletHelper walletHelper;
    private final int numAddressesToCache;

    @Inject
    AddressCache(HDWallet hdWallet, AddressHelper addressHelper, WalletHelper walletHelper, @NumAddressesToCache int numAddressesToCache) {
        this.hdWallet = hdWallet;
        this.addressHelper = addressHelper;
        this.walletHelper = walletHelper;
        this.numAddressesToCache = numAddressesToCache;
    }

    public String getUncompressedPublicKey(DerivationPath path) {
        return hdWallet.getUncompressedPublicKey(path);
    }

    /**
     * @param chainIndex EXTERNAL=0, INTERNAL=1
     */
    void cacheAddressesFor(int chainIndex) {
        if (!shouldCacheAddressesForChain(chainIndex)) return;
        String[] addresses = hdWallet.fillBlock(chainIndex, 0, largestAddressIndexReportedFor(chainIndex) + numAddressesToCache);

        for (int i = 0; i < addresses.length; i++) {
            addressHelper.saveAddress(chainIndex, i, addresses[i]);
        }
    }

    private int calcNumAddressesToHaveCached(int chainIndex) {
        return largestAddressIndexReportedFor(chainIndex) + numAddressesToCache;
    }

    /**
     * @param chainIndex EXTERNAL=0, INTERNAL=1
     */
    private int largestAddressIndexReportedFor(int chainIndex) {
        switch (chainIndex) {
            case HDWallet.EXTERNAL:
                return walletHelper.getCurrentExternalIndex();
            case HDWallet.INTERNAL:
                return walletHelper.getCurrentInternalIndex();
        }

        throw new IllegalArgumentException("Expected 0 or 1");
    }

    private boolean shouldCacheAddressesForChain(int chainIndex) {
        int numAddressesToHaveCached = calcNumAddressesToHaveCached(chainIndex);
        return addressHelper.getAddressCountFor(chainIndex) < numAddressesToHaveCached;
    }
}
