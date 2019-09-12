package com.coinninja.coinkeeper.ui.payment.request

import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayRequestScreenFragmentTest {
    @Test
    fun set_up_copy_button_with_bitcoin_address() {
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().apply {
            accountManager = mock { whenever(it.nextReceiveAddress).thenReturn("--address--") }
            bitcoinUriBuilder = mock()
            whenever(bitcoinUriBuilder.setAddress(any())).thenReturn(bitcoinUriBuilder)
            whenever(bitcoinUriBuilder.build()).thenReturn(mock())
        }
        val scenario = FragmentScenario.launch(PayRequestScreenFragment::class.java)

        scenario.onFragment { fragment ->
            assertThat(fragment.childFragmentManager.findFragmentByTag(PayRequestScreenFragment.fragmentTag)).isNotNull()
        }
    }

}