package com.coinninja.coinkeeper.util.analytics

import com.coinninja.coinkeeper.util.analytics.AnalyticsBalanceRange.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnalyticsBalanceRangeTest {

    @Test
    fun balance_to_enum_for_reporting() {
        assertThat(AnalyticsBalanceRange.fromBalance(0)).isEqualTo(NONE)
        assertThat(AnalyticsBalanceRange.fromBalance(9_999)).isEqualTo(UNDER_DECI_MILLI_BTC)
        assertThat(AnalyticsBalanceRange.fromBalance(99_999)).isEqualTo(UNDER_MILLI_BTC)
        assertThat(AnalyticsBalanceRange.fromBalance(999_999)).isEqualTo(UNDER_CENTI_BTC)
        assertThat(AnalyticsBalanceRange.fromBalance(9_999_999)).isEqualTo(UNDER_DECI_BTC)
        assertThat(AnalyticsBalanceRange.fromBalance(99_999_999)).isEqualTo(OVER_DECI_BTC)
    }
}