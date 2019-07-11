package com.coinninja.coinkeeper.util

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.matchers.IntentMatcher.equalTo
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class CurrencyPreferenceTest {
    private fun createCurrencyPreference(): CurrencyPreference = CurrencyPreference(mock(), mock(), mock())

    @Test
    fun sets_base_currency_preference() {
        val primary = BTCCurrency()
        val secondary: USDCurrency
        secondary = USDCurrency()
        val currencyPreference = createCurrencyPreference()

        currencyPreference.setCurrencies(primary, secondary)

        verify(currencyPreference.preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, primary.symbol)
        verify(currencyPreference.preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, secondary.symbol)
    }

    /**/
    @Test
    fun provides_access_to_currency_preference() {
        val currencyPreference = createCurrencyPreference()
        whenever(currencyPreference.preferencesUtil.getString(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, USDCurrency.SYMBOL))
                .thenReturn(USDCurrency.SYMBOL)
        whenever(currencyPreference.preferencesUtil.getString(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, BTCCurrency.SYMBOL))
                .thenReturn(BTCCurrency.SYMBOL)

        whenever(currencyPreference.currencyFactory.fromSymbol(BTCCurrency.SYMBOL)).thenReturn(BTCCurrency())
        whenever(currencyPreference.currencyFactory.fromSymbol(USDCurrency.SYMBOL)).thenReturn(USDCurrency())

        val defaultCurrencies = currencyPreference.currenciesPreference

        assertThat(defaultCurrencies.primaryCurrency.symbol, equalTo(USDCurrency.SYMBOL))
        assertThat(defaultCurrencies.secondaryCurrency.symbol, equalTo(BTCCurrency.SYMBOL))
    }

    @Test
    fun allows_toggling_of_default() {
        val currencyPreference = createCurrencyPreference()
        whenever(currencyPreference.preferencesUtil.getString(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, USDCurrency.SYMBOL))
                .thenReturn(BTCCurrency.SYMBOL)
        whenever(currencyPreference.preferencesUtil.getString(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, BTCCurrency.SYMBOL))
                .thenReturn(USDCurrency.SYMBOL)

        whenever(currencyPreference.currencyFactory.fromSymbol(BTCCurrency.SYMBOL)).thenReturn(BTCCurrency())
        whenever(currencyPreference.currencyFactory.fromSymbol(USDCurrency.SYMBOL)).thenReturn(USDCurrency())

        val defaultCurrencies = currencyPreference.toggleDefault()

        verify(currencyPreference.preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, USDCurrency.SYMBOL)
        verify(currencyPreference.preferencesUtil).savePreference(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, BTCCurrency.SYMBOL)
        assertThat(defaultCurrencies.primaryCurrency.symbol, equalTo(USDCurrency.SYMBOL))
        assertThat(defaultCurrencies.secondaryCurrency.symbol, equalTo(BTCCurrency.SYMBOL))
    }

    @Test
    fun dispatches_local_notification_that_preference_changed() {
        val currencyPreference = createCurrencyPreference()
        val argumentCaptor = ArgumentCaptor.forClass(Intent::class.java)
        val defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        val expected = Intent(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED)
        expected.putExtra(DropbitIntents.EXTRA_PREFERENCE, defaultCurrencies)
        whenever(currencyPreference.preferencesUtil.getString(CurrencyPreference.PREFERENCE_PRIMARY_CURRENCY, USDCurrency.SYMBOL))
                .thenReturn(USDCurrency.SYMBOL)
        whenever(currencyPreference.preferencesUtil.getString(CurrencyPreference.PREFERENCE_SECONDARY_CURRENCY, BTCCurrency.SYMBOL))
                .thenReturn(BTCCurrency.SYMBOL)
        whenever(currencyPreference.currencyFactory.fromSymbol(BTCCurrency.SYMBOL)).thenReturn(BTCCurrency())
        whenever(currencyPreference.currencyFactory.fromSymbol(USDCurrency.SYMBOL)).thenReturn(USDCurrency())

        currencyPreference.setCurrencies(defaultCurrencies.primaryCurrency, defaultCurrencies.secondaryCurrency)

        verify(currencyPreference.localBroadCastUtil).sendBroadcast(argumentCaptor.capture())
        val intent = argumentCaptor.value
        assertThat(intent, equalTo(expected))
    }

}