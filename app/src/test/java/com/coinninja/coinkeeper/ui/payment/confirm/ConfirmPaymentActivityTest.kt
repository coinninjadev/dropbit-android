package com.coinninja.coinkeeper.ui.payment.confirm

import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.RequestInvoice
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.cn.wallet.mode.AccountMode
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.FeesManager
import com.coinninja.coinkeeper.view.subviews.SharedMemoView
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmPaymentActivityTest {

    private val creationIntent: Intent = Intent(ApplicationProvider.getApplicationContext(), ConfirmPaymentActivity::class.java)


    private fun createScenario(paymentHolder: PaymentHolder = PaymentHolder(),
                               toUser: Identity? = null,
                               transactionData: TransactionData? = null,
                               requestInvoice: RequestInvoice? = null,
                               mode: AccountMode = AccountMode.BLOCKCHAIN,
                               withAdjustableFees: Boolean = false,
                               memo: String = ""

    ): ActivityScenario<ConfirmPaymentActivity> {

        val intent = creationIntent
        toUser?.let { paymentHolder.toUser = it }
        transactionData?.let { paymentHolder.transactionData = it }
        requestInvoice?.let { paymentHolder.requestInvoice = it }
        paymentHolder.memo = memo

        intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)

        val scenario = ActivityScenario.launch<ConfirmPaymentActivity>(intent)
        scenario.onActivity { activity ->
            whenever(activity.feesManager.isAdjustableFeesEnabled).thenReturn(withAdjustableFees)
            activity.onAccountModeChanged(mode)
        }
        return scenario
    }

    @Test
    fun pressing_close_button_finishes_payment() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            activity.closeButton.performClick()

            verify(activity.activityNavigationUtil).navigateToHome(activity)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // AMOUNT SENDING
    @Test
    fun payment_amount__renders_as_BLOCKCHAIN() {
        val paymentHolder = PaymentHolder(USDCurrency(10_000_00))
        paymentHolder.defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        paymentHolder.updateValue(USDCurrency(100_00))
        val scenario = createScenario(
                paymentHolder = paymentHolder,
                mode = AccountMode.BLOCKCHAIN
        )

        scenario.onActivity { activity ->
            assertThat(activity.amountView.primaryCurrencyText).isEqualTo("$100.00")
            assertThat(activity.amountView.secondaryCurrencyText).isEqualTo("0.01")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun payment_amount__renders_as_LIGHTNING() {
        val paymentHolder = PaymentHolder(USDCurrency(10_000_00))
        paymentHolder.defaultCurrencies = DefaultCurrencies(USDCurrency(), BTCCurrency())
        paymentHolder.updateValue(USDCurrency(100_00))
        val scenario = createScenario(
                paymentHolder = paymentHolder,
                mode = AccountMode.LIGHTNING
        )

        scenario.onActivity { activity ->
            assertThat(activity.amountView.primaryCurrencyText).isEqualTo("$100.00")
            assertThat(activity.amountView.secondaryCurrencyText).isEqualTo("1,000,000 sats")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // FEE 

    @Test
    fun network_fee__gone_when_LIGHTNING() {
        val scenario = createScenario(
                mode = AccountMode.LIGHTNING
        )

        scenario.onActivity { activity ->
            assertThat(activity.networkFeeView.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun network_fee__renders_when_BLOCKCHAIN() {
        val scenario = createScenario(
                paymentHolder = PaymentHolder(USDCurrency(10_000_00)),
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000)
        )

        scenario.onActivity { activity ->
            assertThat(activity.networkFeeView.visibility).isEqualTo(View.VISIBLE)
            assertThat(activity.networkFeeView.text).isEqualTo("Network fee 0.0001 ($1.00)")
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // FEE SELECTION

    @Test
    fun adjustable_fees__renders_selection() {
        val scenario = createScenario(
                paymentHolder = PaymentHolder(USDCurrency(10_000_00)),
                mode = AccountMode.BLOCKCHAIN,
                transactionData = TransactionData(arrayOf(mock()), 1_000_000, 10_000),
                withAdjustableFees = true
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__renders_updates_when_selection_changes() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                withAdjustableFees = true
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.VISIBLE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__gone_for_BLOCKCHAIN__when_preference_not_set() {
        val scenario = createScenario(
                mode = AccountMode.BLOCKCHAIN,
                withAdjustableFees = false

        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Test
    fun adjustable_fees__gone_for_LIGHTNING() {
        val scenario = createScenario(
                mode = AccountMode.LIGHTNING
        )

        scenario.onActivity { activity ->
            assertThat(activity.adjustableFeesVisibilityGroup.visibility).isEqualTo(View.GONE)
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // CONTACT RENDERING

    @Ignore
    @Test
    fun contact__phone__number() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Ignore
    @Test
    fun contact__phone__number__name() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Ignore
    @Test
    fun contact__twitter__handle__display_name__avatar() {

        val scenario = createScenario()

        scenario.onActivity { activity ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    // MEMO RENDERING

    @Test
    fun memo__renders() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            verify(activity.sharedMemoView).render(activity.sharedMemoViewGroup,
                    activity.paymentHolder.isSharingMemo,
                    memoText = activity.paymentHolder.memo,
                    displayText = activity.paymentHolder.toUser?.displayName
            )

        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Module
    class ConfirmPaymentActivityTestModule() {
        @Provides
        fun sharedMemoView(): SharedMemoView = mock()

        @Provides
        fun feesManager(): FeesManager {
            val feesManager: FeesManager = mock()
            whenever(feesManager.feePreference).thenReturn(FeesManager.FeeType.FAST)
            return feesManager
        }
    }

}