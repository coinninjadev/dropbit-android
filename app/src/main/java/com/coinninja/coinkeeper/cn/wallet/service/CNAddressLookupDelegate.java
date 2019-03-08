package com.coinninja.coinkeeper.cn.wallet.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.AddressLookupResult;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.Hasher;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.ServiceWorkUtil;

import javax.inject.Inject;

public class CNAddressLookupDelegate extends BroadcastReceiver {

    private final ServiceWorkUtil serviceWorkUtil;
    private final LocalBroadCastUtil localBroadCastUtil;
    private final Hasher hasher;

    IntentFilter intentFilter;
    private CNAddressLookupCompleteCallback callback;

    @Inject
    CNAddressLookupDelegate(ServiceWorkUtil serviceWorkUtil, LocalBroadCastUtil localBroadCastUtil, Hasher hasher) {
        this.serviceWorkUtil = serviceWorkUtil;
        this.localBroadCastUtil = localBroadCastUtil;
        this.hasher = hasher;
        intentFilter = new IntentFilter(Intents.ACTION_WALLET_ADDRESS_RETRIEVED);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (callback != null &&
                Intents.ACTION_WALLET_ADDRESS_RETRIEVED.equals(intent.getAction()) &&
                intent.hasExtra(Intents.EXTRA_ADDRESS_LOOKUP_RESULT)) {

            AddressLookupResult result = intent.getParcelableExtra(Intents.EXTRA_ADDRESS_LOOKUP_RESULT);
            callback.onAddressLookupComplete(result);
        }

        localBroadCastUtil.unregisterReceiver(this);
    }

    public void fetchAddressFor(Contact contact, CNAddressLookupCompleteCallback callback) {
        this.callback = callback;
        localBroadCastUtil.registerReceiver(this, intentFilter);
        serviceWorkUtil.lookupAddressForPhoneNumberHash(contact.getHash());
    }

    public void fetchAddressFor(PhoneNumber phoneNumber, CNAddressLookupCompleteCallback callback) {
        this.callback = callback;
        String phoneNumberHash = hasher.hash(String.format("%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber()));
        localBroadCastUtil.registerReceiver(this, intentFilter);
        serviceWorkUtil.lookupAddressForPhoneNumberHash(phoneNumberHash);
    }

    public void teardown() {
        localBroadCastUtil.unregisterReceiver(this);
    }

    public interface CNAddressLookupCompleteCallback {
        void onAddressLookupComplete(AddressLookupResult addressLookupResult);
    }
}
