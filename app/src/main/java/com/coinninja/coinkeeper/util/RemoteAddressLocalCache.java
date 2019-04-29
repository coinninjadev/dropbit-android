package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.model.dto.AddressDTO;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class RemoteAddressLocalCache {
    public static final String LOCAL_ADDRESS_CACHE_KEY = "all-local-address-cache-key";
    private final PreferencesUtil preferencesUtil;

    @Inject
    RemoteAddressLocalCache(PreferencesUtil preferencesUtil) {
        this.preferencesUtil = preferencesUtil;

    }

    public List<AddressDTO> getLocalRemoteAddressCache() {
        String remoteAddressString = preferencesUtil.getString(LOCAL_ADDRESS_CACHE_KEY, "[]");
        AddressDTO[] addresses = new Gson().fromJson(remoteAddressString, AddressDTO[].class);
        return Arrays.asList(addresses);
    }

    public void setLocalRemoteAddressCache(List<AddressDTO> remoteAddressCache) {
        if (remoteAddressCache == null || remoteAddressCache.size() < 1) {
            preferencesUtil.removePreference(LOCAL_ADDRESS_CACHE_KEY);
            return;
        }
        String json = new Gson().toJson(remoteAddressCache);
        preferencesUtil.savePreference(LOCAL_ADDRESS_CACHE_KEY, json);
    }

}
