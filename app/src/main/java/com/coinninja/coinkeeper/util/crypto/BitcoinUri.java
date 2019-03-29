package com.coinninja.coinkeeper.util.crypto;

import android.net.Uri;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class BitcoinUri {

    private final Uri baseUri;

    public BitcoinUri(Uri baseUri) {
        this.baseUri = baseUri;
        determineIfUriIsBip70();
    }

    private boolean isBip70 = false;
    private Uri bip70Uri = null;

    public String getAddress() {
        return baseUri.getAuthority();
    }

    public Long getSatoshiAmount() {
        String btcAmount = baseUri.getQueryParameter(BitcoinParameter.AMOUNT.getParameterKey());
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

    public boolean getIsBip70() {
        return isBip70;
    }

    private void determineIfUriIsBip70() {
        String bip70 = baseUri.getQueryParameter(BitcoinParameter.BIP70URL.getParameterKey());
        String bip70Long = baseUri.getQueryParameter(BitcoinParameter.BIP70URLLONG.getParameterKey());

        if (bip70 != null) {
            isBip70 = true;
            bip70Uri = Uri.parse(bip70);
        } else if (bip70Long != null) {
            isBip70 = true;
            bip70Uri = Uri.parse(bip70Long);
        }
    }

    public Uri getBip70UrlIfApplicable() {
        if (!isBip70 || bip70Uri == null) { return null; }

        return bip70Uri;
    }

    @NonNull
    @Override
    public String toString() {
        return baseUri.toString().replace("bitcoin://", "bitcoin:");
    }

    public Uri toUri() {
        return baseUri;
    }
}
