package com.coinninja.coinkeeper.util.crypto.uri;

import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;

public class UriException extends Exception {
    private final BitcoinUtil.ADDRESS_INVALID_REASON reason;

    public UriException(BitcoinUtil.ADDRESS_INVALID_REASON reason) {
        this.reason = reason;
    }

    public BitcoinUtil.ADDRESS_INVALID_REASON getReason() {
        return reason;
    }
}
