package app.dropbit.commons.currency

import app.dropbit.commons.currency.USDCurrency.Companion.setMaxLimit
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.*
import org.junit.Test

class USDCurrencyTest {
    @Test
    fun sets_max_value_statically() {
        setMaxLimit(USDCurrency(0))
        assertThat(USDCurrency.maxLongValue, equalTo(Long.MAX_VALUE))
        setMaxLimit(USDCurrency(10000))
        assertThat(USDCurrency.maxLongValue, equalTo(209999999769L))
    }

    @Test
    fun instantiated_from_long() {
        assertThat(USDCurrency(100L).toFormattedCurrency(), equalTo("$1.00"))
    }

    @Test
    fun update_valid() {
        val currency = USDCurrency(100L)
        val formattedValue = "$1,99$,,9.99"

        assertTrue(currency.update(formattedValue))

        assertThat(currency.toLong(), equalTo(199999L))
    }

    @Test
    fun update_invalid() {
        val initialValue = 100L
        val currency = USDCurrency(initialValue)
        val formattedValue = "$1,999.999"
        assertFalse(currency.update(formattedValue))
        assertThat(currency.toLong(), equalTo(initialValue))
    }

    @Test
    fun validate() {
        val currency = USDCurrency(100L)
        assertTrue(currency.validate("$1,99$,,9.99"))
        assertTrue(currency.validate("$1,999.99"))
        assertTrue(currency.validate("1999.99"))
        assertFalse("overflow", currency.validate("$1,999,999,999,999,999,999,999,999,999,999.99"))
        assertFalse(currency.validate("$1,999.999"))
        assertFalse(currency.validate("$1.999.99"))
    }

    @Test
    fun instantiated_from_long_zero() {
        val usdCurrency = USDCurrency(0L)
        assertThat(usdCurrency.toFormattedCurrency(), equalTo("$0.00"))
        assertTrue(usdCurrency.isZero)
    }

    @Test
    fun converts_to_long() {
        assertThat(USDCurrency(100L).toLong(), equalTo(100L))
    }

    // Conversions
    @Test
    fun canConvertToUSD() {
        val currency = USDCurrency(100_00L)
        val conversionValue = USDCurrency(6200_00L)
        assertThat(currency.toUSD(conversionValue).toFormattedCurrency(), equalTo("$100.00"))
    }

    @Test
    fun canConvertToBTC() {
        val usdCurrency = USDCurrency(100_00L)
        val currency = USDCurrency(6200_00L)
        assertThat(usdCurrency.toBTC(currency).toLong(), equalTo(1612903L))
    }

    @Test
    fun canConvertToSats() {
        val usdCurrency = USDCurrency(100_00L)

        val currency = USDCurrency(6200_00L)

        assertThat(usdCurrency.toSats(currency).toLong(), equalTo(1612903L))
    }

    @Test
    fun canConvertToAWholeBTC() {
        var usdCurrency = USDCurrency(6993.17)
        var eval = USDCurrency(6993.17)
        assertThat(usdCurrency.toBTC(eval).toLong(), equalTo(100000000L))
        usdCurrency = USDCurrency(7040.22)
        eval = USDCurrency(7040.22)
        assertThat(usdCurrency.toBTC(eval).toLong(), equalTo(100000000L))
    }

    // Formatting
    @Test
    fun hasAFormat() {
        assertThat(USDCurrency().format, equalTo("#,###.##"))
    }

    @Test
    fun hasCurrencyFormat() {
        assertThat(USDCurrency().currencyFormat, equalTo("$#,##0.00"))
    }

    @Test
    fun usdHasASymbol() {
        assertThat(USDCurrency().symbol, equalTo("$"))
    }

    // Initialization
    @Test
    fun initEmpty() {
        assertThat(USDCurrency().toFormattedCurrency(), equalTo("$0.00"))
    }

    @Test
    fun initFromLong() {
        assertThat(USDCurrency(100L).toFormattedCurrency(), equalTo("$1.00"))
    }

    @Test
    fun initFromDouble() {
        assertThat(USDCurrency(100.0).toFormattedCurrency(), equalTo("$100.00"))
    }
}