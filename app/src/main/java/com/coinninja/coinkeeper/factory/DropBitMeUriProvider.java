package com.coinninja.coinkeeper.factory;

import android.net.Uri;

import com.coinninja.coinkeeper.di.interfaces.DebugBuild;

import javax.inject.Inject;

public class DropBitMeUriProvider {

    private final boolean isDebug;

    @Inject
    DropBitMeUriProvider(@DebugBuild boolean isDebug) {
        this.isDebug = isDebug;
    }

    public Uri provideUri() {
        String base = "https://dropbit.me";
        if (isDebug) {
            base = "https://test.dropbit.me";
        }
        return Uri.parse(base);
    }
}
