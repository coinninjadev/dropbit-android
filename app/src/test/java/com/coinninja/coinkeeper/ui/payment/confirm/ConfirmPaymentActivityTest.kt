package com.coinninja.coinkeeper.ui.payment.confirm

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.TransactionData
import app.coinninja.cn.thunderdome.model.RequestInvoice
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PaymentHolder
import com.coinninja.coinkeeper.util.DropbitIntents
import com.nhaarman.mockitokotlin2.verify
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmPaymentActivityTest {

    private val creationIntent: Intent = Intent(ApplicationProvider.getApplicationContext(), ConfirmPaymentActivity::class.java)


    private fun createScenario(paymentHolder: PaymentHolder = PaymentHolder(),
                               toUser: Identity? = null,
                               transactionData: TransactionData? = null,
                               requestInvoice: RequestInvoice? = null
    ): ActivityScenario<ConfirmPaymentActivity> {

        val intent = creationIntent
        toUser?.let { paymentHolder.toUser = it }
        transactionData?.let { paymentHolder.transactionData = it }
        requestInvoice?.let { paymentHolder.requestInvoice = it }
        intent.putExtra(DropbitIntents.EXTRA_PAYMENT_HOLDER, paymentHolder)

        return ActivityScenario.launch(intent)
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

    // FEE SELECTION

    @Ignore
    @Test
    fun fee_selection__renders_selection() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Ignore
    @Test
    fun fee_selection__renders_updates_when_selection_changes() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Ignore
    @Test
    fun fee_selection__gone_for_LND() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

    @Ignore
    @Test
    fun memo__gone_when_empty() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Ignore
    @Test
    fun memo__shows_memo() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }

    @Ignore
    @Test
    fun memo__shows_memo_shared() {
        val scenario = createScenario()

        scenario.onActivity { activity ->
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        scenario.moveToState(Lifecycle.State.DESTROYED)
        scenario.close()
    }
}