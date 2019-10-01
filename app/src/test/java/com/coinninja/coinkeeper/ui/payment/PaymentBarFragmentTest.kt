package com.coinninja.coinkeeper.ui.payment

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.cn.wallet.mode.AccountModeManager
import com.coinninja.coinkeeper.ui.home.HomeActivity
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentBarFragmentTest {


    private val bitcoinUri: BitcoinUri = mock()
    private val creationIntent = Intent(ApplicationProvider.getApplicationContext(), HomeActivity::class.java)

    private fun configureDI(withBitcoinUri: Boolean = false) {
        val uri = Uri.parse("bitcoin:?r=https://bitpay.com/i/JHbWb7uRHL29bHhH6h5oTa")
        ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>().apply {
            bitcoinUtil = mock()
            bitcoinUriBuilder = mock()
        }.also {
            if (withBitcoinUri) {
                creationIntent.data = uri
                whenever(it.bitcoinUriBuilder.parse(uri.toString())).thenReturn(bitcoinUri)
                whenever(bitcoinUri.isValidPaymentAddress).thenReturn(true)
            }
        }
    }

    private fun createScenario(withBitcoinUri: Boolean = false): ActivityScenario<HomeActivity> {
        configureDI(withBitcoinUri)
        return ActivityScenario.launch(creationIntent)
    }

    @Test
    fun shows_pay_dialog_when_creation_intent_contains_bitcoin_uri() {
        val scenario = createScenario(true)

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.payment_bar_fragment)!! as PaymentBarFragment

            verify(fragment.bitcoinUriBuilder).parse("bitcoin:?r=https://bitpay.com/i/JHbWb7uRHL29bHhH6h5oTa")
            verify(fragment.activityNavigationUtil).navigateToPaymentCreateScreen(activity, bitcoinUri = bitcoinUri)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_payment_create_screen_from_button_press() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.payment_bar_fragment)!! as PaymentBarFragment

            fragment.sendButton.performClick()

            verify(fragment.activityNavigationUtil).navigateToPaymentCreateScreen(activity, false, null)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_payment_create_screen_from_scan_button() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.payment_bar_fragment)!! as PaymentBarFragment

            fragment.scanButton.performClick()

            verify(fragment.activityNavigationUtil).navigateToPaymentCreateScreen(activity, true, null)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun shows_payment_request_screen() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.payment_bar_fragment)!! as PaymentBarFragment

            fragment.requestButton.performClick()

            verify(fragment.activityNavigationUtil).navigateToPaymentRequestScreen(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun removes_observer_when_stopped() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            val fragment = activity.supportFragmentManager.findFragmentById(R.id.payment_bar_fragment)!! as PaymentBarFragment
            val observer = fragment.accountModeChangeObserver

            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(fragment.accountModeManager).removeObserver(observer)
        }

        scenario.close()
    }

    @Module
    class TestPaymentBarModule {
        @Provides
        fun accountModeManager(): AccountModeManager {
            val manager = mock<AccountModeManager>()
            whenever(manager.accountMode).thenReturn(AccountMode.LIGHTNING)
            return manager
        }
    }
}
