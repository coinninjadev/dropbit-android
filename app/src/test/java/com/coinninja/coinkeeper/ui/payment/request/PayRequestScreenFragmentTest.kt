package com.coinninja.coinkeeper.ui.payment.request

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayRequestScreenFragmentTest {
    @Test
    fun `set up copy button with bitcoin address`() {
        val scenario = FragmentScenario.launch(PayRequestScreenFragment::class.java)

        scenario.onFragment { fragment ->
            assertThat(fragment.childFragmentManager.findFragmentByTag(PayRequestScreenFragment.fragmentTag)).isNotNull()
        }
    }

}