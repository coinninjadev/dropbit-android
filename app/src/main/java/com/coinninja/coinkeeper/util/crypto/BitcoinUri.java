package com.coinninja.coinkeeper.util.crypto;

import android.net.Uri;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import androidx.annotation.NonNull;

public class BitcoinUri {

    private static final String URI_QUERY_AMOUNT = "amount";
    private final Uri baseUri;

    BitcoinUri(Uri baseUri) {
        this.baseUri = baseUri;
    }


    public String getAddress() {
        return baseUri.getAuthority();
    }


    public Long getSatoshiAmount() {
        String btcAmount = baseUri.getQueryParameter(URI_QUERY_AMOUNT);
        return btcStringAmountToSatoshis(btcAmount);
    }

    private long btcStringAmountToSatoshis(String amount) {
        if (amount != null && !amount.isEmpty()) {
            try {
                BTCCurrency btcCurrency = new BTCCurrency(amount);
                return btcCurrency.toSatoshis();
            } catch (Exception e) {
                return 0l;
            }
        }
        return 0l;
    }

    @NonNull
    @Override
    public String toString() {
        return baseUri.toString().replace("//", "");
    }

    public Uri toUri() {
        return baseUri;
    }
}
