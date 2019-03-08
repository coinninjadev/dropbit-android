package com.coinninja.coinkeeper.util.crypto;

import android.net.Uri;

import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS;

public class BitcoinUriBuilder {

    public static final String SCHEME = "bitcoin";
    private BitcoinUtil bitcoinUtil;
    Uri.Builder uriBuilder = new Uri.Builder();

    @Inject
    public BitcoinUriBuilder(BitcoinUtil bitcoinUtil) {
        this.bitcoinUtil = bitcoinUtil;
        uriBuilder.scheme(SCHEME);
    }

    public BitcoinUriBuilder setAddress(String address) {
        uriBuilder.authority(address);
        return this;
    }

    public BitcoinUriBuilder setAmount(BTCCurrency btcAmount) {
        BTCCurrency btc = new BTCCurrency(btcAmount.toSatoshis());
        btc.setCurrencyFormat("#,###.00000000");
        uriBuilder.appendQueryParameter("amount", btc.toFormattedCurrency());
        return this;
    }

    public BitcoinUri build() {
        return new BitcoinUri(uriBuilder.build());
    }

    public BitcoinUri parse(String textData) throws UriException {
        if (textData == null || textData.isEmpty()) {
            throw new UriException(NULL_ADDRESS);
        }

        Pattern pattern = Pattern.compile(Intents.BITCOIN_URI_PATTERN);
        Matcher matcher = pattern.matcher(textData);

        String btcAddress;
        String amount;

        if (!matcher.find()) {
            throw new UriException(NOT_STANDARD_BTC_PATTERN);
        }

        btcAddress = matcher.group(1);
        amount = matcher.group(3);

        if (!bitcoinUtil.isValidBTCAddress(btcAddress)) {
            BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();
            throw new UriException(invalidReason);
        }


        BTCCurrency btcCurrency;
        if (amount != null && !amount.isEmpty()) {
            btcCurrency = new BTCCurrency(amount);
        } else {
            btcCurrency = new BTCCurrency(0);
        }
        return parameters(btcAddress, btcCurrency);
    }

    private BitcoinUri parameters(String address, BTCCurrency amount) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME)
                .authority(address)
                .appendQueryParameter("amount", amount.toFormattedString());
        return new BitcoinUri(builder.build());
    }
}
