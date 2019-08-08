package com.coinninja.coinkeeper.ui.market

data class ChartStats(val high: Float, val low: Float, val first: Float, val last: Float) {
    val priceDifference get() = last - first
    val percentageDifference get() = priceDifference / first * 100
}