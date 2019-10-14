package app.dropbit.commons.currency

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

abstract class BaseCurrency : Currency {
    protected var value: BigDecimal = BigDecimal.valueOf(0L)
    private val stripPattern: Regex get() = "[^0-9 $decimalSeparator]|[$decimalSeparator](?!\\d)".toRegex()
    var decimalSeparator: String = DecimalFormatSymbols.getInstance().decimalSeparator.toString()

    override fun toLong(): Long {
        return value.movePointRight(maxNumSubValues).stripTrailingZeros()
                .setScale(maxNumSubValues, RoundingMode.HALF_UP).toLong()
    }

    override fun toFormattedString(): String {
        return DecimalFormat(format).format(value)
    }

    override fun toFormattedCurrency(): String {
        return DecimalFormat(currencyFormat).format(value.setScale(maxNumSubValues, RoundingMode.HALF_UP))
    }

    override fun toBigDecimal(): BigDecimal {
        return value
    }

    override fun update(formattedValue: String): Boolean {
        if (validate(formattedValue)) {
            value = fromFormattedDecimalString(formattedValue)
            return true
        }
        return false
    }

    override fun validate(candidate: String): Boolean {
        return try {
            val bigDecimal = scrubInput(candidate)
            val longValue = bigDecimal.movePointRight(maxNumSubValues).longValueExact()
            longValue in 0 until maxLongValue
        } catch (e: Exception) {
            false
        }
    }

    override val isZero: Boolean
        get() {
            val bigDecimal = toBigDecimal()
            return bigDecimal.compareTo(BigDecimal.ZERO) == 0
        }

    override val isCrypto: Boolean
        get() = this is CryptoCurrency

    override val isFiat: Boolean
        get() = this is FiatCurrency

    override fun toString(): String {
        return value.setScale(maxNumSubValues, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    override fun zero() {
        value = BigDecimal.valueOf(0L)
    }

    protected fun scale(initial: BigDecimal): BigDecimal {
        return initial.stripTrailingZeros().setScale(maxNumSubValues, RoundingMode.HALF_DOWN)
    }

    protected fun fromFormattedDecimalString(initialValue: String): BigDecimal {
        val scrubbed = scrubInput(initialValue)
        scale(scrubbed)
        return scrubbed
    }

    protected fun scrubInput(initialValue: String): BigDecimal {
        var input: String = initialValue.replace(symbol, "").trim()
        input = input.replace(stripPattern, "")
        input = input.replace(decimalSeparator, ".").trim()

        return if (input.isEmpty())
            BigDecimal(0L)
        else
            BigDecimal(input)
    }

    protected abstract val maxLongValue: Long


}