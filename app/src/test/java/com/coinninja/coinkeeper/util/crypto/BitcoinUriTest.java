package com.coinninja.coinkeeper.util.crypto;

import android.app.Application;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.util.crypto.uri.UriException;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.uri.BitcoinUriBuilder;
import com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter;
import com.coinninja.coinkeeper.util.uri.routes.BitcoinRoute;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.uri.parameter.BitcoinParameter.AMOUNT;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BitcoinUriTest {

    @Inject
    BitcoinUtil bitcoinUtil;

    BitcoinUriBuilder uriBuilder = new BitcoinUriBuilder();
    private Application application;
    private HDWallet hdWallet;

    @Before
    public void setUp() throws Exception {
        hdWallet = mock(HDWallet.class);
        application = RuntimeEnvironment.application;
        bitcoinUtil = new BitcoinUtil(application, hdWallet);

        when(bitcoinUtil.isValidBTCAddress("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa")).thenReturn(true);

        when(hdWallet.isBase58CheckEncoded(anyString())).thenReturn(true);
    }

    @Test
    public void builds_with_amount() {
        String address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";
        BTCCurrency btc = new BTCCurrency("1.0");
        HashMap<BitcoinParameter, String> parameters = new HashMap<>();
        parameters.put(AMOUNT, btc.toUriFormattedString());

        assertThat(uriBuilder.build(BitcoinRoute.DEFAULT.setAddress(address), parameters).toString(),
                equalTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=1.00000000"));

        assertThat(btc.toFormattedCurrency(), equalTo("\u20BF 1"));
    }

    @Test
    public void builds_from_address() {
        String address = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        assertThat(uriBuilder.build(BitcoinRoute.DEFAULT.setAddress(address)).toString(),
                equalTo("bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
    }

    @Test
    public void parse_a_valid_btc_uri_with_btc_address_and_amount_test() throws Exception {
        String sampleURI = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325";

        BitcoinUri bitcoinUri = bitcoinUtil.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(325000l));
    }

    @Test
    public void parse_a_valid_btc_uri_with_btc_address_and_no_amount_test() throws Exception {
        String sampleURI = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        BitcoinUri bitcoinUri = bitcoinUtil.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(0l));
    }

    @Test
    public void parse_just_a_valid_btc_address_and_no_amount_not_a_uri_test() throws Exception {
        String sampleURI = "35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa";

        BitcoinUri bitcoinUri = bitcoinUtil.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(0l));
    }

    @Test
    public void parses_uri_with_amount() throws UriException {
        String text = "bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=.00153794";
        BitcoinUri uri = bitcoinUtil.parse(text);

        assertThat(uri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(uri.getSatoshiAmount(), equalTo(153794L));
    }

    @Test
    public void parse_a_random_string_that_contains_a_valid_btc_uri_test() throws Exception {
        String sampleURI = "Hello, here is my btc request bitcoin:35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa?amount=0.00325 Thanks for sending me the money";

        BitcoinUri bitcoinUri = bitcoinUtil.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(325000l));
    }

    @Test
    public void parse_a_random_string_that_contains_a_btc_address_somewhere_in_it_test() throws Exception {
        String sampleURI = "Whats up my man. Here is my btc address 35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa did you need any more information?";

        BitcoinUri bitcoinUri = bitcoinUtil.parse(sampleURI);

        assertThat(bitcoinUri.getAddress(), equalTo("35t99geKQGdRyJC7fKQ4GeJrV5YvYCo7xa"));
        assertThat(bitcoinUri.getSatoshiAmount(), equalTo(0l));
    }
}