package com.coinninja.coinkeeper.factory;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

public class CurrencyFactory {
    @Inject
    CurrencyFactory() {}

    private static final Map<String, Currency> currencyMap;
    static {
        Map<String, Currency> aMap = new HashMap<>();
        aMap.put(BTCCurrency.SYMBOL, new BTCCurrency());
        aMap.put(USDCurrency.SYMBOL, new USDCurrency());
        currencyMap = Collections.unmodifiableMap(aMap);
    }

    public Currency fromSymbol(String symbol) {
        return currencyMap.get(symbol);
    }
}
