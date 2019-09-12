package com.coinninja.coinkeeper.ui.lightning.deposit

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningDepositActivityTest {

    val creationIntent = Intent(ApplicationProvider.getApplicationContext(), LightningDepositActivity::class.java)
    private fun createScenario(): ActivityScenario<LightningDepositActivity> {
        val app = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        app.currencyPreference = mock()
        whenever(app.currencyPreference.currenciesPreference).thenReturn(DefaultCurrencies(USDCurrency(), BTCCurrency()))
        return ActivityScenario.launch(creationIntent)
    }

    @Test
    fun does_not_focus_on_input_when_initialized_with_deposit_amount() {
        createScenario().onActivity { activity ->

            assertThat(activity.depositAmountView.hasFocus()).isFalse()
        }
    }

    @Test
    fun close_button_navigates_back() {
        createScenario().onActivity { activity ->
            activity.close.performClick()

            assertThat(activity.isFinishing).isEqualTo(true)
        }
    }

    @Test
    fun observes_model_changes() {
        createScenario().onActivity { activity ->
            verify(activity.walletViewModel.fetchLightningBalance()).observe(activity, activity.lightningBalanceObserver)
            verify(activity.walletViewModel.fetchBtcLatestPrice()).observe(activity, activity.latestPriceObserver)
            verify(activity.fundingViewModel.transactionData).observe(activity, activity.transactionDataObserver)
        }
    }

    @Test
    fun renders_amount_to_send() {
        val amount = USDCurrency(20_00)
        val latestPrice = USDCurrency(10_000_00)
        val btcAmount = amount.toBTC(latestPrice)
        creationIntent.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)
        createScenario().onActivity { activity ->
            activity.latestPriceObserver.onChanged(latestPrice)

            val depositValueView = activity.depositAmountView
            assertThat(depositValueView.paymentHolder.fiat.toLong()).isEqualTo(amount.toLong())
            assertThat(depositValueView.paymentHolder.cryptoCurrency.toLong()).isEqualTo(btcAmount.toSatoshis())
        }
    }

    @Test
    fun checks_funding_for_amount__when_initialized_with_amount() {
        val amount = USDCurrency(20_00)
        val latestPrice = USDCurrency(10_000_00)
        val btcAmount = amount.toBTC(latestPrice)
        creationIntent.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)

        createScenario().onActivity { activity ->
            activity.latestPriceObserver.onChanged(latestPrice)

            verify(activity.fundingViewModel).fundLightningDeposit(btcAmount.toLong())
        }
    }

    @Test
    fun clears_initial_input_when_consumed() {
        val latestPrice = USDCurrency(10_000_00)
        val amount = USDCurrency(20_00)
        creationIntent.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)
        createScenario().onActivity { activity ->
            activity.latestPriceObserver.onChanged(latestPrice)

            assertThat(activity.intent.hasExtra(DropbitIntents.EXTRA_AMOUNT)).isFalse()
        }

    }

    @Test
    fun notifies_user_of_insufficient_funding__initialized_with_amount() {
        creationIntent.putExtra(DropbitIntents.EXTRA_AMOUNT, USDCurrency(20_00))

        createScenario().onActivity { activity ->
            val latestPrice = USDCurrency(10_000_00)
            activity.latestPriceObserver.onChanged(latestPrice)
            activity.confirmed = true
            activity.transactionDataObserver.onChanged(TransactionData(emptyArray(), 0, 0, 0, mock(), ""))

            val dialog = activity.supportFragmentManager.findFragmentByTag("NON_SUFFICIENT_FUNDS_DIALOG") as GenericAlertDialog

            assertThat(dialog).isNotNull()
            assertThat(dialog.message).isEqualTo("Attempting to deposit 0.002. Not enough spendable funds.")
            assertThat(activity.confirmed).isFalse()
        }
    }

    @Test
    fun confirming_deposit_funds_transaction() {
        val amount = USDCurrency(20_00)
        val latestPrice = USDCurrency(10_000_00)
        val btcAmount = amount.toBTC(latestPrice)
        creationIntent.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)

        createScenario().onActivity { activity ->
            activity.latestPriceObserver.onChanged(latestPrice)
            activity.onConfirmationCompleted()

            assertThat(activity.confirmed).isTrue()
            verify(activity.fundingViewModel, atLeast(1)).fundLightningDeposit(btcAmount.toLong())
        }
    }

    @Test
    fun confirmed_payment_request_processes_once_funded() {
        val amount = USDCurrency(20_00)
        val latestPrice = USDCurrency(10_000_00)
        val btcAmount = amount.toBTC(latestPrice)
        creationIntent.putExtra(DropbitIntents.EXTRA_AMOUNT, amount)
        createScenario().onActivity { activity ->
            activity.latestPriceObserver.onChanged(latestPrice)
            activity.onConfirmationCompleted()

            val transactionData = TransactionData(arrayOf(mock()), btcAmount.toSatoshis(), 0, 0, mock(), "")
            activity.transactionDataObserver.onChanged(transactionData)

            verify(activity.activityNavigationUtil).navigateToBroadcast(activity, BroadcastTransactionDTO(transactionData))
        }
    }

    // Starting with no values to deposit

    @Test
    fun does_not_fund_when_initialized_with_zero() {
        createScenario().onActivity { activity ->

            verify(activity.fundingViewModel, times(0)).fundLightningDeposit(any())
        }

    }

    @Test
    fun disables_send_max() {
        createScenario().onActivity { activity ->

            assertThat(activity.depositAmountView.canSendMax).isFalse()
        }

    }

    // Max / Min Loads
    @Test
    fun fetches_current_lightning_balance() {
        createScenario().onActivity { activity ->
            verify(activity.walletViewModel).fetchLightningBalance()
            verify(activity.walletViewModel.fetchLightningBalance()).observe(activity, activity.lightningBalanceObserver)

            activity.lightningBalanceObserver.onChanged(BTCCurrency(1000L))

            assertThat(activity.lightningBalance!!.toLong()).isEqualTo(1000L)
        }
    }

    @Test
    fun fetches_current_btc_value() {
        createScenario().onActivity { activity ->
            verify(activity.walletViewModel).fetchBtcLatestPrice()
            verify(activity.walletViewModel.fetchBtcLatestPrice()).observe(activity, activity.latestPriceObserver)

            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))

            assertThat(activity.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_500_00)
        }
    }

    @Test
    fun does_not_allow_user_to_deposit_less_than_minimum() {
        val usdCurrency = USDCurrency(LightningDepositActivity.MIN_DEPOSIT_AMOUNT.toLong() - 1)
        val latestPrice = USDCurrency(10_000_00)

        createScenario().onActivity { activity ->
            activity.paymentHolder.updateValue(usdCurrency)
            activity.latestPriceObserver.onChanged(latestPrice)
            activity.confirmed = true
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()), 0, 0, 0, mock(), ""))

            val dialog = activity.supportFragmentManager.findFragmentByTag("INVALID_DEPOSIT_DIALOG")!! as GenericAlertDialog

            assertThat(dialog).isNotNull()
            assertThat(dialog.message).isEqualTo("Attempting to deposit ${usdCurrency.toFormattedCurrency()}. " +
                    "Minimum deposit size at this time is ${LightningDepositActivity.MIN_DEPOSIT_AMOUNT.toFormattedCurrency()}."
            )
            assertThat(activity.confirmed).isFalse()
        }
    }

    @Test
    fun does_not_allow_deposits_to_exceed_maximum_balance() {
        val usdCurrency = USDCurrency(75_00)
        val latestPrice = USDCurrency(10_000_00)
        val balanceValue = USDCurrency(450_00)
        val currentBalance = balanceValue.toBTC(latestPrice)

        createScenario().onActivity { activity ->
            activity.paymentHolder.updateValue(usdCurrency)
            activity.latestPriceObserver.onChanged(latestPrice)
            activity.lightningBalanceObserver.onChanged(currentBalance)
            activity.confirmed = true
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()), 0, 0, 0, mock(), ""))

            val dialog = activity.supportFragmentManager.findFragmentByTag("INVALID_DEPOSIT_DIALOG")!! as GenericAlertDialog

            assertThat(dialog).isNotNull()
            assertThat(dialog.message).isEqualTo("Attempting to deposit ${usdCurrency.toFormattedCurrency()} which would put your account over the maximum balance." +
                    " Your current balance is ${balanceValue.toFormattedCurrency()}. Maximum balance allowed at " +
                    "this time is ${LightningDepositActivity.MAX_DEPOSIT_AMOUNT.toFormattedCurrency()}.")
            assertThat(activity.confirmed).isFalse()
        }
    }

    @Test
    fun does_not_allow_empty_deposits() {
        val usdCurrency = USDCurrency(0)
        val latestPrice = USDCurrency(10_000_00)

        createScenario().onActivity { activity ->
            activity.paymentHolder.updateValue(usdCurrency)
            activity.latestPriceObserver.onChanged(latestPrice)
            activity.confirmed = true
            activity.transactionDataObserver.onChanged(TransactionData(arrayOf(mock()), 0, 0, 0, mock(), ""))

            val dialog = activity.supportFragmentManager.findFragmentByTag("INVALID_DEPOSIT_DIALOG")!! as GenericAlertDialog

            assertThat(dialog).isNotNull()
            assertThat(dialog.message).isEqualTo("Must deposit at least ${LightningDepositActivity.MIN_DEPOSIT_AMOUNT.toFormattedCurrency()}.")
            assertThat(activity.confirmed).isFalse()
        }
    }

    @Module
    class LightningDepositActivityTestModule() {
        @Provides
        fun provideFundingViewModelProvider(): FundingViewModelProvider {
            val fundingViewModelProvider: FundingViewModelProvider = mock()
            val fundingViewModel: FundingViewModel = mock()
            whenever(fundingViewModel.fundLightningDeposit(any())).thenReturn(mock())
            whenever(fundingViewModel.transactionData).thenReturn(mock())
            whenever(fundingViewModelProvider.provide(any())).thenReturn(fundingViewModel)
            return fundingViewModelProvider
        }
    }

}