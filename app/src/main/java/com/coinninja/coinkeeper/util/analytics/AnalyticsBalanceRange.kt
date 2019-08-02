package com.coinninja.coinkeeper.util.analytics

enum class AnalyticsBalanceRange(val id: Int, val label: String) {
    UNDER_MILLI_BTC(1, "UnderMilliBTC"),
    UNDER_CENTI_BTC(2, "UnderCentiBTC"),
    UNDER_DECI_BTC(3, "UnderDeciBTC"),
    OVER_DECI_BTC(4, "OverDeciBTC");

    companion object {
        fun fromBalance(satoshis: Long): AnalyticsBalanceRange {
            return if (satoshis < 100_000) UNDER_MILLI_BTC
            else if (satoshis < 1_000_000) UNDER_CENTI_BTC
            else if (satoshis < 10_000_000) UNDER_DECI_BTC
            else OVER_DECI_BTC
        }
    }
}