package com.coinninja.coinkeeper.cn.account;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class RemoteAddressCache {
    private static final int NUM_ADDRESSES_TO_CACHE = 5;
    private final SignedCoinKeeperApiClient apiClient;
    private final AccountManager accountManager;

    @Inject
    RemoteAddressCache(SignedCoinKeeperApiClient apiClient, AccountManager accountManager) {
        this.apiClient = apiClient;
        this.accountManager = accountManager;
    }

    public void cacheAddresses() {
        Response response = apiClient.getCNWalletAddresses();

        if (response.isSuccessful()) {
            List<CNWalletAddress> cachedAddresses = (List<CNWalletAddress>) response.body();

            HashMap<String, String> addressToPubKey = accountManager
                    .unusedAddressesToPubKey(HDWallet.EXTERNAL, NUM_ADDRESSES_TO_CACHE);

            removeUsedAddresses(cachedAddresses, addressToPubKey);
            removeServerAddressesWithoutPublicKey(cachedAddresses);
            sendAddresses(cachedAddresses, addressToPubKey);
        }

    }

    private void removeServerAddressesWithoutPublicKey(List<CNWalletAddress> cachedAddresses) {
        for (CNWalletAddress address : cachedAddresses) {
            if (address.getPublicKey() == null || address.getPublicKey().isEmpty()) {
                apiClient.removeAddress(address.getAddress());
            }
        }
    }

    private void removeUsedAddresses(List<CNWalletAddress> cachedAddresses, HashMap<String, String> idToUnusedAddress) {
        for (CNWalletAddress walletAddress : cachedAddresses) {
            if (idToUnusedAddress.get(walletAddress.getAddress()) == null)
                apiClient.removeAddress(walletAddress.getAddress());
        }
    }

    private void sendAddresses(List<CNWalletAddress> cachedAddresses, HashMap<String, String> addressToPubKey) {
        HashMap<String, String> addressToPubKeyToSend = new HashMap(addressToPubKey);

        for (CNWalletAddress walletAddress : cachedAddresses) {
            if (addressToPubKey.get(walletAddress.getAddress()) != null)
                addressToPubKeyToSend.remove(walletAddress.getAddress());
        }

        for (String address : addressToPubKeyToSend.keySet()) {
            apiClient.addAddress(address, addressToPubKeyToSend.get(address));
        }
    }

}
