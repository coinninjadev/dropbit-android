package com.coinninja.coinkeeper.util;

import android.content.Intent;

import com.coinninja.coinkeeper.factory.CurrencyFactory;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.Currency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import javax.inject.Inject;

public class CurrencyPreference {
    static final String PREFERENCE_PRIMARY_CURRENCY = "primaryCurrency";
    static final String PREFERENCE_SECONDARY_CURRENCY = "secondaryCurrency";
    private final PreferencesUtil preferencesUtil;
    private final CurrencyFactory currencyFactory;
    private final LocalBroadCastUtil localBroadCastUtil;

    @Inject
    public CurrencyPreference(PreferencesUtil preferencesUtil, CurrencyFactory currencyFactory,
                              LocalBroadCastUtil localBroadCastUtil) {
        this.preferencesUtil = preferencesUtil;
        this.currencyFactory = currencyFactory;
        this.localBroadCastUtil = localBroadCastUtil;
    }

    public void reset() {
        currencyFactory.reset();
    }

    public void setCurrencies(Currency primary, Currency secondary) {
        preferencesUtil.savePreference(PREFERENCE_PRIMARY_CURRENCY, primary.getSymbol());
        preferencesUtil.savePreference(PREFERENCE_SECONDARY_CURRENCY, secondary.getSymbol());
        Intent intent = new Intent(Intents.ACTION_CURRENCY_PREFERENCE_CHANGED);
        intent.putExtra(Intents.EXTRA_PREFERENCE, getCurrenciesPreference());
        localBroadCastUtil.sendBroadcast(intent);
    }

    public DefaultCurrencies getCurrenciesPreference() {
        String primarySymbol = preferencesUtil.getString(PREFERENCE_PRIMARY_CURRENCY, BTCCurrency.SYMBOL);
        String secondarySymbol = preferencesUtil.getString(PREFERENCE_SECONDARY_CURRENCY, USDCurrency.SYMBOL);
        Currency primaryCurrency = currencyFactory.fromSymbol(primarySymbol);
        Currency secondaryCurrency = currencyFactory.fromSymbol(secondarySymbol);
        return new DefaultCurrencies(primaryCurrency, secondaryCurrency);
    }

    public DefaultCurrencies toggleDefault() {
        DefaultCurrencies defaultCurrencies = getCurrenciesPreference();
        setCurrencies(defaultCurrencies.getSecondaryCurrency(), defaultCurrencies.getPrimaryCurrency());
        return new DefaultCurrencies(defaultCurrencies.getSecondaryCurrency(), defaultCurrencies.getPrimaryCurrency());
    }

    public Currency getFiat() {
        return getCurrenciesPreference().getFiat();
    }
}
