package app.dropbit.commons.currency

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.math.BigDecimal

class SatoshiCurrencyTest {

    @Test
    fun init__default() {
        assertThat(SatoshiCurrency().toLong()).isEqualTo(0)
    }

    @Test
    fun init__long() {
        assertThat(SatoshiCurrency(1_000).toLong()).isEqualTo(1_000)
    }

    @Test
    fun init__bigDec() {
        assertThat(SatoshiCurrency(BigDecimal.valueOf(1_000L)).toLong()).isEqualTo(1_000)
    }


    // Formatting

    @Test
    fun formatting___string() {
        val currency = SatoshiCurrency(1_000_000)

        assertThat(currency.toString()).isEqualTo("1000000")
    }

    @Test
    fun formatting___type_as_you_go() {
        val currency = SatoshiCurrency(1_000_000)

        currency.update("1,000,0000 sats")

        assertThat(currency.toFormattedCurrency()).isEqualTo("10,000,000 sats")
    }

    @Test
    fun formatting___as_sats() {
        val currency = SatoshiCurrency(1_000_000)

        assertThat(currency.toFormattedCurrency()).isEqualTo("1,000,000 sats")
    }


    // Conversion

    @Test
    fun conversion__sats() {
        val evaluationCurrency = USDCurrency(10_500_00)
        val currency = SatoshiCurrency(1_000_000)

        assertThat(currency.toSats(evaluationCurrency).toLong()).isEqualTo(currency.toLong())
    }

    @Test
    fun conversion__toUSD() {
        val evaluationCurrency = USDCurrency(10_500_00)

        val btc = BTCCurrency(1_000_000)
        val sats = SatoshiCurrency(1_000_000)

        assertThat(sats.toUSD(evaluationCurrency).toLong()).isEqualTo(btc.toUSD(evaluationCurrency).toLong())
    }

    @Test
    fun conversion__btc() {
        val evaluationCurrency = USDCurrency(10_500_00)
        val currency = SatoshiCurrency(1_000_000)

        assertThat(currency.toBTC(evaluationCurrency).toLong()).isEqualTo(currency.toLong())
    }

}