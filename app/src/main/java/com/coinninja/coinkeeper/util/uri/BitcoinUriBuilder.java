package com.coinninja.coinkeeper.util.uri;

import android.net.Uri;

import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter;
import com.coinninja.coinkeeper.util.uri.routes.BitcoinRoute;

import java.util.Map;

public class BitcoinUriBuilder extends UriParameterInterface<BitcoinRoute, BitcoinParameter, BitcoinUri> {

    String address;

    @Override
    public String getBaseScheme(){
        return "bitcoin";
    }

    @Override
    public String getBaseAuthority(){
        return address;
    }

    @Override
    public BitcoinUri build(BitcoinRoute route, Map<BitcoinParameter, String> parameters) {
        address = route.getAddress();
        Uri.Builder builder = getBuilder();

        for (BitcoinParameter parameter: parameters.keySet()) {
            builder.appendQueryParameter(parameter.getParameterKey(), parameters.get(parameter));
        }

        return new BitcoinUri(builder.build());
    }

    @Override
    public BitcoinUri build(BitcoinRoute route) {
        address = route.getAddress();
        return new BitcoinUri(getBuilder().build());
    }
}
