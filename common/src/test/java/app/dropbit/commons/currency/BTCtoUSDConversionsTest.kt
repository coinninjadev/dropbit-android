package app.dropbit.commons.currency

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class BTCtoUSDConversionsTest {
    private val evaluationCurrency: Currency = USDCurrency(6603_30)

    /**
     * Given USD: 0
     * Given BTC: .01976285
     */
    @Test
    fun convertsBTCtoUSD() {
        val btcCurrency = BTCCurrency(0.01976285)
        val usdCurrency = btcCurrency.toUSD(evaluationCurrency)
        assertThat(usdCurrency.toFormattedCurrency(), equalTo("$130.50"))
    }

    /**
     * Given USD: 130
     * Given BTC: 0
     */
    @Test
    fun convertsUSDtoBTC() {
        val usdCurrency = USDCurrency(130.5)
        val btcCurrency = usdCurrency.toBTC(evaluationCurrency)
        assertThat(btcCurrency.toLong(), equalTo(1976285L))
    }

    /**
     * eval usd 6612.70
     * Given USD: 3
     * Given BTC: .00045367
     */
    @Test
    fun roundingIssueUSDtoBTC() {
        val eval = USDCurrency(6612.7)
        val btcCurrency = BTCCurrency(.00045367)
        val usdCurrency = btcCurrency.toUSD(eval)
        assertThat(usdCurrency.toFormattedCurrency(), equalTo("$3.00"))
    }
}