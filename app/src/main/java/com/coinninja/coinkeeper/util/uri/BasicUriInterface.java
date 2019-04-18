package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

public abstract class BasicUriInterface<RouteType, UriType> extends BasicUriBuilder {
    public abstract UriType build(RouteType route);
}

