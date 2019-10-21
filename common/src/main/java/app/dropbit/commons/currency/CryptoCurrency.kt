package app.dropbit.commons.currency

import android.content.Context
import android.graphics.drawable.Drawable

interface CryptoCurrency : Currency {
    fun getSymbolDrawable(context: Context): Drawable?
    fun toFiat(conversionFiat: FiatCurrency): FiatCurrency

    companion object {
        const val NO_SYMBOL_FORMAT = "#,##0.########"
    }
}