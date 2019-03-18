package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import java.util.Map;

public abstract class UrlBuilderInterface<T, R, X> extends UriParameterInterface<T, R, X> {
    public abstract X build(T route, Map<R, String> parameters, String... breadcrumbs);
    public abstract X build(T route, String... breadcrumbs);
}
