package com.coinninja.coinkeeper.ui.home

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.nhaarman.mockitokotlin2.whenever
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
}