package com.coinninja.coinkeeper.util.currency;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class BTCtoUSDConversionsTest {

    private final static double CONVERSION_VALUE = 6603.30D;
    private Currency evalueationCurrency;

    @Before
    public void setUp() {
        evalueationCurrency = new USDCurrency(CONVERSION_VALUE);
    }

    /**
     * Given USD: 0
     * Given BTC: .01976285
     */
    @Test
    public void convertsBTCtoUSD() {
        BTCCurrency btcCurrency = new BTCCurrency("0.01976285");

        USDCurrency usdCurrency = btcCurrency.toUSD(evalueationCurrency);

        assertThat(usdCurrency.toFormattedCurrency(), equalTo("$130.50"));
    }

    /**
     * Given USD: 130
     * Given BTC: 0
     */
    @Test
    public void convertsUSDtoBTC() {
        USDCurrency usdCurrency = new USDCurrency("130.5");

        BTCCurrency btcCurrency = usdCurrency.toBTC(evalueationCurrency);

        assertThat(btcCurrency.toLong(), equalTo(1976285L));
    }


    /**
     * eval usd 6612.70
     * Given USD: 3
     * Given BTC: .00045367
     */
    @Test
    public void roundingIssueUSDtoBTC() {
        USDCurrency eval = new USDCurrency(6612.7D);
        BTCCurrency btcCurrency = new BTCCurrency(".00045367");

        USDCurrency usdCurrency = btcCurrency.toUSD(eval);

        assertThat(usdCurrency.toFormattedCurrency(), equalTo("$3.00"));
    }
}
