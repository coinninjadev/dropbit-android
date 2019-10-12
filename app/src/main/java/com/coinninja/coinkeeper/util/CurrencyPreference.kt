package com.coinninja.coinkeeper.util

import android.content.Intent
import app.dropbit.annotations.Mockable
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.Currency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.factory.CurrencyFactory
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.coinninja.coinkeeper.util.android.PreferencesUtil
import javax.inject.Inject

@Mockable
class CurrencyPreference @Inject constructor(internal val preferencesUtil: PreferencesUtil,
                                             internal val currencyFactory: CurrencyFactory,
                                             internal val localBroadCastUtil: LocalBroadCastUtil) {


    val currenciesPreference: DefaultCurrencies
        get() {
            val primarySymbol = preferencesUtil.getString(PREFERENCE_PRIMARY_CURRENCY, USDCurrency.SYMBOL)
            val secondarySymbol = preferencesUtil.getString(PREFERENCE_SECONDARY_CURRENCY, BTCCurrency.SYMBOL)
            val primaryCurrency = currencyFactory.fromSymbol(primarySymbol)
            val secondaryCurrency = currencyFactory.fromSymbol(secondarySymbol)
            return DefaultCurrencies(primaryCurrency, secondaryCurrency)
        }

    val fiat: Currency get() = currenciesPreference.fiat

    fun reset() = currencyFactory.reset()

    fun toggleDefault(): DefaultCurrencies {
        val defaultCurrencies = currenciesPreference
        setCurrencies(defaultCurrencies.secondaryCurrency, defaultCurrencies.primaryCurrency)
        return DefaultCurrencies(defaultCurrencies.secondaryCurrency, defaultCurrencies.primaryCurrency)
    }

    internal fun setCurrencies(primary: Currency, secondary: Currency) {
        preferencesUtil.savePreference(PREFERENCE_PRIMARY_CURRENCY, primary.symbol)
        preferencesUtil.savePreference(PREFERENCE_SECONDARY_CURRENCY, secondary.symbol)
        val intent = Intent(DropbitIntents.ACTION_CURRENCY_PREFERENCE_CHANGED)
        intent.putExtra(DropbitIntents.EXTRA_PREFERENCE, currenciesPreference)
        localBroadCastUtil.sendBroadcast(intent)
    }

    companion object {
        internal const val PREFERENCE_PRIMARY_CURRENCY = "primaryCurrency"
        internal const val PREFERENCE_SECONDARY_CURRENCY = "secondaryCurrency"
    }
}
