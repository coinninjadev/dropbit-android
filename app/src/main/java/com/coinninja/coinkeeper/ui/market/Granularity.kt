package com.coinninja.coinkeeper.ui.market

enum class Granularity(val ord: Int, val value: String) {

    DAY(0, "daily"), WEEK(1, "monthly"), MONTH(2, "monthly"), YEAR(3, "alltime"), ALL(4, "alltime");

    companion object {
        fun from(granularity: Int): Granularity {
            when (granularity) {
                0 -> return DAY
                1 -> return WEEK
                2 -> return MONTH
                3 -> return YEAR
                4 -> return ALL
                else -> return DAY
            }
        }
    }
}
