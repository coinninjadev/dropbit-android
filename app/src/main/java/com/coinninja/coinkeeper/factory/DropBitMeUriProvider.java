package com.coinninja.coinkeeper.factory;

import android.net.Uri;

import com.coinninja.coinkeeper.di.interfaces.IsProduction;

import javax.inject.Inject;

public class DropBitMeUriProvider {

    private final boolean isProduction;

    @Inject
    DropBitMeUriProvider(@IsProduction boolean isProduction) {
        this.isProduction = isProduction;
    }

    public Uri provideUri() {
        String base = "https://test.dropbit.me";
        if (isProduction) {
            base = "https://dropbit.me";
        }
        return Uri.parse(base);
    }
}
