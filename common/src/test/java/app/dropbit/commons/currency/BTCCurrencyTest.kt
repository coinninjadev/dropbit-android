package app.dropbit.commons.currency

import junit.framework.Assert.assertTrue
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThat
import org.junit.Test
import java.math.BigDecimal

class BTCCurrencyTest {
    @Test
    fun isValid() {
        assertTrue(BTCCurrency(0L).isValid())
        assertTrue(BTCCurrency(1L).isValid())
        assertTrue(BTCCurrency(BTCCurrency.maxLongValue).isValid())
        assertFalse(BTCCurrency(BTCCurrency.maxLongValue + 1).isValid())
        assertFalse(BTCCurrency(-1L).isValid())
    }

    @Test
    fun update_valid() {
        val currency = BTCCurrency(100L)
        val formattedValue = String.format("%s 1,99,,9.99", BTCCurrency.symbol)
        assertTrue(currency.update(formattedValue))
        assertThat(currency.toLong(), equalTo(199999000000L))
    }

    @Test
    fun update_invalid() {
        val initialValue = 100L
        val currency = BTCCurrency(initialValue)
        val formattedValue = String.format("%s 1,999.999999999", BTCCurrency.symbol)
        assertFalse(currency.update(formattedValue))
        assertThat(currency.toLong(), equalTo(initialValue))
    }

    @Test
    fun validate() {
        val currency = BTCCurrency(100L)
        assertTrue(currency.validate("1,99,,9.99"))
        assertTrue(currency.validate("1,999.99"))
        assertTrue(currency.validate("1999.99"))
        assertFalse(currency.validate("1,999.999999999"))
        assertFalse(currency.validate("1.999.99"))
        assertFalse(currency.validate("21,000,000.01"))
    }

    @Test
    fun canConvertToUSD() {
        val currency = BTCCurrency(1.00784009)
        val conversionValue = USDCurrency(6_200_00)
        assertThat(currency.toUSD(conversionValue).toFormattedCurrency(), equalTo("$6,248.61"))
    }

    @Test
    fun currencyClearsFormatting() {
        val currency = BTCCurrency(1000.008)
        assertThat(currency.toFormattedString(), equalTo("1,000.008"))
    }

    @Test
    fun canConvertToBTC() {
        val initialValue = 10000000000L
        val currency = BTCCurrency(initialValue)
        val conversionValue = USDCurrency(6200)
        assertThat(currency.toBTC(conversionValue).toLong(), equalTo(initialValue))
    }

    @Test
    fun canConvertToSats() {
        val initialValue = 10000000000L
        val currency = BTCCurrency(initialValue)

        val conversionValue = USDCurrency(6200)

        assertThat(currency.toSats(conversionValue).toLong(), equalTo(initialValue))
    }

    @Test
    fun canInitFromSatoshi() {
        val currency = BTCCurrency(1880L)
        assertThat(currency.toLong(), equalTo(1880L))
    }

    // Validation
    @Test
    fun validatesSmaller() {
        assertTrue(BTCCurrency(20999999.9769).isValid())
    }

    @Test
    fun validation__zero_is_valid() {
        val btcCurrency = BTCCurrency()
        assertThat(btcCurrency.toLong(), equalTo(0L))
        assertTrue(btcCurrency.isValid())
        assertTrue(btcCurrency.update(""))
    }

    // Formatting
    @Test
    fun can_use_alt_formatting() {
        val currency = BTCCurrency(1880L)
        assertThat(currency.toFormattedCurrency(), equalTo("\u20BF 0.0000188"))
        currency.currencyFormat = BTCCurrency.ALT_CURRENCY_FORMAT
        assertThat(currency.toFormattedCurrency(), equalTo("0.00001880 BTC"))
    }

    // Formatting
    @Test
    fun uri_formatting() {
        var currency = BTCCurrency(1880L)
        assertThat(currency.toUriFormattedString(), equalTo("0.00001880"))

        currency = BTCCurrency(0.0000188)
        assertThat(currency.toUriFormattedString(), equalTo("0.00001880"))
    }

    @Test
    fun usdHasASymbol() {
        assertThat(BTCCurrency().symbol, equalTo("\u20BF"))
    }

    // Initialization
    @Test
    fun initEmpty() {
        assertThat(BTCCurrency().toLong(), equalTo(0L))
    }

    @Test
    fun initFromDouble() {
        assertThat(BTCCurrency(100.0).toLong(), equalTo(10000000000L))
    }

    @Test
    fun initFromBigDecimal() {
        assertThat(BTCCurrency(BigDecimal(100.000457899)).toLong(), equalTo(10000045790L))
    }
}