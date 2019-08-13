package com.coinninja.coinkeeper.ui.home

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.viewpager.widget.ViewPager
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.google.android.material.tabs.TabLayout
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers.equalTo
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
internal class HomeActivityTest {

    private val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
    private val creationIntent = Intent(application, HomeActivity::class.java)

    private fun setupActivity(): ActivityScenario<HomeActivity> {
        setupDI()
        val scenario = ActivityScenario.launch<HomeActivity>(creationIntent)
        scenario.moveToState(Lifecycle.State.RESUMED)
        return scenario
    }

    private fun setupDI() {
        val defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        application.activityNavigationUtil = Mockito.mock(ActivityNavigationUtil::class.java)
        application.currencyPreference = Mockito.mock(CurrencyPreference::class.java)
        whenever(application.currencyPreference.currenciesPreference).thenReturn(defaultCurrencies)
    }

    @Test
    fun shows_detail_of_transaction_from_Creation_intent() {
        val txid = "--TXID--"
        creationIntent.putExtra(DropbitIntents.EXTRA_TRANSACTION_ID, txid)
        val scenario = setupActivity()

        scenario.onActivity { activity ->

            Mockito.verify(application.activityNavigationUtil).showTransactionDetail(activity, txid = txid)
            Assert.assertFalse(activity.intent.hasExtra(DropbitIntents.EXTRA_TRANSACTION_ID))
        }
    }

    @Test
    fun configures_pager_and_tabs() {
        setupActivity().onActivity { activity ->
            val pager = activity.findViewById<ViewPager>(R.id.home_pager)
            val tabs = activity.findViewById<TabLayout>(R.id.pager_tabs)
            Assert.assertThat(pager.currentItem, equalTo(1))
            assertEquals(pager.adapter, activity.homePagerAdapterProvider.provide(activity.supportFragmentManager, activity.lifecycle.currentState))
            Assert.assertThat(tabs.selectedTabPosition, equalTo(1))
        }
    }

    @Test
    fun shows_market_selection_when_observer_called() {
        setupActivity().onActivity { activity ->
            val pager = activity.findViewById<ViewPager>(R.id.home_pager)

            Assert.assertThat(pager.currentItem, equalTo(1))

            activity.showMarketPage()

            Assert.assertThat(pager.currentItem, equalTo(0))
        }
    }

    @Test
    fun restores_state_when_resuming_session() {
        val scenario = setupActivity()
        scenario.onActivity { activity ->
            val pager = activity.findViewById<ViewPager>(R.id.home_pager)
            val tabs = activity.findViewById<TabLayout>(R.id.pager_tabs)
            pager.setCurrentItem(0, false)
            Assert.assertThat(pager.currentItem, equalTo(0))
            Assert.assertThat(tabs.selectedTabPosition, equalTo(0))
        }

        scenario.recreate()

        scenario.onActivity { activity ->
            val pager = activity.findViewById<ViewPager>(R.id.home_pager)
            Assert.assertThat(pager.currentItem, equalTo(0))
        }
    }

    @Test
    fun observes_market_page_being_selected() {
        setupActivity().onActivity { activity ->
            val pager = activity.findViewById<ViewPager>(R.id.home_pager)
            Assert.assertThat(pager.currentItem, equalTo(1))

            activity.showMarketPage()

            Assert.assertThat(pager.currentItem, equalTo(0))
            verify(activity.analytics).trackEvent(Analytics.EVENT_CHARTS_OPENED)
        }
    }
}
