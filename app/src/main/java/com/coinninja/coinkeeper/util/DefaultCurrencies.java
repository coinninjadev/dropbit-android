package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.util.currency.Currency;

public class DefaultCurrencies {
    private final Currency primaryCurrency;
    private final Currency secondaryCurrency;

    public DefaultCurrencies(Currency primaryCurrency, Currency secondaryCurrency) {
        this.primaryCurrency = primaryCurrency;
        this.secondaryCurrency = secondaryCurrency;
    }

    public Currency getFiat(){
        return getPrimaryCurrency().isFiat() ?
                getPrimaryCurrency() :
                getSecondaryCurrency();
    }

    public Currency getCrypto(){
        return getPrimaryCurrency().isCrypto() ?
                getPrimaryCurrency() :
                getSecondaryCurrency();
    }

    public Currency getPrimaryCurrency() {
        return primaryCurrency;
    }

    public Currency getSecondaryCurrency() {
        return secondaryCurrency;
    }

}
