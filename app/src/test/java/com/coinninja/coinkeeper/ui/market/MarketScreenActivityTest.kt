package com.coinninja.coinkeeper.ui.market

import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketScreenActivityTest {
    @Test
    fun requests_granularity_when_clicked() {
        createScenario().onActivity { activity ->
            activity.findViewById<Button>(R.id.granularity_day)!!.performClick()
            verify(activity.marketDataViewModel, times(2)).loadGranularity(Granularity.DAY)

            activity.findViewById<Button>(R.id.granularity_week)!!.performClick()
            verify(activity.marketDataViewModel).loadGranularity(Granularity.WEEK)

            activity.findViewById<Button>(R.id.granularity_month)!!.performClick()
            verify(activity.marketDataViewModel).loadGranularity(Granularity.MONTH)

            activity.findViewById<Button>(R.id.granularity_year)!!.performClick()
            verify(activity.marketDataViewModel).loadGranularity(Granularity.YEAR)

            activity.findViewById<Button>(R.id.granularity_all)!!.performClick()
            verify(activity.marketDataViewModel).loadGranularity(Granularity.ALL)
        }

    }

    @Test
    fun observes_granularity_changes() {
        createScenario().onActivity { activity ->
            verify(activity.marketDataViewModel.currentGranularity).observe(activity, activity.currentGranularityObserver)
        }
    }

    @Test
    fun loads_granularity_when_started() {
        createScenario().onActivity { activity ->
            verify(activity.marketDataViewModel).loadGranularity(activity.currentGranularity)
        }
    }

    @Test
    fun retains_state() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.currentGranularity = Granularity.YEAR
        }

        scenario.recreate()

        scenario.onActivity { activity ->
            assertThat(activity.currentGranularity).isEqualTo(Granularity.YEAR)
            verify(activity.marketDataViewModel).loadGranularity(Granularity.YEAR)
        }
    }

    @Test
    fun observes_period_data_changes() {
        createScenario().onActivity { activity ->
            verify(activity.marketDataViewModel.periodData).observe(activity, activity.periodDataObserver)
        }
    }

    @Test
    fun observes_price_changes() {
        createScenario().onActivity { activity ->
            verify(activity.marketDataViewModel.currentBtcPrice).observe(activity, activity.btcPriceObserver)
            verify(activity.marketDataViewModel).loadCurrentPrice()
        }
    }

    @Test
    fun updates_price_when_price_changes() {
        createScenario().onActivity { activity ->
            val price = activity.findViewById<TextView>(R.id.price)!!.also {
                it.text = "$12,000.00"
            }

            activity.btcPriceObserver.onChanged(USDCurrency(12450.00))

            assertThat(price.text).isEqualTo("$12,450.00")
        }
    }

    @Test
    fun updates_price_movement_appropriately() {
        createScenario().onActivity { activity ->
            activity.updatePriceMovement(ChartStats(0F, 0F, 950F, 1000F))
            assertThat(activity.findViewById<TextView>(R.id.market_sentiment)!!.text).isEqualTo("$50.00 (5.26%)")

            activity.updatePriceMovement(ChartStats(0F, 0F, 1000F, 950F))
            assertThat(activity.findViewById<TextView>(R.id.market_sentiment)!!.text).isEqualTo("-$50.00 (5%)")
        }
    }

    @Test
    fun sets_up_list() {
        createScenario().onActivity {
            it.findViewById<RecyclerView>(R.id.news)!!.also { view ->
                assertThat(view.adapter).isEqualTo(it.newsAdapter)
                assertThat(view.layoutManager).isNotNull()
            }
        }
    }

    @Test
    fun fetches_first_page_of_news_articles() {
        createScenario().onActivity {
            val orderOperations = inOrder(it.newsAdapter, it.newsViewModel, it.newsViewModel.articles)

            orderOperations.verify(it.newsAdapter).clearArticles()
            orderOperations.verify(it.newsViewModel.articles).observe(it, it.articleChangeObserver)
            orderOperations.verify(it.newsViewModel).fetchNews(0)
        }
    }

    @Test
    fun fetches_second_page_of_news_articles() {
        createScenario().onActivity {
            whenever(it.newsAdapter.itemCount).thenReturn(5)

            it.loadNewsArticles()

            verify(it.newsViewModel).fetchNews(5)
        }
    }

    @Test
    fun clears_news_articles_when_pulling_to_refresh() {
        createScenario().onActivity { fragment ->
            fragment.onRefreshNews()

            verify(fragment.newsAdapter, times(2)).clearArticles()
            verify(fragment.newsViewModel, times(2)).fetchNews(0)
            assertThat(fragment.isLoading).isEqualTo(true)
        }
    }

    @Test
    fun sets_page_size_after_pages_loaded() {
        createScenario().onActivity {
            val articles: List<NewsArticle> = listOf(mock(), mock())
            whenever(it.newsAdapter.itemCount).thenReturn(articles.size)

            it.articleChangeObserver.onChanged(articles)

            assertThat(it.pageSize).isEqualTo(articles.size)
            assertThat(it.isLoading).isEqualTo(false)
        }
    }

    private fun createScenario(): ActivityScenario<MarketScreenActivity> {
        return ActivityScenario.launch(MarketScreenActivity::class.java)
    }

}