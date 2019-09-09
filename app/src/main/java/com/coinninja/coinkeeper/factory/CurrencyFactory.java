package com.coinninja.coinkeeper.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import app.dropbit.commons.currency.BTCCurrency;
import app.dropbit.commons.currency.Currency;
import app.dropbit.commons.currency.USDCurrency;

public class CurrencyFactory {

    private Map<String, Currency> currencyMap = new HashMap();

    @Inject
    CurrencyFactory() {
        reset();
    }

    private void setupDataSource() {
        Map<String, Currency> aMap = new HashMap<>();
        aMap.put(BTCCurrency.SYMBOL, new BTCCurrency());
        aMap.put(USDCurrency.SYMBOL, new USDCurrency());
        currencyMap = Collections.unmodifiableMap(aMap);
    }

    public void reset() {
        currencyMap = new HashMap();
        setupDataSource();
    }

    public Currency fromSymbol(String symbol) {
        return currencyMap.get(symbol);
    }
}
