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
        return createBuilder(route, parameters, breadcrumbs).build();
    }

    @Override
    public Uri build(CoinNinjaRoute route, String... breadcrumbs) {
        return createBuilder(route, null, breadcrumbs).build();
    }

    @Override
    public String getBaseAuthority() {
        return "coinninja.com";
    }

    @Override
    public String getBaseScheme() {
        return "https";
    }

    @Override
    public Uri build(CoinNinjaRoute route, Map<CoinNinjaParameter, String> parameters) {
        return createBuilder(route, parameters, "").build();
    }

    private Uri.Builder createBuilder(CoinNinjaRoute route, Map<CoinNinjaParameter, String> parameters, String... breadcrumbs) {
        Uri.Builder builder = getBuilder().appendPath(route.getPath());

        if (breadcrumbs != null) {
            for (String breadcrumb : breadcrumbs) {
                if (!breadcrumb.isEmpty()) {
                    builder.appendPath(breadcrumb);
                }
            }
        }

        if (parameters != null) {
            for (CoinNinjaParameter parameter: parameters.keySet()) {
                builder.appendQueryParameter(parameter.getParameterKey(), parameters.get(parameter));
            }
        }

        return builder;
    }

}
