package com.coinninja.coinkeeper.ui.market

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.dropbit.commons.util.decimalFormatted
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.HistoricalPriceRecord
import com.coinninja.coinkeeper.service.client.model.NewsArticle
import com.coinninja.coinkeeper.ui.base.BaseActivity
import com.coinninja.coinkeeper.ui.news.NewsAdapter
import com.coinninja.coinkeeper.ui.news.NewsViewModel
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.util.currency.asFormattedUsdCurrencyString
import com.coinninja.coinkeeper.viewModel.MarketDataViewModel
import com.github.mikephil.charting.charts.LineChart
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class MarketScreenActivity : BaseActivity() {

    @Inject
    lateinit var marketChartController: MarketChartController

    @Inject
    lateinit var marketDataViewModel: MarketDataViewModel

    @Inject
    lateinit var marketChartButtonBarController: MarketChartButtonBarController

    @Inject
    lateinit var newsAdapter: NewsAdapter

    @Inject
    lateinit var newsViewModel: NewsViewModel

    internal var currentGranularity: Granularity = Granularity.DAY

    internal val currentGranularityObserver = Observer<Granularity> { granularity ->
        currentGranularity = granularity
        marketChartButtonBarController.onGranularityChanged(currentGranularity, granularityBar)
    }

    var isLoading: Boolean = true
    var pageSize: Int = 0
    val articleChangeObserver = Observer<List<NewsArticle>> { loadedArticles ->
        findViewById<SwipeRefreshLayout>(R.id.news_pull_to_refresh)?.apply { isRefreshing = false }
        newsAdapter.addArticles(loadedArticles)
        pageSize = newsAdapter.itemCount
        isLoading = false
    }

    val onScrollChangedObserver = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            findViewById<RecyclerView>(R.id.news)?.also {
                it.layoutManager?.let { layoutManager ->
                    val linearLayoutManager = layoutManager as LinearLayoutManager
                    val visibleItemCount = linearLayoutManager.childCount
                    val totalItemCount = linearLayoutManager.itemCount
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()

                    if (!isLoading) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                                && firstVisibleItemPosition >= 0
                                && totalItemCount >= pageSize - 10) {
                            loadNewsArticles()
                        }
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
        }
    }

    internal val btcPriceObserver = Observer<USDCurrency> { price ->
        findViewById<TextView>(R.id.price)?.apply {
            text = price.toFormattedCurrency()
        }
    }

    internal val periodDataObserver = Observer<List<HistoricalPriceRecord>> { prices ->
        val chartStats = marketChartController.renderPeriodData(lineChart, marketChartController.getDataForGranularity(currentGranularity, prices.reversed()))
        updatePriceMovement(chartStats)
        updateMinMax(chartStats)
    }


    internal val granularityBar: ViewGroup? get() = findViewById(R.id.market_granularity)
    internal val lineChart: LineChart? get() = findViewById(R.id.chart)
    internal val marketSentiment: TextView? get() = findViewById(R.id.market_sentiment)
    internal val marketHigh: TextView? get() = findViewById(R.id.market_high)
    internal val marketLow: TextView? get() = findViewById(R.id.market_low)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_market)
        marketChartController.configureChart(lineChart)
        setLayoutDescription(R.xml.motion_scene_charts)
    }


    override fun onResume() {
        super.onResume()
        marketChartButtonBarController.setupButtonBar(currentGranularity, granularityBar, marketDataViewModel)
        marketDataViewModel.currentGranularity.observe(this, currentGranularityObserver)
        marketDataViewModel.currentBtcPrice.observe(this, btcPriceObserver)
        marketDataViewModel.periodData.observe(this, periodDataObserver)
        marketDataViewModel.loadGranularity(currentGranularity)
        marketDataViewModel.loadCurrentPrice()
        setupNewsList()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.let {
            currentGranularity = Granularity.from(savedInstanceState.getInt("GRANULARITY", 0))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("GRANULARITY", currentGranularity.ord)
    }

    internal fun loadNewsArticles() {
        isLoading = true
        newsViewModel.fetchNews(newsAdapter.itemCount)
    }

    fun onRefreshNews() {
        newsAdapter.clearArticles()
        pageSize = 0
        loadNewsArticles()
    }

    private fun setupNewsList() {
        findViewById<RecyclerView>(R.id.news)?.apply {
            val layoutManager = LinearLayoutManager(context)
            this.layoutManager = layoutManager
            addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
            setHasFixedSize(false)
            adapter = newsAdapter
            setOnScrollListener(onScrollChangedObserver)
        }
        findViewById<SwipeRefreshLayout>(R.id.news_pull_to_refresh)?.apply {
            setOnRefreshListener { onRefreshNews() }
        }

        newsAdapter.clearArticles()
        newsViewModel.articles.observe(this, articleChangeObserver)
        loadNewsArticles()
    }

    internal fun updateMinMax(chartStats: ChartStats) {
        marketLow?.text = chartStats.low.asFormattedUsdCurrencyString()
        marketHigh?.text = chartStats.high.asFormattedUsdCurrencyString()
    }

    internal fun updatePriceMovement(chartStats: ChartStats) {
        val appearance: Int
        val leader: String

        if (chartStats.percentageDifference < 0) {
            appearance = R.style.TextAppearance_Chart_Sentiment_Down
            leader = "-"
        } else {
            appearance = R.style.TextAppearance_Chart_Sentiment_Up
            leader = ""
        }

        val priceDiff = if (chartStats.priceDifference > 0) chartStats.priceDifference else chartStats.priceDifference * -1
        val percentDiff = if (chartStats.percentageDifference > 0) chartStats.percentageDifference else chartStats.percentageDifference * -1

        marketSentiment?.apply {
            text = context.getString(R.string.market_movement_up,
                    leader,
                    USDCurrency(priceDiff.toDouble()).toFormattedCurrency(),
                    BigDecimal(percentDiff.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toDouble().decimalFormatted(),
                    getString(R.string.percentage)
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(appearance)
            } else {
                setTextAppearance(context, appearance)
            }
        }
    }
}
