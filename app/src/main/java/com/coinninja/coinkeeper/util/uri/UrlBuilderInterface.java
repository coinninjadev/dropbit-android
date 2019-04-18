package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import java.util.Map;

public abstract class UrlBuilderInterface<RouteType, ParameterType, UriType> extends UriParameterInterface<RouteType, ParameterType, UriType> {
    public abstract UriType build(RouteType route, Map<ParameterType, String> parameters, String... breadcrumbs);
    public abstract UriType build(RouteType route, String... breadcrumbs);
}
