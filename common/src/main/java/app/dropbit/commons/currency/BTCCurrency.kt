package app.dropbit.commons.currency

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import app.dropbit.commons.R.drawable
import java.math.BigDecimal
import java.math.RoundingMode

class BTCCurrency : BaseCurrency, CryptoCurrency, Parcelable {
    override val symbol: String = BTCCurrency.symbol
    override val maxNumSubValues: Int = SUB_NUM_MAX
    override val maxNumWholeValues: Int = 8
    override val format: String = CryptoCurrency.NO_SYMBOL_FORMAT
    override val incrementalFormat: String = CryptoCurrency.NO_SYMBOL_FORMAT
    override var currencyFormat = DEFAULT_CURRENCY_FORMAT

    override val maxLongValue: Long get() = BTCCurrency.maxLongValue

    constructor()
    constructor(initialValue: Long) : this() {
        value = scale(BigDecimal(initialValue).movePointLeft(maxNumSubValues))
    }

    constructor(initialValue: Double) : this() {
        value = BigDecimal(initialValue).stripTrailingZeros().setScale(maxNumSubValues, RoundingMode.HALF_UP)
    }

    constructor(initialValue: BigDecimal) : this() {
        value = initialValue.stripTrailingZeros().setScale(maxNumSubValues, RoundingMode.HALF_UP)
    }

    internal constructor(parcel: Parcel) : this(parcel.readLong())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(toLong())
    }

    override fun isValid(): Boolean {
        return toLong() >= 0 && toLong() <= this.maxLongValue
    }

    fun toUriFormattedString(): String {
        val btc = BTCCurrency(toLong())
        btc.currencyFormat = "#,##0.00000000"
        return btc.toFormattedCurrency()
    }

    override fun toUSD(conversionValue: Currency): USDCurrency {
        if (conversionValue.isZero) {
            return USDCurrency()
        }
        return USDCurrency(toBigDecimal().multiply(conversionValue.toBigDecimal()))
    }

    override fun toSats(conversionValue: Currency): SatoshiCurrency {
        return SatoshiCurrency(toLong())
    }

    override fun toBTC(conversionValue: Currency): BTCCurrency {
        return this
    }

    override fun getSymbolDrawable(context: Context): Drawable? {
        return context.getDrawable(drawable.ic_btc_icon)
    }

    override fun toFiat(conversionFiat: FiatCurrency): FiatCurrency {
        return toUSD(conversionFiat)
    }


    companion object {
        const val SUB_NUM_MAX: Int = 8
        const val ALT_CURRENCY_FORMAT = "#,##0.00000000 BTC"
        const val symbol = "\u20BF"
        const val maxLongValue = 2099999997690000L

        @JvmField
        val CREATOR: Creator<BTCCurrency?> = object : Creator<BTCCurrency?> {
            override fun createFromParcel(parcel: Parcel): BTCCurrency {
                return BTCCurrency(parcel)
            }

            override fun newArray(size: Int): Array<BTCCurrency?> {
                return arrayOfNulls<BTCCurrency?>(size)
            }
        }
        private val DEFAULT_CURRENCY_FORMAT = String.format("%s #,##0.########", symbol)
        const val format = "#,##0.########"
    }
}