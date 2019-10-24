package com.coinninja.coinkeeper.ui.home

import android.content.Intent
import android.net.Uri
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
import com.coinninja.coinkeeper.ui.payment.create.CreatePaymentActivity
import com.coinninja.coinkeeper.util.CurrencyPreference
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric

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

    private fun flush() {
        Robolectric.flushBackgroundThreadScheduler()
        Robolectric.flushForegroundThreadScheduler()
    }


    private fun setupDI() {
        val defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        application.activityNavigationUtil = Mockito.mock(ActivityNavigationUtil::class.java)
        application.currencyPreference = Mockito.mock(CurrencyPreference::class.java)
        whenever(application.currencyPreference.currenciesPreference).thenReturn(defaultCurrencies)
    }

    @Test
    fun intents_with_wyre_uri_show_dialog__with_transfer_id() {
        val uri = Uri.parse("dropbit://wyre?transferId=tID")
        creationIntent.data = uri

        val scenario = setupActivity()
        scenario.onActivity { activity ->
            val dialog = activity.supportFragmentManager.findFragmentByTag(HomeActivity.wyreTransferDialog) as GenericAlertDialog
            assertThat(dialog).isNotNull()
            assertThat(creationIntent.data).isNull()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun schedules_30_second_sync() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            verify(activity.syncWalletManager).schedule30SecondSync()
            flush()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun lock_status_change__hides_payment_bar__when_account_mode_lightning() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            whenever(activity.accountModeManger.accountMode).thenReturn(AccountMode.LIGHTNING)
            activity.selectTabForMode()

            activity.isLightningLockedObserver.onChanged(true)
            flush()


            assertThat(activity.paymentBarFragment.view?.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun lock_status_change__does_not_hide_payment_bar__when_account_mode_blockchain() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            whenever(activity.accountModeManger.accountMode).thenReturn(AccountMode.BLOCKCHAIN)
            activity.selectTabForMode()
            flush()

            activity.isLightningLockedObserver.onChanged(true)


            assertThat(activity.paymentBarFragment.view?.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun payment_bar_is_hidden_when_on_locked_lightning_screen() {
        val scenario = setupActivity()

        scenario.onActivity { activity ->
            activity.isLightningLockedObserver.onChanged(true)
            flush()

            activity.tabs.selectTab(activity.tabs.getTabAt(1))
            flush()
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.GONE)

            activity.tabs.selectTab(activity.tabs.getTabAt(0))
            flush()
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.VISIBLE)

            activity.isLightningLockedObserver.onChanged(false)

            activity.tabs.selectTab(activity.tabs.getTabAt(1))
            flush()
            assertThat(activity.paymentBarFragment.view!!.visibility).isEqualTo(View.VISIBLE)

            activity.tabs.selectTab(activity.tabs.getTabAt(0))
            flush()
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

            flush()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun changing_tabs_changes_account_modes() {
        val scenario = setupActivity()
        scenario.onActivity { activity ->

            activity.tabs.selectTab(activity.tabs.getTabAt(1))
            flush()
            verify(activity.walletViewModel).setMode(AccountMode.LIGHTNING)

            activity.tabs.selectTab(activity.tabs.getTabAt(0))
            flush()
            verify(activity.walletViewModel).setMode(AccountMode.BLOCKCHAIN)
            flush()
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adds_tabs_to_appbar() {
        val scenario = setupActivity()
        scenario.onActivity {
            verify(it.actionBarController).addTab(it, R.layout.home_appbar_tab_1, 0)
            Robolectric.flushBackgroundThreadScheduler()
            flush()
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun sets_matches_mode_when_resumed() {
        val scenario = setupActivity()
        scenario.onActivity { activity ->
            assertThat(activity.tabs.selectedTabPosition).isEqualTo(1)
            flush()
        }
        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}
