package com.coinninja.coinkeeper.ui.market

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.HistoricalPriceRecord
import com.coinninja.coinkeeper.ui.base.BaseFragment
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.viewModel.MarketDataViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class MarketChartsFragment : BaseFragment() {
    companion object {
        const val dayPeriodLimit = 1440
        const val weekPeriodLimit = 168
        const val monthPeriodLimit = 720
        const val yearPeriodLimit = 365
    }

    @Inject
    lateinit var marketDataViewModel: MarketDataViewModel

    internal var currentGranularity: Granularity = Granularity.DAY

    internal val currentGranularityObserver = Observer<Granularity> { granularity ->
        clearSelection()
        currentGranularity = granularity
        updateGranularity()
    }

    internal val btcPriceObserver = Observer<USDCurrency> { price ->
        findViewById<TextView>(R.id.price)?.apply {
            text = price.toFormattedCurrency()
        }
    }


    internal val periodDataObserver = Observer<List<HistoricalPriceRecord>> { prices ->
        renderPeriodData(prices.reversed())
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_market_charts, container, false)
        configureChart(view.findViewById<LineChart>(R.id.chart))
        return view
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.granularity_day)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.DAY) }
        findViewById<Button>(R.id.granularity_week)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.WEEK) }
        findViewById<Button>(R.id.granularity_month)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.MONTH) }
        findViewById<Button>(R.id.granularity_year)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.YEAR) }
        findViewById<Button>(R.id.granularity_all)?.setOnClickListener { marketDataViewModel.loadGranularity(Granularity.ALL) }
        marketDataViewModel.currentGranularity.observe(this, currentGranularityObserver)
        marketDataViewModel.currentBtcPrice.observe(this, btcPriceObserver)
        marketDataViewModel.periodData.observe(this, periodDataObserver)
        updateGranularity()
        marketDataViewModel.loadGranularity(currentGranularity)
        marketDataViewModel.loadCurrentPrice()
    }

    private fun updateGranularity() {
        when (currentGranularity) {
            Granularity.DAY -> select(findViewById(R.id.granularity_day))
            Granularity.WEEK -> select(findViewById(R.id.granularity_week))
            Granularity.MONTH -> select(findViewById(R.id.granularity_month))
            Granularity.YEAR -> select(findViewById(R.id.granularity_year))
            Granularity.ALL -> select(findViewById(R.id.granularity_all))
        }
    }

    private fun clearSelection() {
        when (currentGranularity) {
            Granularity.DAY -> reset(findViewById(R.id.granularity_day))
            Granularity.WEEK -> reset(findViewById(R.id.granularity_week))
            Granularity.MONTH -> reset(findViewById(R.id.granularity_month))
            Granularity.YEAR -> reset(findViewById(R.id.granularity_year))
            Granularity.ALL -> reset(findViewById(R.id.granularity_all))
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            currentGranularity = Granularity.from(savedInstanceState.getInt("GRANULARITY", 0))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("GRANULARITY", currentGranularity.ord)
    }

    private fun renderPeriodData(prices: List<HistoricalPriceRecord>?) {
        val priceData = getDataFor(prices)
        findViewById<LineChart>(R.id.chart)?.apply {
            clear()
            data = LineData(LineDataSet(priceData, "set 1").also {
                it.lineWidth = 2.0F
                it.color = resources.getColor(R.color.colorAccent)
                it.setDrawValues(false)
                it.setDrawCircles(false)
            })
            axisRight.mAxisMaximum = data.yMax
            axisRight.mAxisMinimum = data.yMin
            updateMinMaxValues(data.yMin, data.yMax)
            animateX(300, Easing.EaseInOutBack)
            notifyDataSetChanged()
            invalidate()
        }
        updatePriceMovement(priceData)
    }

    internal fun updateMinMaxValues(min: Float, max: Float) {
        findViewById<TextView>(R.id.market_low)?.apply {
            text = USDCurrency(min.toDouble()).toFormattedCurrency()
        }
        findViewById<TextView>(R.id.market_high)?.apply {
            text = USDCurrency(max.toDouble()).toFormattedCurrency()
        }
    }

    internal fun updatePriceMovement(data: MutableList<Entry>) {
        findViewById<TextView>(R.id.market_sentiment)?.apply {
            var priceDifference = data.last().y - data.first().y
            var percentageDifference = priceDifference / data.first().y * 100
            var appearance = 0
            var leader = ""
            if (percentageDifference < 0) {
                percentageDifference *= -1F
                priceDifference *= -1F
                leader = "-"
                appearance = R.style.TextAppearance_Chart_Sentiment_Down
            } else {
                appearance = R.style.TextAppearance_Chart_Sentiment_Up
            }
            text = context.getString(R.string.market_movement_up,
                    leader,
                    USDCurrency(priceDifference.toDouble()).toFormattedCurrency(),
                    BigDecimal(percentageDifference.toDouble()).setScale(2, RoundingMode.HALF_EVEN).toString(),
                    getString(R.string.percentage)
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setTextAppearance(appearance)
            } else {
                setTextAppearance(context, appearance)
            }

        }
    }

    private fun getDataFor(prices: List<HistoricalPriceRecord>?): MutableList<Entry> {
        val entries = mutableListOf<Entry>()
        prices?.let {
            val limit = getLimitForPeriod() ?: prices.size
            (prices.size - limit - 1..prices.size - 1).forEachIndexed { i: Int, position: Int ->
                try {
                    entries.add(Entry(i.toFloat(), it[position].average))
                } catch (err: Exception) {

                }
            }

        }

        return entries
    }

    private fun getLimitForPeriod(): Int? {
        when (currentGranularity) {
            Granularity.DAY -> return dayPeriodLimit
            Granularity.WEEK -> return weekPeriodLimit
            Granularity.MONTH -> return monthPeriodLimit
            Granularity.YEAR -> return yearPeriodLimit
            else -> return null
        }

    }

    @Suppress("DEPRECATION")
    @SuppressLint("ResourceType")
    private fun reset(button: Button?) {
        button?.apply {
            setBackgroundResource(R.drawable.button_market_granularity)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setTextAppearance(context, R.style.TextAppearance_MarketGranularity)
            } else {
                setTextAppearance(R.style.TextAppearance_MarketGranularity)
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun select(button: Button?) {
        button?.apply {
            background = this@MarketChartsFragment.resources.getDrawable(R.drawable.button_market_granularity_pressed, this@MarketChartsFragment.activity?.theme)
            setTextColor(resources.getColor(R.color.font_white))
        }
    }

    private fun configureChart(chart: LineChart?) {
        chart?.apply {
            clear()
            notifyDataSetChanged()
            invalidate()
            isHighlightPerDragEnabled = false
            description = Description().also { it.text = "" }
            isAutoScaleMinMaxEnabled = true
            setTouchEnabled(false)
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            setPinchZoom(false)
            isDragEnabled = false
            setViewPortOffsets(0F, 0F, 0F, 0F)
            setDrawBorders(false)
            requestDisallowInterceptTouchEvent(false)
            resetZoom()
            xAxis.apply {
                isEnabled = false
                requestDisallowInterceptTouchEvent(false)
                setDrawGridLines(false)
                setDrawLabels(true)
                setDrawAxisLine(false)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawLimitLinesBehindData(true)
                setAvoidFirstLastClipping(false)
            }

            axisLeft.apply {
                isEnabled = false
                maxWidth = 0F
            }

            axisRight.apply {
                isEnabled = true
                maxWidth = 0F
                gridLineWidth = 0F
                setDrawLabels(false)
                textColor = resources.getColor(R.color.font_gray)
                setDrawGridLines(false)
                setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
            }


            legend.apply { isEnabled = false }

        }
    }

}
