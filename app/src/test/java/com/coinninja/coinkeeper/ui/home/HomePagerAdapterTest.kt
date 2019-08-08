package com.coinninja.coinkeeper.ui.home

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.ui.lightning.LightningHistoryFragment
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryFragment
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomePagerAdapterTest {
    @Test
    fun `has 2 screens`() {
        val adapter = createHomePageAdapter()

        assertThat(adapter.count).isEqualTo(2)
    }

    @Test
    fun page_1_is_the_Transaction_History_Screen() {
        assertTrue(createHomePageAdapter().getItem(0) is TransactionHistoryFragment)
    }

    @Test
    fun page_2_is_the_Lightning_History_Screen() {
        assertTrue(createHomePageAdapter().getItem(1) is LightningHistoryFragment)
    }

    private fun createHomePageAdapter(): HomePagerAdapter = HomePagerAdapter(mock(), Lifecycle.State.CREATED.ordinal)
}