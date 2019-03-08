package com.coinninja.coinkeeper.util.crypto;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.IS_BC1;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BitcoinUriTest {

    private BitcoinUtil bitcoinUtil;
    BitcoinUriBuilder uriBuilder;

    @Before
    public void setUp() throws Exception {
        bitcoinUtil = mock(BitcoinUtil.class);
        uriBuilder = new BitcoinUriBuilder(bitcoinUtil);

        when(bitcoinUtil.isValidBTCAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")).thenReturn(true);
        when(bitcoinUtil.isValidBTCAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")).thenReturn(true);

    }

    @Test
    public void builds_with_amount() {
        String address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        BTCCurrency btc = new BTCCurrency("1.0");

        assertThat(uriBuilder.setAddress(address).setAmount(btc).build().toString(),
                equalTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.00000000"));

        assertThat(btc.toFormattedCurrency(), equalTo("\u20BF 1"));
    }

    @Test
    public void builds_from_address() {
        String address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        assertThat(uriBuilder.setAddress(address).build().toString(),
                equalTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
    }

    @Test
    public void parse_a_valid_btc_uri_with_btc_address_and_amount_test() throws Exception {
        String sampleURI = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325";

        BitcoinUri bitcoinUri = uriBuilder.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(325000l));
    }

    @Test
    public void parse_a_valid_btc_uri_with_btc_address_and_no_amount_test() throws Exception {
        String sampleURI = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        BitcoinUri bitcoinUri = uriBuilder.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(0l));
    }

    @Test
    public void parse_just_a_valid_btc_address_and_no_amount_not_a_uri_test() throws Exception {
        String sampleURI = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        BitcoinUri bitcoinUri = uriBuilder.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(0l));
    }

    @Test
    public void parses_uri_with_amount() throws UriException {
        String text = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=.00153794";
        BitcoinUri uri = uriBuilder.parse(text);

        assertThat(uri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(uri.getSatoshiAmount(), equalTo(153794L));
    }

    @Test
    public void parse_a_random_string_that_contains_a_valid_btc_uri_test() throws Exception {
        String sampleURI = "Hello, here is my btc request bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325 Thanks for sending me the money";

        BitcoinUri bitcoinUri = uriBuilder.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(325000l));
    }

    @Test
    public void parse_a_random_string_that_contains_a_btc_address_somewhere_in_it_test() throws Exception {
        String sampleURI = "Whats up my man. Here is my btc address 35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa did you need any more information?";

        BitcoinUri bitcoinUri = uriBuilder.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(0l));
    }

    @Test
    public void throw_exception_when_parsing_empty_data_test() {
        String sampleURI = "";
        when(bitcoinUtil.isValidBTCAddress(sampleURI)).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NULL_ADDRESS);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            uriBuilder.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NULL_ADDRESS));
    }

    @Test
    public void throw_exception_when_parsing_null_data_test() {
        String sampleURI = null;
        when(bitcoinUtil.isValidBTCAddress(sampleURI)).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NULL_ADDRESS);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            uriBuilder.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NULL_ADDRESS));
    }

    @Test
    public void throw_exception_when_parsing_a_string_with_no_btc_data_test() {
        String sampleURI = "Hello, how are you today?";
        when(bitcoinUtil.isValidBTCAddress(sampleURI)).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_STANDARD_BTC_PATTERN);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            uriBuilder.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_STANDARD_BTC_PATTERN));
    }

    @Test
    public void throw_exception_when_parsing_a_BC1_addres_test() {
        String sampleURI = "BC135t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        when(bitcoinUtil.isValidBTCAddress("BC135t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(IS_BC1);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            uriBuilder.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(IS_BC1));
    }

    @Test
    public void throw_exception_when_parsing_NOT_STANDARD_BTC_PATTERN_address_test() {
        String sampleURI = "555t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        when(bitcoinUtil.isValidBTCAddress("555t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_STANDARD_BTC_PATTERN);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            uriBuilder.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_STANDARD_BTC_PATTERN));
    }

    @Test
    public void throw_exception_when_parsing_NOT_BASE58_address_test() throws Exception {
        String sampleURI = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaaaa";
        when(bitcoinUtil.isValidBTCAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaaaa")).thenReturn(false);
        when(bitcoinUtil.getInvalidReason()).thenReturn(NOT_BASE58);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            uriBuilder.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_BASE58));
    }
}