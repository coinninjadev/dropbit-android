package app.dropbit.commons.currency

import android.os.Parcelable
import java.math.BigDecimal

interface Currency : Parcelable {
    val isZero: Boolean
    val isCrypto: Boolean
    val isFiat: Boolean
    val symbol: String
    val format: String
    var currencyFormat: String
    val maxNumSubValues: Int
    val maxNumWholeValues: Int
    val incrementalFormat: String

    fun isValid(): Boolean
    fun toLong(): Long
    fun zero()
    fun toFormattedString(): String
    fun toFormattedCurrency(): String
    fun toBigDecimal(): BigDecimal
    fun toBTC(conversionValue: Currency): BTCCurrency
    fun toUSD(conversionValue: Currency): USDCurrency
    fun toSats(conversionValue: Currency): SatoshiCurrency
    fun update(formattedValue: String): Boolean
    fun validate(candidate: String): Boolean
}