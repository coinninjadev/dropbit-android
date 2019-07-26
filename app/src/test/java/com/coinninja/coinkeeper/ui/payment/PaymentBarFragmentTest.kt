package com.coinninja.coinkeeper.ui.payment

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.android.helpers.Views.clickOn
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.service.client.model.TransactionFee
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.ui.payment.request.RequestDialogFragment
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.util.crypto.BitcoinUri
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.fragment.PayDialogFragment
import com.coinninja.matchers.IntentFilterSubject.Companion.assertThatIntentFilter
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentBarFragmentTest {

    companion object {
        private const val initialUSDValue = 10000L
        private val initialFee: TransactionFee = TransactionFee(0.0, 5.0, 10.0)
    }

    private val usdCurrency = USDCurrency()
    private val btcCurrency = BTCCurrency()
    private val bitcoinUri: BitcoinUri = mock()

    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()
    private val creationIntent = Intent(application, TestableActivity::class.java)

    private var defaultCurrencies: DefaultCurrencies = DefaultCurrencies(btcCurrency, usdCurrency)
    private var scenario: ActivityScenario<TestableActivity> = ActivityScenario.launch(creationIntent)

    private val paymentBarFragment = PaymentBarFragment()

    private fun configureDI() {
        application.apply {
            walletHelper = mock()
            currencyPreference = mock()
            bitcoinUtil = mock()
        }.also {
            whenever(it.walletHelper.latestPrice).thenReturn(USDCurrency(initialUSDValue))
            whenever(it.walletHelper.latestFee).thenReturn(initialFee)
            whenever(it.currencyPreference.currenciesPreference).thenReturn(defaultCurrencies)
            whenever(it.currencyPreference.fiat).thenReturn(usdCurrency)

        }
    }

    private val sendButton: View get() = paymentBarFragment.findViewById(R.id.send_btn)!!
    private val requestButton: View get() = paymentBarFragment.findViewById(R.id.request_btn)!!

    private fun launchHome(withBitcoinUri: Boolean = false) {
        val uri = Uri.parse("bitcoin:?r=https://bitpay.com/i/JHbWb7uRHL29bHhH6h5oTa")
        configureDI()
        paymentBarFragment.apply {
            paymentUtil = mock()
        }.also {
            whenever(it.paymentUtil.paymentHolder).thenReturn(it.paymentHolder)
        }
        scenario.onActivity {
            if (withBitcoinUri) {
                whenever(application.bitcoinUtil.parse(uri.toString())).thenReturn(bitcoinUri)
                it.intent.data = uri
            }
            it.supportFragmentManager.beginTransaction().apply {
                add(R.id.test_frame, paymentBarFragment)
                commit()
            }
        }
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun shows_pay_dialog_when_creation_intent_contains_bitcoin_uri() {
        launchHome(true)

        verify(paymentBarFragment.bitcoinUtil).parse("bitcoin:?r=https://bitpay.com/i/JHbWb7uRHL29bHhH6h5oTa")
        verify(paymentBarFragment.activityNavigationUtil).showDialogWithTag(eq(paymentBarFragment.childFragmentManager),
                any(), eq(PayDialogFragment::class.java.simpleName))

        scenario.onActivity {
            assertThat(it.intent.data).isEqualTo(null)
        }
    }

    fun observes_wallet_sync_complete() {
        launchHome()
        verify(application.localBroadCastUtil).registerReceiver(paymentBarFragment.receiver, paymentBarFragment.intentFilter)

        assertThatIntentFilter(paymentBarFragment.intentFilter).containsAction(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE)
    }

    @Test
    fun updates_spendable_balance_after_wallet_sync() {
        launchHome()
        whenever(application.walletHelper.spendableBalance).thenReturn(BTCCurrency(50000L))
                .thenReturn(BTCCurrency(10L))

        paymentBarFragment.receiver.onReceive(paymentBarFragment.context, Intent(DropbitIntents.ACTION_WALLET_SYNC_COMPLETE))
        clickOn(sendButton)

        assertThat(paymentBarFragment.paymentHolder.spendableBalance.toLong()).isEqualTo(10L)
    }

    @Test
    fun stops_observing_wallet_sync_when_stopped() {
        launchHome()
        val fragment = paymentBarFragment

        scenario.moveToState(Lifecycle.State.DESTROYED)

        verify(application.localBroadCastUtil).unregisterReceiver(fragment.receiver)
    }

    @Test
    fun shows_payment_dialog() {
        launchHome()
        whenever(application.walletHelper.spendableBalance).thenReturn(BTCCurrency(50000L))
        val paymentBar = paymentBarFragment

        clickOn(sendButton)

        verify(paymentBar.activityNavigationUtil).showDialogWithTag(eq(paymentBar.childFragmentManager),
                any(), eq(PayDialogFragment::class.java.simpleName))

        /* TODO figure out how to capture these arguments
        val dialog = argumentCaptor.value
        assertThat(dialog.paymentUtil.paymentHolder!!.evaluationCurrency.toLong()).isEqualTo(initialUSDValue)
        assertThat(dialog.paymentUtil.paymentHolder!!.spendableBalance.toLong()).isEqualTo(application.walletHelper.spendableBalance.toLong())
        verify(dialog.paymentUtil).setFee(initialFee.slow)
        verify(dialog.paymentUtil).paymentHolder = paymentBar.paymentHolder
        assertThat(dialog.paymentUtil).isEqualTo(paymentBar.paymentUtil)
        assertThat(dialog.paymentUtil.paymentHolder).isEqualTo(paymentBar.paymentHolder)
         */
    }

    @Test
    fun shows_request_dialog() {
        launchHome()
        clickOn(requestButton)

        verify(application.activityNavigationUtil).showDialogWithTag(eq(paymentBarFragment.childFragmentManager),
                any(), eq(RequestDialogFragment::class.java.simpleName))
    }

    @Test
    fun clears_payment_info_when_payment_canceled() {
        launchHome()
        clickOn(sendButton)
        val paymentBar = paymentBarFragment
        paymentBar.paymentHolder.paymentAddress = "--address--"

        val payDialog: PayDialogFragment = mock()
        paymentBar.cancelPayment(payDialog)

        assertThat(paymentBar.paymentHolder.getPaymentAddress()).isEqualTo("")
        verify(payDialog).dismiss()
    }

    @Module
    class TestPaymentBarModule {
        @Provides
        fun feesManager(): FeesManager {
            val manager = mock<FeesManager>()
            whenever(manager.currentFee()).thenReturn(initialFee.slow)
            return manager
        }

    }
}
