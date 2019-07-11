package com.coinninja.coinkeeper.ui.market

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarketScreenFragmentTest {

    @Test
    fun `sets up market charts and news`() {
        val scenario = FragmentScenario.launch(MarketScreenFragment::class.java)

        scenario.onFragment { fragment ->
            assertThat(fragment.childFragmentManager.findFragmentByTag(MarketScreenFragment.marketChartsFragmentTag)).isNotNull()
            assertThat(fragment.childFragmentManager.findFragmentByTag(MarketScreenFragment.marketNewsFragmentTag)).isNotNull()
        }
    }
}
