package com.coinninja.coinkeeper.ui.home

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.ui.market.MarketScreenFragment
import com.coinninja.coinkeeper.ui.payment.request.PayRequestScreenFragment
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomePagerAdapterTest {
    @Test
    fun `has 3 screens`() {
        val adapter = createHomePageAdapter()

        assertThat(adapter.count).isEqualTo(3)
    }

    @Test
    fun `page 1 is the Market Screen`() {
        assertTrue(createHomePageAdapter().getItem(0) is MarketScreenFragment)
    }

    @Test
    fun `page 2 is the Transaction History Screen`() {
        assertTrue(createHomePageAdapter().getItem(1) is TransactionHistoryFragment)
    }

    @Test
    fun `page 3 is the Pay Request Screen`() {
        assertTrue(createHomePageAdapter().getItem(2) is PayRequestScreenFragment)
    }

    private fun createHomePageAdapter(): HomePagerAdapter = HomePagerAdapter(mock(), Lifecycle.State.CREATED.ordinal)
}