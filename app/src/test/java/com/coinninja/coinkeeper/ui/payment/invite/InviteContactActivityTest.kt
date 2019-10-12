package com.coinninja.coinkeeper.ui.payment.invite

import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.cn.transaction.FundingViewModelProvider
import com.coinninja.coinkeeper.cn.transaction.notification.FundingViewModel
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import com.coinninja.coinkeeper.util.DropbitIntents
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InviteContactActivityTest {
    private val paymentHolder:PaymentHolder
        get() {
            val holder = PaymentHolder(evaluationCurrency = USDCurrency(10_000))
            holder.updateValue(BTCCurrency(100_000))
            return holder
        }

    private fun createScenario(paymentHolder: PaymentHolder? = null): ActivityScenario<InviteContactActivity> {
        Intent(ApplicationProvider.getApplicationContext(), InviteContactActivity::class.java).also { intent ->
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
    fun initialization__fails_when_no_invite_value() {
        val scenario = createScenario(PaymentHolder())

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
            verify(activity.fundingViewModel).performContactInvite(activity.paymentHolder)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // LIFECYCLE

    @Test
    fun lifecycle__observes_when_resumed() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            verify(activity.fundingViewModel.invitedContactResponse).observe(activity, activity.invitedContactResponseObserver)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun lifecycle__removes_observers_when_paused() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            scenario.moveToState(Lifecycle.State.DESTROYED)

            verify(activity.fundingViewModel.invitedContactResponse).removeObserver(activity.invitedContactResponseObserver)
        }

        scenario.close()
    }

    // SUCCESS

    @Test
    fun success__shows_state() {
        val scenario = createScenario(paymentHolder)

        scenario.onActivity { activity ->
            activity.invitedContactResponseObserver.onChanged(InvitedContact(
                    "--server-id--",
                    1570378663,
                    1570378663,
                    "",
                    "--hash--",
                    "new",
                    "--wallet-id--"
            ))

            assertThat(activity.sendingProgressView.progress).isEqualTo(100)
            assertThat(activity.sendingProgressLabel.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            assertThat(activity.sendingProgressLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionActionBtn.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.text).isEqualTo(activity.getString(R.string.invite_sent_successfully))

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
            activity.invitedContactResponseObserver.onChanged(InvitedContact())


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
            activity.invitedContactResponseObserver.onChanged(InvitedContact())

            activity.transactionActionBtn.performClick()

            assertThat(activity.sendingProgressView.progress).isEqualTo(0)
            assertThat(activity.sendingProgressLabel.text).isEqualTo(activity.getString(R.string.broadcast_sent_label))
            assertThat(activity.sendingProgressLabel.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.transactionIdLabel.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionIdLink.visibility).isEqualTo(View.GONE)
            assertThat(activity.transactionActionBtn.visibility).isEqualTo(View.GONE)
            verify(activity.fundingViewModel, times(2)).performContactInvite(activity.paymentHolder)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }


    @Module
    class InviteContactActivityTestModule() {
        @Provides
        fun fundingViewModelProvider(): FundingViewModelProvider {
            val provider = mock<FundingViewModelProvider>()
            val viewModel: FundingViewModel = mock()
            whenever(provider.provide(any())).thenReturn(viewModel)
            whenever(viewModel.invitedContactResponse).thenReturn(mock())
            return provider
        }

    }

}