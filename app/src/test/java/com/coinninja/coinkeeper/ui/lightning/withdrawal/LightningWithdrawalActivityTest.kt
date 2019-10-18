package com.coinninja.coinkeeper.ui.lightning.withdrawal

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.SatoshiCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningWithdrawalActivityTest {

    private fun createScenario(): ActivityScenario<LightningWithdrawalActivity> {
        return ActivityScenario.launch(LightningWithdrawalActivity::class.java)
    }

    @Test
    fun close_button_navigates_back() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.closeButton.performClick()

            assertThat(activity.isFinishing).isTrue()
        }
        scenario.close()
    }

    @Test
    fun initializes() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            assertThat(activity.withdrawalAmount.canSendMax).isFalse()
            verify(activity.fundingViewModel.lightningWithdrawalDropbitFee).observe(activity, activity.dropbitFeeObserver)
            verify(activity.fundingViewModel.lightningWithdrawalNetworkFee).observe(activity, activity.networkFeeObserver)

            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))

            assertThat(activity.paymentHolder.primaryCurrency.toLong()).isEqualTo(0L)
            assertThat(activity.paymentHolder.evaluationCurrency.toLong()).isEqualTo(10_500_00L)
            assertThat(activity.withdrawalAmount.onValidEntryObserver).isEqualTo(activity.onValidEntryObserver)
        }
        scenario.close()
    }

    @Test
    fun calculates_fees_on_user_input() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.paymentHolder.updateValue(SatoshiCurrency(50000))

            activity.onValidEntryObserver.onValidEntry()

            verify(activity.fundingViewModel).fundLightningWithdrawal(50000)
        }
        scenario.close()
    }

    @Test
    fun updates_dropbit_fees_when_calculation_returns() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))

            activity.dropbitFeeObserver.onChanged(BTCCurrency(5_000))

            assertThat(activity.dropbitFee.text).isEqualTo("5,000 sats ($0.53)")
        }
        scenario.close()
    }

    @Test
    fun updates_network_fees_when_calculation_returns() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))

            activity.networkFeeObserver.onChanged(BTCCurrency(50_000))

            assertThat(activity.networkFee.text).isEqualTo("50,000 sats ($5.25)")
        }
        scenario.close()
    }

    @Test
    fun notifies_user_when_withdrawal_below_minimum() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))
            activity.paymentHolder.updateValue(SatoshiCurrency(39_000))
            activity.dropbitFeeObserver.onChanged(SatoshiCurrency(5_000))
            activity.networkFeeObserver.onChanged(SatoshiCurrency(50_000))
            activity.lightningBalanceObserver.onChanged(SatoshiCurrency(155_000))
            activity.onValidEntryObserver.onValidEntry()

            activity.processWithdrawal()

            val dialog = activity.supportFragmentManager.findFragmentByTag("INVALID_WITHDRAWAL") as GenericAlertDialog
            assertThat(dialog.message).isEqualTo("Must withdraw at least 40,000 sats.")
            assertThat(activity.confirmed).isFalse()
            assertThat(activity.dropbitFee.text).isEqualTo("NA")
            assertThat(activity.networkFee.text).isEqualTo("NA")
        }
        scenario.close()
    }

    @Test
    fun notifies_user_when_withdrawal_amount_zero() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))
            activity.paymentHolder.updateValue(BTCCurrency(0))

            activity.processWithdrawal()

            val dialog = activity.supportFragmentManager.findFragmentByTag("INVALID_WITHDRAWAL") as GenericAlertDialog
            assertThat(dialog.message).isEqualTo("Please enter an amount to withdrawal.")
            assertThat(activity.confirmed).isFalse()
            assertThat(activity.dropbitFee.text).isEqualTo("NA")
            assertThat(activity.networkFee.text).isEqualTo("NA")
        }
        scenario.close()
    }

    @Test
    fun notifies_user_when_that_non_sufficient_funds_for_withdraw() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))
            activity.lightningBalanceObserver.onChanged(USDCurrency(20_00).toBTC(activity.paymentHolder.evaluationCurrency))
            activity.paymentHolder.updateValue(USDCurrency(21_00).toSats(activity.paymentHolder.evaluationCurrency))
            activity.dropbitFeeObserver.onChanged(USDCurrency(10).toBTC(activity.paymentHolder.evaluationCurrency))
            activity.networkFeeObserver.onChanged(USDCurrency(50).toBTC(activity.paymentHolder.evaluationCurrency))

            activity.processWithdrawal()

            val dialog = activity.supportFragmentManager.findFragmentByTag("INVALID_WITHDRAWAL") as GenericAlertDialog
            assertThat(dialog.message).isEqualTo("Amount exceeds usable Lightning balance of 190,476 sats.")
            assertThat(activity.confirmed).isFalse()
        }
        scenario.close()
    }

    @Test
    fun valid_withdraw_request_processes_lightning_withdrawal() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.latestPriceObserver.onChanged(USDCurrency(10_500_00))
            activity.paymentHolder.updateValue(SatoshiCurrency(150_000))
            activity.dropbitFeeObserver.onChanged(BTCCurrency(5_000))
            activity.networkFeeObserver.onChanged(BTCCurrency(50_000))
            activity.lightningBalanceObserver.onChanged(BTCCurrency(205_000))

            activity.processWithdrawal()

            val expectedRequest = WithdrawalRequest(activity.paymentHolder.crypto.toLong(),
                    activity.dropBitFeeValue.toLong(), activity.networkFeeValue.toLong())
            verify(activity.activityNavigationUtil).showWithdrawalCompleted(activity, expectedRequest)
        }
        scenario.close()
    }

    @Module
    class LightningWithdrawalActivityTestModule {
        @Provides
        fun provideFundingViewModelProvider(): FundingViewModelProvider {
            val fundingViewModelProvider: FundingViewModelProvider = mock()
            val fundingViewModel: FundingViewModel = mock()
            whenever(fundingViewModel.fundLightningDeposit(any())).thenReturn(mock())
            whenever(fundingViewModel.transactionData).thenReturn(mock())
            whenever(fundingViewModel.lightningWithdrawalDropbitFee).thenReturn(mock())
            whenever(fundingViewModel.lightningWithdrawalNetworkFee).thenReturn(mock())
            whenever(fundingViewModel.lightningWithdrawalCompleted).thenReturn(mock())
            whenever(fundingViewModelProvider.provide(any())).thenReturn(fundingViewModel)
            return fundingViewModelProvider
        }

    }
}