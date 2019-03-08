package com.coinninja.coinkeeper.util.android;

import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.util.Intents;

import javax.inject.Inject;

public class ServiceWorkUtil {
    private final Context context;

    @Inject
    ServiceWorkUtil(@ApplicationContext Context context) {
        this.context = context;
    }

    public void lookupAddressForPhoneNumberHash(String phoneNumberHash) {
        Intent intent = new Intent(context, CNWalletAddressRequestService.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER_HASH, phoneNumberHash);
        context.startService(intent);
    }
}
