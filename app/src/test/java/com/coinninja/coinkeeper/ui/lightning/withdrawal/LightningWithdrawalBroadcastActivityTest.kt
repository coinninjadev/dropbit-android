package com.coinninja.coinkeeper.ui.lightning.withdrawal

import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.thunderdome.model.WithdrawalRequest
import app.dropbit.commons.currency.BTCCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.util.DropbitIntents
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LightningWithdrawalBroadcastActivityTest {
    private val withdrawalRequest
        get() = WithdrawalRequest(
                BTCCurrency(1_000_000),
                BTCCurrency(50_000),
                BTCCurrency(5_000)
        )

    private fun createScenario(): ActivityScenario<LightningWithdrawalBroadcastActivity> {
        val intent = Intent(ApplicationProvider.getApplicationContext(), LightningWithdrawalBroadcastActivity::class.java)
        intent.putExtra(DropbitIntents.EXTRA_WITHDRAWAL_REQUEST, withdrawalRequest)
        return ActivityScenario.launch(intent)
    }

    @Test
    fun broadcasts_when_initialized() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            assertThat(activity.withdrawalRequest).isEqualTo(withdrawalRequest)
            verify(activity.fundingViewModel.lightningWithdrawalCompleted).observe(activity, activity.onCompleteObserver)
            verify(activity.fundingViewModel).processWithdrawal(withdrawalRequest)
        }
        scenario.close()
    }

    @Test
    fun inits_empty() {
        val scenario = createScenario()
        scenario.onActivity { activity ->

            activity.sendingProgressView.also {
                assertThat(it.progress).isEqualTo(0)
                assertThat(it.tag).isEqualTo(R.drawable.ic_sending_check_idle)
            }

            activity.sendingProgressLabel.also {
                assertThat(it.visibility).isEqualTo(VISIBLE)
                assertThat(it.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            }

            assertThat(activity.transactionIdLabel.visibility).isEqualTo(GONE)
            assertThat(activity.transactionIdIcon.visibility).isEqualTo(GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(GONE)

            activity.transactionActionBtn.also {
                assertThat(it.visibility).isEqualTo(GONE)
                assertThat(it.text).isEqualTo(activity.getString(R.string.broadcast_sent_ok))
            }

        }

        scenario.close()
    }

    @Test
    fun shows_complete() {
        val scenario = createScenario()
        scenario.onActivity { activity ->

            activity.onCompleteObserver.onChanged(true)


            activity.sendingProgressView.also {
                assertThat(it.progress).isEqualTo(100)
                assertThat(it.tag).isEqualTo(R.drawable.ic_sending_check_success)
            }

            activity.sendingProgressLabel.also {
                assertThat(it.visibility).isEqualTo(VISIBLE)
                assertThat(it.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            }

            assertThat(activity.transactionIdLabel.visibility).isEqualTo(GONE)
            assertThat(activity.transactionIdIcon.visibility).isEqualTo(GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(GONE)

            activity.transactionActionBtn.also {
                assertThat(it.visibility).isEqualTo(VISIBLE)
                assertThat(it.text).isEqualTo(activity.getString(R.string.broadcast_sent_ok))
            }

        }

        scenario.close()
    }

    @Test
    fun shows_failure() {
        val scenario = createScenario()
        scenario.onActivity { activity ->

            activity.onCompleteObserver.onChanged(false)


            activity.sendingProgressView.also {
                assertThat(it.progress).isEqualTo(100)
                assertThat(it.tag).isEqualTo(R.drawable.ic_sending_x_fail)
            }

            activity.sendingProgressLabel.also {
                assertThat(it.visibility).isEqualTo(VISIBLE)
                assertThat(it.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            }

            assertThat(activity.transactionIdLabel.visibility).isEqualTo(GONE)
            assertThat(activity.transactionIdIcon.visibility).isEqualTo(GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(GONE)

            activity.transactionActionBtn.also {
                assertThat(it.visibility).isEqualTo(VISIBLE)
                assertThat(it.text).isEqualTo(activity.getString(R.string.broadcast_sent_try_again))
            }

        }

        scenario.close()
    }

    @Test
    fun clicking_action_button_navigates_home__when_success() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.onCompleteObserver.onChanged(true)

            activity.transactionActionBtn.performClick()

            verify(activity.activityNavigationUtil).navigateToHome(activity)
        }
        scenario.close()
    }

    @Test
    fun clicking_action_button_retries__when_failed() {
        val scenario = createScenario()
        scenario.onActivity { activity ->
            activity.onCompleteObserver.onChanged(false)

            activity.transactionActionBtn.performClick()

            verify(activity.fundingViewModel, times(2)).processWithdrawal(withdrawalRequest)
        }
        scenario.close()
    }

    @Module
    class LightningWithdrawalBroadcastActivityTestModule {
        @Provides
        fun provideFundingViewModelProvider(): FundingViewModelProvider {
            val fundingViewModelProvider: FundingViewModelProvider = mock()
            val fundingViewModel: FundingViewModel = mock()
            whenever(fundingViewModel.fundLightningDeposit(any())).thenReturn(mock())
            whenever(fundingViewModel.lightningWithdrawalCompleted).thenReturn(mock())
            whenever(fundingViewModelProvider.provide(any())).thenReturn(fundingViewModel)
            return fundingViewModelProvider
        }

    }
}