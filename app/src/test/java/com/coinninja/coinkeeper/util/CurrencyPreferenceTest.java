package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.factory.CurrencyFactory;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.util.currency.BTCCurrency;
import com.coinninja.coinkeeper.util.currency.USDCurrency;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyPreferenceTest {

    @InjectMocks
    CurrencyPreference currencyPreference;
    @Mock
    private CurrencyFactory currencyFactory;
    @Mock
    private PreferencesUtil preferencesUtil;

    @After
    public void tearDown() {
        currencyFactory = null;
        preferencesUtil = null;
        currencyPreference = null;
    }

    @Test
    public void sets_base_currency_preference() {
        BTCCurrency primary = new BTCCurrency();
        USDCurrency secondary;
        secondary = new USDCurrency();

        currencyPreference.setCurrencies(primary, secondary);

        verify(preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, primary.getSymbol());
        verify(preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, secondary.getSymbol());
    }

    @Test
    public void provides_access_to_currency_preference() {
        when(preferencesUtil.getString(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, BTCCurrency.SYMBOL))
                .thenReturn(BTCCurrency.SYMBOL);
        when(preferencesUtil.getString(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, USDCurrency.SYMBOL))
                .thenReturn(USDCurrency.SYMBOL);

        when(currencyFactory.fromSymbol(BTCCurrency.SYMBOL)).thenReturn(new BTCCurrency());
        when(currencyFactory.fromSymbol(USDCurrency.SYMBOL)).thenReturn(new USDCurrency());

        DefaultCurrencies defaultCurrencies = currencyPreference.getCurrenciesPreference();

        assertThat(defaultCurrencies.getPrimaryCurrency().getSymbol(), equalTo(BTCCurrency.SYMBOL));
        assertThat(defaultCurrencies.getSecondaryCurrency().getSymbol(), equalTo(USDCurrency.SYMBOL));
    }

    @Test
    public void allows_toggling_of_default() {
        when(preferencesUtil.getString(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, BTCCurrency.SYMBOL))
                .thenReturn(BTCCurrency.SYMBOL);
        when(preferencesUtil.getString(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, USDCurrency.SYMBOL))
                .thenReturn(USDCurrency.SYMBOL);

        when(currencyFactory.fromSymbol(BTCCurrency.SYMBOL)).thenReturn(new BTCCurrency());
        when(currencyFactory.fromSymbol(USDCurrency.SYMBOL)).thenReturn(new USDCurrency());

        DefaultCurrencies defaultCurrencies = currencyPreference.toggleDefault();

        verify(preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, USDCurrency.SYMBOL);
        verify(preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, BTCCurrency.SYMBOL);
        assertThat(defaultCurrencies.getPrimaryCurrency().getSymbol(), equalTo(USDCurrency.SYMBOL));
        assertThat(defaultCurrencies.getSecondaryCurrency().getSymbol(), equalTo(BTCCurrency.SYMBOL));
    }
}