package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

public class BasicUriBuilder {

    protected Uri.Builder getBuilder() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(getBaseScheme()).
                authority(getBaseAuthority());

        return builder;
    }

    public String getBaseAuthority() {
        return "";
    }
    public String getBaseScheme() {
        return "";
    }
}
