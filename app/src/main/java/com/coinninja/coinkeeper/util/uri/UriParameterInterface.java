package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import java.util.Map;

public abstract class UriParameterInterface<T, R, X> extends BasicUriInterface<T, X> {
    public abstract X build(T route, Map<R, String> parameters);
}
