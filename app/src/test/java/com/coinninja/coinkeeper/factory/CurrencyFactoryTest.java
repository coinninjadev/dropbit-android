package com.coinninja.coinkeeper.factory;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CurrencyFactoryTest {

    CurrencyFactory factory = new CurrencyFactory();

    @After
    public void tearDown() {
        factory = null;
    }

    @Test
    public void creates_from_btc() {
        assertTrue(factory.fromSymbol(BTCCurrency.SYMBOL) instanceof BTCCurrency);
    }

    @Test
    public void creates_from_usd() {
        assertTrue(factory.fromSymbol("$") instanceof USDCurrency);
    }

}