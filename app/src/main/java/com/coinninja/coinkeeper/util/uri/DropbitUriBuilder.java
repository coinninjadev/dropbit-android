package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import com.coinninja.coinkeeper.util.uri.parameter.DropbitParameter;
import com.coinninja.coinkeeper.util.uri.routes.DropbitRoute;

import java.util.Map;

import javax.inject.Inject;

public class DropbitUriBuilder extends UrlBuilderInterface<DropbitRoute, DropbitParameter, Uri> {

    @Inject
    public DropbitUriBuilder() {
    }

    @Override
    public Uri build(DropbitRoute route) {
        Uri.Builder builder = getBuilder();

        for (String path : route.getPath()) {
            builder.appendPath(path);
        }

        return builder.build();
    }

    @Override
    public Uri build(DropbitRoute route, Map<DropbitParameter, String> parameters, String... breadcrumbs) {
        return build(route);
    }

    @Override
    public Uri build(DropbitRoute route, String... breadcrumbs) {
        return build(route);
    }

    @Override
    public String getBaseAuthority() {
        return "dropbit.app";
    }

    @Override
    public String getBaseScheme(){
        return "https";
    }

    @Override
    public Uri build(DropbitRoute route, Map<DropbitParameter, String> parameters) {
        return build(route);
    }
}
