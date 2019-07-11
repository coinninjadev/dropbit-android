package com.coinninja.coinkeeper.ui.market

import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.github.mikephil.charting.data.Entry
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketChartsFragmentTest {

    @Test
    fun `requests granularity when clicked`() {
        createScenario().onFragment { fragment ->
            fragment.findViewById<Button>(R.id.granularity_day)!!.performClick()
            verify(fragment.marketDataViewModel, times(2)).loadGranularity(Granularity.DAY)

            fragment.findViewById<Button>(R.id.granularity_week)!!.performClick()
            verify(fragment.marketDataViewModel).loadGranularity(Granularity.WEEK)

            fragment.findViewById<Button>(R.id.granularity_month)!!.performClick()
            verify(fragment.marketDataViewModel).loadGranularity(Granularity.MONTH)

            fragment.findViewById<Button>(R.id.granularity_year)!!.performClick()
            verify(fragment.marketDataViewModel).loadGranularity(Granularity.YEAR)

            fragment.findViewById<Button>(R.id.granularity_all)!!.performClick()
            verify(fragment.marketDataViewModel).loadGranularity(Granularity.ALL)
        }

    }

    @Test
    fun `observes granularity changes`() {
        createScenario().onFragment { fragment ->
            verify(fragment.marketDataViewModel.currentGranularity).observe(fragment, fragment.currentGranularityObserver)
        }
    }

    @Test
    fun `loads granularity when started`() {
        createScenario().onFragment { fragment ->
            verify(fragment.marketDataViewModel).loadGranularity(fragment.currentGranularity)
        }
    }

    @Test
    fun `retains state`() {
        val scenario = createScenario()
        scenario.onFragment { fragment ->
            fragment.currentGranularity = Granularity.YEAR
        }

        scenario.recreate()

        scenario.onFragment { fragment ->
            assertThat(fragment.currentGranularity).isEqualTo(Granularity.YEAR)
            verify(fragment.marketDataViewModel).loadGranularity(Granularity.YEAR)
        }
    }

    @Test
    fun `observes period data changes`() {
        createScenario().onFragment { fragment ->
            verify(fragment.marketDataViewModel.periodData).observe(fragment, fragment.periodDataObserver)
        }
    }

    @Test
    fun `observes price changes`() {
        createScenario().onFragment { fragment ->
            verify(fragment.marketDataViewModel.currentBtcPrice).observe(fragment, fragment.btcPriceObserver)
            verify(fragment.marketDataViewModel).loadCurrentPrice()
        }
    }

    @Test
    fun `updates price when price changes`() {
        createScenario().onFragment { fragment ->
            val price = fragment.findViewById<TextView>(R.id.price)!!.also {
                it.text = "$12,000.00"
            }

            fragment.btcPriceObserver.onChanged(USDCurrency(12450.00))

            assertThat(price.text).isEqualTo("$12,450.00")
        }
    }

    @Test
    fun `updates price movement appropriately`() {
        createScenario().onFragment { fragment ->
            fragment.updatePriceMovement(mutableListOf(Entry(0F, 950F), Entry(1F, 1000F)))
            assertThat(fragment.findViewById<TextView>(R.id.market_sentiment)!!.text).isEqualTo("$50.00 (5.26%)")

            fragment.updatePriceMovement(mutableListOf(Entry(0F, 1000F), Entry(1F, 950F)))
            assertThat(fragment.findViewById<TextView>(R.id.market_sentiment)!!.text).isEqualTo("-$50.00 (5.00%)")
        }
    }

    private fun createScenario(): FragmentScenario<MarketChartsFragment> {
        return FragmentScenario.launch(MarketChartsFragment::class.java)
    }
}