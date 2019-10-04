package com.coinninja.coinkeeper.ui.lightning.broadcast

import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.thunderdome.model.LedgerInvoice
import app.coinninja.cn.thunderdome.model.RequestInvoice
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.util.DropbitIntents
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BroadcastLightningPaymentActivityTest {
    private val paymentHolder
        get() = PaymentHolder().also {
            it.requestInvoice = RequestInvoice(numSatoshis = 10932)
            it.requestInvoice!!.encoded = "ld-encoded"
        }

    private fun createScenario(paymentHolder: PaymentHolder? = null): ActivityScenario<BroadcastLightningPaymentActivity> {
        Intent(ApplicationProvider.getApplicationContext(), BroadcastLightningPaymentActivity::class.java).also { intent ->
            paymentHolder?.let {
                intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)
            }

            return ActivityScenario.launch(intent)
        }
    }

    // INITIALIZATION

    @Test
    fun initialization__fails_when_no_payment_data() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun initialization__fails_when_no_invoice() {
        val paymentHolder = PaymentHolder()
        paymentHolder.requestInvoice = RequestInvoice()
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            assertThat(activity.isFinishing).isTrue()
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun initialization__clears_and_broadcasts() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            assertThat(activity.sendingProgressView.progress).isEqualTo(0)
            assertThat(activity.sendingProgressLabel.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            assertThat(activity.sendingProgressLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionActionBtn.visibility).isEqualTo(View.GONE)
            verify(activity.fundingViewModel).performLightningPayment("ld-encoded", 10932)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // LIFECYCLE

    @Test
    fun lifecycle__observes_when_resumed() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            verify(activity.fundingViewModel.ledgerInvoice).observe(activity, activity.paidInvoiceCompleteObserver)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun lifecycle__removes_observers_when_paused() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(activity.fundingViewModel.ledgerInvoice).removeObserver(activity.paidInvoiceCompleteObserver)
        }

        scenario.close()
    }

    // SUCCESS

    @Test
    fun success__shows_state() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            activity.paidInvoiceCompleteObserver.onChanged(LedgerInvoice(value = 10932))


            assertThat(activity.sendingProgressView.progress).isEqualTo(100)
            assertThat(activity.sendingProgressLabel.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            assertThat(activity.sendingProgressLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLink.text).isEqualTo("ld-encoded")
            assertThat(activity.transactionActionBtn.visibility).isEqualTo(View.VISIBLE)

            activity.transactionActionBtn.performClick()

            verify(activity.activityNavigationUtil).navigateToHome(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // FAILURE

    @Test
    fun failure__shows_state() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            activity.paidInvoiceCompleteObserver.onChanged(LedgerInvoice(value = 0))


            assertThat(activity.sendingProgressView.progress).isEqualTo(100)
            assertThat(activity.sendingProgressLabel.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            assertThat(activity.sendingProgressLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionActionBtn.text).isEqualTo(activity.getString(R.string.broadcast_sent_try_again))
            assertThat(activity.transactionActionBtn.visibility).isEqualTo(View.VISIBLE)

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun failure__retry_clears_and_rebroadcasts() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            activity.paidInvoiceCompleteObserver.onChanged(LedgerInvoice(value = 0))

            activity.transactionActionBtn.performClick()

            assertThat(activity.sendingProgressView.progress).isEqualTo(0)
            assertThat(activity.sendingProgressLabel.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            assertThat(activity.sendingProgressLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionActionBtn.visibility).isEqualTo(View.GONE)
            verify(activity.fundingViewModel, times(2)).performLightningPayment("ld-encoded", 10932)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class BroadcastLightningPaymentActivityTestModule {
        @Provides
        fun fundingViewModelProvider(): FundingViewModelProvider {
            val provider = mock<FundingViewModelProvider>()
            val viewModel: FundingViewModel = mock()
            whenever(provider.provide(any())).thenReturn(viewModel)
            whenever(viewModel.ledgerInvoice).thenReturn(mock())
            return provider
        }

    }

}