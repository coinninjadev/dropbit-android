package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import java.util.Map;

public abstract class UriParameterInterface<RouteType, ParameterType, UriType> extends BasicUriInterface<RouteType, UriType> {
    public abstract UriType build(RouteType route, Map<ParameterType, String> parameters);
}
