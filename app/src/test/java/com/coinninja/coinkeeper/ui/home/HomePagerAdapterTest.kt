package com.coinninja.coinkeeper.ui.home

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomePagerAdapterTest {
    @Test
    fun `has 1 screens`() {
        val adapter = createHomePageAdapter()

        assertThat(adapter.count).isEqualTo(1)
    }

    @Test
    fun `page 1 is the Transaction History Screen`() {
        assertTrue(createHomePageAdapter().getItem(1) is TransactionHistoryFragment)
    }


    private fun createHomePageAdapter(): HomePagerAdapter = HomePagerAdapter(mock(), Lifecycle.State.CREATED.ordinal)
}