package com.coinninja.coinkeeper.util.crypto;

import android.app.Application;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.IS_BC1;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN;
import static com.coinninja.coinkeeper.util.crypto.BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BitcoinUtilTest {
    private String[] invalid_words = {"word1", "word2", "word3", "word4", "word5", "word6",
            "word7", "word8", "word9", "word10", "word11", "word12"};
    private String[] valid_words = {"abandon", "abandon", "abandon", "abandon", "abandon", "abandon",
            "abandon", "abandon", "abandon", "abandon", "abandon", "about"};

    private BitcoinUtil bitcoinUtil;
    private Application application;

    @Mock
    private HDWallet hdWallet;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        application = RuntimeEnvironment.application;
        bitcoinUtil = new BitcoinUtil(application, hdWallet);

        when(hdWallet.isBase58CheckEncoded("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")).thenReturn(true);
    }

    @Test
    public void test_valid_bip39_seed_words() {
        String[] sampleWords = valid_words;

        boolean isValid = bitcoinUtil.isValidBIP39Words(sampleWords);

        assertTrue(isValid);
    }


    @Test
    public void test_null_seed_words() {
        String[] sampleWords = null;

        boolean isValid = bitcoinUtil.isValidBIP39Words(sampleWords);

        assertFalse(isValid);
    }

    @Test
    public void test_invalid_bip39_seed_words___not_part_of_the_list_of_words() {
        String[] sampleWords = invalid_words;

        boolean isValid = bitcoinUtil.isValidBIP39Words(sampleWords);

        assertFalse(isValid);
    }

    @Test
    public void test_invalid_bip39_seed_words___missing_words() {
        String[] sampleWords = new String[]{valid_words[0], valid_words[1], valid_words[2], valid_words[3]};

        boolean isValid = bitcoinUtil.isValidBIP39Words(sampleWords);

        assertThat(sampleWords.length, equalTo(4));
        assertFalse(isValid);
    }

    @Test
    public void is_valid_btc_address_check_with_a_valid_address_test() {
        String sampleAddress = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        boolean isValidBTCAddress = bitcoinUtil.isValidBTCAddress(sampleAddress);

        assertTrue(isValidBTCAddress);
    }

    @Test
    public void is_valid_btc_address_check_with_error_NOT_STANDARD_BTC_PATTERN_reason_test() {
        String sampleAddress = "444445t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        boolean isValidBTCAddress = bitcoinUtil.isValidBTCAddress(sampleAddress);
        BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();

        assertFalse(isValidBTCAddress);
        assertThat(invalidReason, equalTo(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_STANDARD_BTC_PATTERN));
    }

    @Test
    public void is_valid_btc_address_check_with_error_IS_A_BC1_reason_test() {
        String sampleAddress = "BC135t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        boolean isValidBTCAddress = bitcoinUtil.isValidBTCAddress(sampleAddress);
        BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();

        assertFalse(isValidBTCAddress);
        assertThat(invalidReason, equalTo(BitcoinUtil.ADDRESS_INVALID_REASON.IS_BC1));
    }

    @Test
    public void is_valid_btc_address_check_with_error_NOT_BASE58_reason_test() {
        String sampleAddress = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaaaaa";
        when(hdWallet.isBase58CheckEncoded("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaaaaa")).thenReturn(false);

        boolean isValidBTCAddress = bitcoinUtil.isValidBTCAddress(sampleAddress);
        BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();

        assertFalse(isValidBTCAddress);
        assertThat(invalidReason, equalTo(BitcoinUtil.ADDRESS_INVALID_REASON.NOT_BASE58));
    }

    @Test
    public void is_valid_btc_address_check_with_error_null_reason_test() {
        String sampleAddress = null;

        boolean isValidBTCAddress = bitcoinUtil.isValidBTCAddress(sampleAddress);
        BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();

        assertFalse(isValidBTCAddress);
        assertThat(invalidReason, equalTo(BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS));
    }

    @Test
    public void is_valid_btc_address_check_with_error_empty_reason_test() {
        String sampleAddress = "";

        boolean isValidBTCAddress = bitcoinUtil.isValidBTCAddress(sampleAddress);
        BitcoinUtil.ADDRESS_INVALID_REASON invalidReason = bitcoinUtil.getInvalidReason();

        assertFalse(isValidBTCAddress);
        assertThat(invalidReason, equalTo(BitcoinUtil.ADDRESS_INVALID_REASON.NULL_ADDRESS));
    }

    @Test
    public void parse_btc_address_from_any_text_test() {
        String sampleText = "this is some text that also has a btc address inside it 35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa that it this is the end ";

        String address = bitcoinUtil.parseBTCAddressFromText(sampleText);

        assertThat(address, equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
    }

    @Test
    public void parse_btc_address_from_any_text_error_null_test() {
        String sampleText = null;

        String address = bitcoinUtil.parseBTCAddressFromText(sampleText);

        assertTrue(address.isEmpty());
    }

    @Test
    public void parse_btc_address_from_any_text_error_empty_test() {
        String sampleText = "";

        String address = bitcoinUtil.parseBTCAddressFromText(sampleText);

        assertTrue(address.isEmpty());
    }


    @Test
    public void throw_exception_when_parsing_empty_data_test() {
        String sampleURI = "";

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            bitcoinUtil.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NULL_ADDRESS));
    }

    @Test
    public void throw_exception_when_parsing_null_data_test() {
        String sampleURI = null;

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            bitcoinUtil.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NULL_ADDRESS));
    }

    @Test
    public void throw_exception_when_parsing_a_string_with_no_btc_data_test() {
        String sampleURI = "Hello, how are you today?";

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            bitcoinUtil.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_STANDARD_BTC_PATTERN));
    }

    @Test
    public void throw_exception_when_parsing_NOT_STANDARD_BTC_PATTERN_address_test() {
        String sampleURI = "555t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            bitcoinUtil.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_STANDARD_BTC_PATTERN));
    }

    @Test
    public void throw_exception_when_parsing_NOT_BASE58_address_test() throws Exception {
        String sampleURI = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xaaaa";
        when(hdWallet.isBase58CheckEncoded(anyString())).thenReturn(false);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            bitcoinUtil.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(NOT_BASE58));
    }

    @Test
    public void throw_exception_when_parsing_a_BC1_address_test() {
        String sampleURI = "bc135t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        when(hdWallet.isBase58CheckEncoded(anyString())).thenReturn(true);

        BitcoinUtil.ADDRESS_INVALID_REASON reason = null;
        try {
            bitcoinUtil.parse(sampleURI);
        } catch (UriException e) {
            reason = e.getReason();
        }

        assertThat(reason, equalTo(IS_BC1));
    }
}