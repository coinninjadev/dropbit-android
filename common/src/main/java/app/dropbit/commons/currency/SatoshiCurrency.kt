package app.dropbit.commons.currency

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.math.BigDecimal
import java.math.RoundingMode

class SatoshiCurrency : BaseCurrency, CryptoCurrency, Parcelable {
    override val symbol: String = SatoshiCurrency.symbol
    override val maxNumSubValues: Int = SUB_NUM_MAX
    override val maxNumWholeValues: Int = 16
    override val format: String = "#,##0"
    override var currencyFormat = "$format$symbol"
    override val incrementalFormat: String = currencyFormat

    override val maxLongValue: Long get() = BTCCurrency.maxLongValue

    constructor()
    constructor(initialValue: Long) : this() {
        value = scale(BigDecimal(initialValue).movePointLeft(maxNumSubValues))
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

    override fun getSymbolDrawable(context: Context): Drawable? = null

    override fun toFiat(conversionFiat: FiatCurrency): FiatCurrency {
        return toUSD(conversionFiat)
    }

    override fun toUSD(conversionValue: Currency): USDCurrency {
        if (conversionValue.isZero) {
            return USDCurrency()
        }
        return USDCurrency(toBigDecimal().movePointLeft(BTCCurrency.SUB_NUM_MAX).multiply(conversionValue.toBigDecimal()))
    }

    override fun toSats(conversionValue: Currency): SatoshiCurrency {
        return this
    }

    override fun toBTC(conversionValue: Currency): BTCCurrency {
        return BTCCurrency(toLong())
    }

    companion object {
        const val SUB_NUM_MAX: Int = 0
        const val symbol = " sats"

        @JvmField
        val CREATOR: Creator<SatoshiCurrency?> = object : Creator<SatoshiCurrency?> {
            override fun createFromParcel(parcel: Parcel): SatoshiCurrency {
                return SatoshiCurrency(parcel)
            }

            override fun newArray(size: Int): Array<SatoshiCurrency?> {
                return arrayOfNulls<SatoshiCurrency?>(size)
            }
        }
    }
}