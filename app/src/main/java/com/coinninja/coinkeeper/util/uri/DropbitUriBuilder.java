package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter;
import com.coinninja.coinkeeper.util.uri.routes.DropbitRoute;

import java.util.Map;

import javax.inject.Inject;

public class DropbitUriBuilder extends UrlBuilderInterface<DropbitRoute, BitcoinParameter, Uri> {

    private static final String TOOLTIP_ROUTE = "tooltip";
    private static final String REGULAR_TRANSACTION_ROUTE = "regulartransaction";
    private static final String DROPBIT_TRANSACTION_ROUTE = "dropbittransaction";
    private static final String TRANSACTION_DETAILS = "transactiondetails";

    @Override
    public Uri build(DropbitRoute route) {
        Uri.Builder builder = getBuilder()
                .appendPath(TOOLTIP_ROUTE);

        switch (route) {
            case TRANSACTION_DETAILS:
                builder.appendPath(TRANSACTION_DETAILS);
                break;
            case REGULAR_TRANSACTION:
                builder.appendPath(REGULAR_TRANSACTION_ROUTE);
                break;
            case DROPBIT_TRANSACTION:
                builder.appendPath(DROPBIT_TRANSACTION_ROUTE);
                break;
        }

        return builder.build();
    }

    @Override
    public Uri build(DropbitRoute route, Map<BitcoinParameter, String> parameters, String... breadcrumbs) {
        return null;
    }

    @Override
    public Uri build(DropbitRoute route, String... breadcrumbs) {
        return build(route);
    }

    @Override
    public String getBaseAuthority() {
        return "dropbit.com";
    }

    @Override
    public Uri build(DropbitRoute route, Map<BitcoinParameter, String> parameters) {
        return null;
    }
}
