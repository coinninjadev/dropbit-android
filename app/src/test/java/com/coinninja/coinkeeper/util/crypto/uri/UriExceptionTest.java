package com.coinninja.coinkeeper.util.crypto.uri;

import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;

import org.junit.Test;

import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UriExceptionTest {

    @Test
    public void throw_UriException_test() {
        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            throw new UriException(NOT_STANDARD_BTC_PATTERN);
        } catch (UriException e) {
            e.printStackTrace();
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_STANDARD_BTC_PATTERN));
    }
}