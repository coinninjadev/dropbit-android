package app.dropbit.commons.currency

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols

class USDCurrency : BaseCurrency, FiatCurrency, Currency, Parcelable {

    constructor() : super()

    constructor(initialValue: Long) : this() {
        value = scale(BigDecimal(initialValue).movePointLeft(maxNumSubValues))
    }

    constructor(initialValue: Double) : this() {
        value = BigDecimal(initialValue).stripTrailingZeros().setScale(this.maxNumSubValues, RoundingMode.HALF_UP)
    }

    constructor(initialValue: BigDecimal) : this() {
        value = initialValue.setScale(maxNumSubValues, RoundingMode.HALF_UP).stripTrailingZeros()
    }

    init {
        decimalSeparator = DecimalFormatSymbols.getInstance().decimalSeparator.toString()
        value = value.stripTrailingZeros()
    }

    internal constructor(parcel: Parcel) : this(parcel.readLong())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(toLong())
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as USDCurrency

        if (toLong() != other.toLong()) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * toLong().toInt()
    }

    override val symbol: String = USDCurrency.symbol
    override val format = "#,###.##"
    override val incrementalFormat: String = "$#,##0.##"
    override var currencyFormat = "$#,##0.00"
    override val maxNumSubValues: Int = 2
    override val maxNumWholeValues: Int = 10
    public override val maxLongValue: Long get() = USDCurrency.maxLongValue


    override fun isValid(): Boolean = toLong() <= maxLongValue

    override fun toBTC(conversionValue: Currency): BTCCurrency {
        if (conversionValue.isZero) {
            return BTCCurrency()
        }

        return BTCCurrency(toBigDecimal().divide(conversionValue.toBigDecimal(),
                BTCCurrency.SUB_NUM_MAX, RoundingMode.HALF_UP))
    }

    override fun toUSD(conversionValue: Currency): USDCurrency {
        return this
    }

    override fun toSats(conversionValue: Currency): SatoshiCurrency {
        if (conversionValue.isZero) {
            return SatoshiCurrency()
        }

        return SatoshiCurrency(toBTC(conversionValue).toLong())
    }


    companion object {
        const val symbol = "$"

        @JvmField
        var maxLongValue = Long.MAX_VALUE

        @JvmField
        val CREATOR: Creator<USDCurrency?> = object : Creator<USDCurrency?> {
            override fun createFromParcel(parcel: Parcel): USDCurrency {
                return USDCurrency(parcel)
            }

            override fun newArray(size: Int): Array<USDCurrency?> {
                return arrayOfNulls<USDCurrency?>(size)
            }
        }

        @JvmStatic
        fun setMaxLimit(currency: USDCurrency) {
            try {
                maxLongValue = BTCCurrency(BTCCurrency.maxLongValue).toUSD(currency).toLong()
            } catch (ex: FormatNotValidException) {
                maxLongValue = Long.MAX_VALUE
            }
            if (maxLongValue == 0L) {
                maxLongValue = Long.MAX_VALUE
            }
        }
    }
}