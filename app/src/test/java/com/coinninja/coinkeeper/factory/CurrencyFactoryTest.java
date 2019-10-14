package com.coinninja.coinkeeper.factory;

import org.junit.After;
import org.junit.Test;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.USDCurrency;

import static org.junit.Assert.assertTrue;

public class CurrencyFactoryTest {

    CurrencyFactory factory = new CurrencyFactory();

    @After
    public void tearDown() {
        factory = null;
    }

    @Test
    public void creates_from_btc() {
        assertTrue(factory.fromSymbol(BTCCurrency.symbol) instanceof BTCCurrency);
    }

    @Test
    public void creates_from_usd() {
        assertTrue(factory.fromSymbol("$") instanceof USDCurrency);
    }

}