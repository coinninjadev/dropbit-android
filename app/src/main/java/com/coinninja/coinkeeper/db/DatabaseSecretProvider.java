package com.coinninja.coinkeeper.db;

import android.os.Build;

import com.coinninja.coinkeeper.di.interfaces.AppSecret;
import com.coinninja.coinkeeper.di.interfaces.DefaultSecret;
import com.coinninja.coinkeeper.util.Hasher;

import javax.inject.Inject;

public class DatabaseSecretProvider {
    private final Hasher hasher;
    private final String appSecret;
    private final String defaultSecret;

    @Inject
    DatabaseSecretProvider(Hasher hasher, @AppSecret String appSecret, @DefaultSecret String defaultSecret) {
        this.hasher = hasher;
        this.appSecret = appSecret;
        this.defaultSecret = defaultSecret;
    }

    public String getSecret() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return defaultSecret;
        else
            return hasher.hash(appSecret);
    }

    public String getDefault() {
        return defaultSecret;
    }
}
