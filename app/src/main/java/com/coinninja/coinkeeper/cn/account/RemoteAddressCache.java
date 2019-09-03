package com.coinninja.coinkeeper.cn.account;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.CNWalletAddress;
import com.coinninja.coinkeeper.util.RemoteAddressLocalCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

@SuppressWarnings("ALL")
public class RemoteAddressCache {
    private static final int NUM_ADDRESSES_TO_CACHE = 5;
    private final SignedCoinKeeperApiClient apiClient;
    private final AccountManager accountManager;
    private final RemoteAddressLocalCache remoteAddressLocalCache;

    @Inject
    RemoteAddressCache(SignedCoinKeeperApiClient apiClient, AccountManager accountManager, RemoteAddressLocalCache remoteAddressLocalCache) {
        this.apiClient = apiClient;
        this.accountManager = accountManager;
        this.remoteAddressLocalCache = remoteAddressLocalCache;
    }

    public void cacheAddresses() {
        Response response = apiClient.getCnWalletAddresses();

        if (response.isSuccessful()) {
            List<CNWalletAddress> cachedAddresses = (List<CNWalletAddress>) response.body();

            HashMap<String, AddressDTO> addressStringToDTO = accountManager
                    .unusedAddressesToPubKey(HDWallet.EXTERNAL, NUM_ADDRESSES_TO_CACHE);

            removeUsedAddresses(cachedAddresses, addressStringToDTO);
            removeServerAddressesWithoutPublicKey(cachedAddresses);
            sendAddresses(cachedAddresses, addressStringToDTO);
        }

    }

    private void cacheUnusedAddressesLocally(List<AddressDTO> unusedAddresses) {
        remoteAddressLocalCache.setLocalRemoteAddressCache(unusedAddresses);
    }

    private void removeServerAddressesWithoutPublicKey(List<CNWalletAddress> cachedAddresses) {
        for (CNWalletAddress address : cachedAddresses) {
            if (address.getPublicKey() == null || address.getPublicKey().isEmpty()) {
                apiClient.removeAddress(address.getAddress());
            }
        }
    }

    private void removeUsedAddresses(List<CNWalletAddress> cachedAddresses, HashMap<String, AddressDTO> idToUnusedAddress) {
        for (CNWalletAddress walletAddress : cachedAddresses) {
            if (idToUnusedAddress.get(walletAddress.getAddress()) == null)
                apiClient.removeAddress(walletAddress.getAddress());
        }
    }

    private void sendAddresses(List<CNWalletAddress> cachedAddresses, HashMap<String, AddressDTO> addressDTOHashMap) {
        HashMap<String, AddressDTO> addressToDTOMapCopy = new HashMap(addressDTOHashMap);

        for (CNWalletAddress walletAddress : cachedAddresses) {
            if (addressDTOHashMap.get(walletAddress.getAddress()) != null) {
                addressToDTOMapCopy.remove(walletAddress.getAddress());
            }
        }

        for (String address : addressToDTOMapCopy.keySet()) {
            AddressDTO addressToSend = addressToDTOMapCopy.get(address);
            apiClient.addAddress(address, addressToSend.getUncompressedPublicKey());
        }

        cacheUnusedAddressesLocally(new ArrayList<>(addressDTOHashMap.values()));
    }

}
