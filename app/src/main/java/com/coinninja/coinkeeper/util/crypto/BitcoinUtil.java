package com.coinninja.coinkeeper.util.crypto;

import android.content.Context;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS;
import static com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter.AMOUNT;
import static com.coinninja.coinkeeper.util.uri.routes.BitcoinRoute.DEFAULT;

public class BitcoinUtil {
    private final Context context;
    private ADDRESS_INVALID_REASON addressInvalid;
    private BitcoinUriBuilder bitcoinUrlBuilder = new BitcoinUriBuilder();

    private HDWallet hdWallet;

    @Inject
    public BitcoinUtil(@ApplicationContext Context context, HDWallet hdWallet) {
        this.context = context;
        this.hdWallet = hdWallet;
    }

    public boolean isValidBIP39Words(String[] seedWords) {
        if (seedWords == null) return false;

        if (seedWords.length != 12) return false;

        List<String> words = Arrays.asList(context.getResources().getStringArray(R.array.recovery_words));
        if (words.containsAll(Arrays.asList(seedWords))) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidBTCAddress(String address) {
        if (address == null || address.isEmpty()) {
            addressInvalid = ADDRESS_INVALID_REASON.NULL_ADDRESS;
            return false;
        }

        if (isValidBC1Address(address)) {
            addressInvalid = ADDRESS_INVALID_REASON.IS_BC1;
            return false;
        }

        if (!isValidBTCAddressPattern(address)) {
            addressInvalid = ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
            return false;
        }

        if (!isValidBase58Address(address)) {
            addressInvalid = ADDRESS_INVALID_REASON.NOT_BASE58;
            return false;
        }

        addressInvalid = null;
        return true;
    }

    private boolean isValidBTCAddressPattern(String address) {
        String parsedAddress = parseBTCAddressFromText(address);
        if (parsedAddress == null) {
            return false;
        }

        if (parsedAddress.isEmpty()) {
            return false;
        }

        return parsedAddress.contains(address);
    }

    public String parseBTCAddressFromText(String anyString) {
        if (anyString == null || anyString.isEmpty()) {
            return "";
        }

        Pattern pattern = Pattern.compile(Intents.BITCOIN_ADDRESS_PATTERN);

        Matcher matcher = pattern.matcher(anyString);
        if (matcher.find()) {
            String out = matcher.group(0);
            return out;
        }
        return "";
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

        if (!isValidBTCAddress(btcAddress)) {
            BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = getInvalidReason();
            throw new UriException(invalidReason);
        }

        BTCCurrency btcCurrency;
        if (amount != null && !amount.isEmpty()) {
            btcCurrency = new BTCCurrency(amount);
        } else {
            btcCurrency = new BTCCurrency(0);
        }

        HashMap<BitcoinParameter, String> parameters = new HashMap<>();
        parameters.put(AMOUNT, btcCurrency.toFormattedString());

        return bitcoinUrlBuilder.build(DEFAULT.setAddress(btcAddress), parameters);
    }

    private boolean isValidBC1Address(String address) {
        String bc1KeyNormalized = "bc1".toLowerCase();
        String addressNormalized = address.toLowerCase();
        return addressNormalized.startsWith(bc1KeyNormalized);
    }

    public boolean isValidBase58Address(String address) {
        return hdWallet.isBase58CheckEncoded(address);
    }

    public ADDRESS_INVALID_REASON getInvalidReason() {
        return addressInvalid;
    }

    public enum ADDRESS_INVALID_REASON {
        NULL_ADDRESS, IS_BC1, NOT_BASE58, NOT_STANDARD_BTC_PATTERN
    }
}
