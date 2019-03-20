package com.coinninja.coinkeeper.util.uri;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.DROPBIT_TRANSACTION;
import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.REGULAR_TRANSACTION;
import static com.coinninja.coinkeeper.util.uri.routes.DropbitRoute.TRANSACTION_DETAILS;
import static junit.framework.TestCase.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class DropbitUriBuilderTest {

    private DropbitUriBuilder builder = new DropbitUriBuilder();

    @Test
    public void test_dropbit_transaction_url() {
        assertEquals(builder.build(DROPBIT_TRANSACTION).toString(), "https://dropbit.com/tooltip/dropbittransaction");
    }

    @Test
    public void test_regular_transaction_url() {
        assertEquals(builder.build(REGULAR_TRANSACTION).toString(), "https://dropbit.com/tooltip/regulartransaction");
    }

    @Test
    public void test_transaction_details_url() {
        assertEquals(builder.build(TRANSACTION_DETAILS).toString(), "https://dropbit.com/tooltip/transactiondetails");
    }

}