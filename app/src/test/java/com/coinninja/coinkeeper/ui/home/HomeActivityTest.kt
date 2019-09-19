package com.coinninja.coinkeeper.ui.home

import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.android.synthetic.main.activity_home.*
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
    fun checks_for_locked_lightning_account() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            verify(activity.walletViewModel.isLightningLocked).observe(activity, activity.isLightningLockedObserver)
            verify(activity.walletViewModel).checkLightningLock()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun locks_lighting_when_locked() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(true)
            verify((activity.home_pager.adapter as HomePagerAdapter)).isLightningLocked = true


        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun unlocks_lighting_when_not_locked() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(false)
            verify((activity.home_pager.adapter as HomePagerAdapter)).isLightningLocked = false
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun payment_bar_is_hidden_when_on_locked_lightning_screen() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(true)

            activity.tabs.selectTab(activity.tabs.getTabAt(1))
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.GONE)

            activity.tabs.selectTab(activity.tabs.getTabAt(0))
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.VISIBLE)

            activity.isLightningLockedObserver.onChanged(false)

            activity.tabs.selectTab(activity.tabs.getTabAt(1))
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.VISIBLE)

            activity.tabs.selectTab(activity.tabs.getTabAt(0))
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_detail_of_transaction_from_Creation_intent() {
        val txid = "--TXID--"
        creationIntent.putExtra(DropbitIntents.EXTRA_TRANSACTION_ID, txid)
        val scenario = setupActivity()

        scenario.onActivity { activity ->

            verify(application.activityNavigationUtil).showTransactionDetail(activity, txid = txid)
            Assert.assertFalse(activity.intent.hasExtra(DropbitIntents.EXTRA_TRANSACTION_ID))
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun changing_tabs_changes_account_modes() {
        val scenario = setupActivity()
        scenario.onActivity { activity ->

            activity.tabs.selectTab(activity.tabs.getTabAt(1))
            verify(activity.accountModeManger).changeMode(AccountMode.LIGHTNING)

            activity.tabs.selectTab(activity.tabs.getTabAt(0))
            verify(activity.accountModeManger, times(1)).changeMode(AccountMode.BLOCKCHAIN)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adds_tabs_to_appbar() {
        val scenario = setupActivity()
        scenario.onActivity {
            verify(it.actionBarController).addTab(it, R.layout.home_appbar_tab_1, 0)
            verify(it.actionBarController).addTab(it, R.layout.home_appbar_tab_2, 1)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun sets_matches_mode_when_resumed() {
        val scenario = setupActivity()
        scenario.onActivity { activity ->

            assertThat(activity.tabs.selectedTabPosition).isEqualTo(1)
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}
