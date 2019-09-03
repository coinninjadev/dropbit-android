package com.coinninja.coinkeeper.util.analytics

enum class AnalyticsBalanceRange(val id: Int, val label: String) {
    NONE(0, "None"),
    UNDER_DECI_MILLI_BTC(1, "UnderDeciMilliBTC"),
    UNDER_MILLI_BTC(2, "UnderMilliBTC"),
    UNDER_CENTI_BTC(3, "UnderCentiBTC"),
    UNDER_DECI_BTC(4, "UnderDeciBTC"),
    OVER_DECI_BTC(5, "OverDeciBTC");

    companion object {
        fun fromBalance(satoshis: Long): AnalyticsBalanceRange {
            return if (satoshis == 0L) NONE
            else if (satoshis < 10_000) UNDER_DECI_MILLI_BTC
            else if (satoshis < 100_000) UNDER_MILLI_BTC
            else if (satoshis < 1_000_000) UNDER_CENTI_BTC
            else if (satoshis < 10_000_000) UNDER_DECI_BTC
            else OVER_DECI_BTC
        }
    }
}