package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;
import com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class CoinNinjaUriBuilder extends UrlBuilderInterface<CoinNinjaRoute, CoinNinjaParameter, Uri> {

    @Inject
    public CoinNinjaUriBuilder() {
    }

    @Override
    public Uri build(CoinNinjaRoute route) {
        return build(route, new HashMap<>());
    }

    @Override
    public Uri build(CoinNinjaRoute route, Map<CoinNinjaParameter, String> parameters, String... breadcrumbs) {
        return build(route, breadcrumbs);
    }

    @Override
    public Uri build(CoinNinjaRoute route, String... breadcrumbs) {
        Uri.Builder builder = getBuilder().appendPath(route.getPath());

        for (String breadcrumb : breadcrumbs) {
            builder.appendPath(breadcrumb);
        }

        return builder.build();
    }

    @Override
    public String getBaseAuthority() {
        return "coinninja.com";
    }

    @Override
    public String getBaseScheme(){
        return "https";
    }

    @Override
    public Uri build(CoinNinjaRoute route, Map<CoinNinjaParameter, String> parameters) {
        return build(route, new HashMap<>());
    }
}
