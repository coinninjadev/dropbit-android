package com.coinninja.coinkeeper.factory;

import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

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

    public void reset(){
        currencyMap = new HashMap();
        setupDataSource();
    }

    public Currency fromSymbol(String symbol) {
        return currencyMap.get(symbol);
    }
}
