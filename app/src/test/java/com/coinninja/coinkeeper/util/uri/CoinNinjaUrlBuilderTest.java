package com.coinninja.coinkeeper.util.uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.ADDRESS;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.TRANSACTION;
import static junit.framework.TestCase.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class CoinNinjaUrlBuilderTest {

    private CoinNinjaUriBuilder builder = new CoinNinjaUriBuilder();

    @Test
    public void test_coinninja_transaction_url() {
        String txid = "skjfvhw9834fhqw398fhw493e8fhw39fdhjsfjbkhe4g";
        assertEquals(builder.build(TRANSACTION, txid).toString(), "https://coinninja.com/tx/" + txid);
    }

    @Test
    public void test_coinninja_address_url() {
        String address = "3409g3j4g0934jtweigfjw0943tru2w90";
        assertEquals(builder.build(ADDRESS, address).toString(), "https://coinninja.com/address/" + address);
    }

}
