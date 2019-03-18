package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute;

import javax.inject.Inject;

public class CoinNinjaUriBuilder extends CustomUriBuilder<CoinNinjaRoute> {

    private static final String TRANSACTION_ROUTE = "tx";
    private static final String ADDRESS_ROUTE = "address";

    @Inject
    public CoinNinjaUriBuilder() {
    }

    @Override
    public Uri build(CoinNinjaRoute route) {
        Uri.Builder builder = getBuilder();

        switch (route) {
            case ADDRESS:
            case TRANSACTION:
                break;
        }

        return builder.build();
    }

    @Override
    public Uri build(CoinNinjaRoute route, String... breadcrumbs) {
        Uri.Builder builder = getBuilder();

        switch (route) {
            case TRANSACTION:
                builder.appendPath(TRANSACTION_ROUTE);
                break;
            case ADDRESS:
                builder.appendPath(ADDRESS_ROUTE);
                break;
        }

        for (String breadcrumb : breadcrumbs) {
            builder.appendPath(breadcrumb);
        }

        return builder.build();
    }

    @Override
    public String getBaseAuthority() {
        return "coinninja.com";
    }
}
