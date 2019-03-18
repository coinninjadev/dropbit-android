package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

public abstract class BasicUriInterface<T, X> extends BasicUriBuilder {
    public abstract X build(T route);
}

