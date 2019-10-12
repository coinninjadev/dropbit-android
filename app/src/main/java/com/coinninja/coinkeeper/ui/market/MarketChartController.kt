package com.coinninja.coinkeeper.ui.market

import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.service.client.model.HistoricalPriceRecord
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class MarketChartController {

    fun renderPeriodData(lineChart: LineChart?, priceData: MutableList<Entry>): ChartStats {
        lineChart?.let { chart ->
            chart.apply {
                clear()
                data = LineData(LineDataSet(priceData, "set 1").also {
                    it.lineWidth = 2.0F
                    it.color = resources.getColor(R.color.colorAccent)
                    it.setDrawValues(false)
                    it.setDrawCircles(false)
                })
                axisRight.mAxisMaximum = data.yMax
                axisRight.mAxisMinimum = data.yMin
                animateX(300, Easing.EaseInOutBack)
                notifyDataSetChanged()
                invalidate()
            }

            return ChartStats(
                    chart.data.yMax,
                    chart.data.yMin,
                    priceData.first().y,
                    priceData.last().y
            )
        }
        return ChartStats(0F, 0F, 0F, 0F)
    }

    fun configureChart(chart: LineChart?) {
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

    fun getDataForGranularity(granularity: Granularity, prices: List<HistoricalPriceRecord>): MutableList<Entry> {
        val entries = mutableListOf<Entry>()
        val limit = getLimitForPeriod(granularity) ?: prices.size
        (prices.size - limit - 1..prices.size - 1).forEachIndexed { i: Int, position: Int ->
            try {
                entries.add(Entry(i.toFloat(), prices[position].average))
            } catch (err: Exception) {

            }
        }


        return entries
    }

    private fun getLimitForPeriod(granularity: Granularity): Int? {
        when (granularity) {
            Granularity.DAY -> return dayPeriodLimit
            Granularity.WEEK -> return weekPeriodLimit
            Granularity.MONTH -> return monthPeriodLimit
            Granularity.YEAR -> return yearPeriodLimit
            else -> return null
        }

    }

    companion object {
        const val dayPeriodLimit = 1440
        const val weekPeriodLimit = 168
        const val monthPeriodLimit = 720
        const val yearPeriodLimit = 365
    }

}
