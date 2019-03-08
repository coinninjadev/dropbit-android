package com.coinninja.coinkeeper.cn.wallet.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import dagger.android.AndroidInjection;
import retrofit2.Response;

public class CNWalletAddressRequestService extends IntentService {
    public static final String FETCHING_CONTACTS_RECEIVE_ADDRESS_FAILED = "|---- Fetching contact's receive address failed";

    @Inject
    CNLogger logger;

    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Inject
    SignedCoinKeeperApiClient apiClient;

    public CNWalletAddressRequestService() {
        super(CNWalletAddressRequestService.class.getSimpleName());
    }

    public CNWalletAddressRequestService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null || !intent.hasExtra(Intents.EXTRA_PHONE_NUMBER_HASH)) return;

        String phoneNumberHash = intent.getStringExtra(Intents.EXTRA_PHONE_NUMBER_HASH);
        AddressLookupResult addressLookupResult = lookupAddress(phoneNumberHash);

        intent = new Intent(Intents.ACTION_WALLET_ADDRESS_RETRIEVED);
        intent.putExtra(Intents.EXTRA_ADDRESS_LOOKUP_RESULT, addressLookupResult);
        localBroadCastUtil.sendBroadcast(intent);
    }

    private AddressLookupResult lookupAddress(String phoneHash) {
        AddressLookupResult addressLookupResult = new AddressLookupResult();
        Response response = apiClient.queryWalletAddress(phoneHash);

        if (response.isSuccessful()) {
            List<AddressLookupResult> results = (List<AddressLookupResult>) response.body();
            if (results.size() > 0) {
                addressLookupResult = results.get(0);
            }
        } else {
            logger.logError(getClass().getSimpleName(), FETCHING_CONTACTS_RECEIVE_ADDRESS_FAILED, response);
        }

        return addressLookupResult;

    }
}
