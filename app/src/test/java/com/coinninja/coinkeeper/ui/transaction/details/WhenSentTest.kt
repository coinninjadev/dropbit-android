package com.coinninja.coinkeeper.ui.transaction.details

import android.view.View
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.util.currency.BTCCurrency
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhenSentTest {
    private fun createBindableTransaction(): BindableTransaction = BindableTransaction(
            ApplicationProvider.getApplicationContext(), mock()
    ).also {
        it.sendState = BindableTransaction.SendState.SEND
    }

    private fun createPageAdapter(bindableTransaction: BindableTransaction): TransactionDetailPageAdapter = TransactionDetailPageAdapter(mock(), mock(),
            DefaultCurrencies(USDCurrency(), BTCCurrency()), mock(), mock(), mock()).also {

        whenever(it.transactionAdapterUtil.translateTransaction(any<TransactionsInvitesSummary>())).thenReturn(bindableTransaction)
        whenever(it.walletHelper.latestPrice).thenReturn(USDCurrency(1000.00))
    }

    private fun createScenario(): ActivityScenario<TestableActivity> = ActivityScenario.launch(TestableActivity::class.java).also {
        it.onActivity {
            it.appendLayout(R.layout.page_transaction_detail)
        }
    }

    @Test
    fun receiving_transaction_historic_price_with_invite() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.historicalTransactionUSDValue = 34000L
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = BindableTransaction.InviteState.RECEIVED_PENDING
        bindableTransaction.sendState = BindableTransaction.SendState.RECEIVE
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when received $88.40 at send")
            }
        }
    }

    @Test
    fun transaction_historic_price_with_invite() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.historicalTransactionUSDValue = 34000L
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = BindableTransaction.InviteState.SENT_PENDING
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when received $88.40 when sent")
            }
        }
    }

    @Test
    fun transaction_historic_price_with_invite_zero_value() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 0L
        bindableTransaction.historicalTransactionUSDValue = 34000L
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = BindableTransaction.InviteState.SENT_PENDING
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$88.40 when sent")
            }
        }

    }

    @Test
    fun transaction_historic_price_with_invite_tx_zero_value() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.historicalTransactionUSDValue = 0L
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = BindableTransaction.InviteState.SENT_PENDING
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when sent")
            }
        }

    }

    @Test
    fun only_invite_historic_price_with_invite_when_no_transaction_canceled() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.inviteState = BindableTransaction.InviteState.CANCELED
        bindableTransaction.sendState = BindableTransaction.SendState.SEND_CANCELED
        bindableTransaction.value = 26000000L
        bindableTransaction.txID = null

        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when sent")
            }
        }
    }

    @Test
    fun only_invite_historic_price_when_receiving_with_invite_when_no_transaction_canceled() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.inviteState = BindableTransaction.InviteState.RECEIVED_PENDING
        bindableTransaction.sendState = BindableTransaction.SendState.RECEIVE
        bindableTransaction.value = 26000000L
        bindableTransaction.txID = null

        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when received")
            }
        }
    }

    @Test
    fun only_invite_historic_price_with_invite_when_no_transaction() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.inviteState = BindableTransaction.InviteState.SENT_PENDING
        bindableTransaction.value = 26000000L
        bindableTransaction.txID = null

        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when sent")
            }
        }
    }

    @Test
    fun sending_transaction_historic_price_with_transaction_only() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = null
        bindableTransaction.sendState = BindableTransaction.SendState.RECEIVE
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when received")
            }
        }

    }

    @Test
    fun receiving_transaction_historic_price_with_transaction_only() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = null
        bindableTransaction.sendState = BindableTransaction.SendState.SEND
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("$86.00 when sent")
            }
        }

    }

    @Test
    fun receiving_transaction_historic_price_with_transaction_only_when_no_historical_price() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.txID = "dsr98gy35g987whg98w4tw4809w4hjg80w9s"
        bindableTransaction.value = 26000000L
        bindableTransaction.inviteState = null
        bindableTransaction.sendState = BindableTransaction.SendState.SEND
        bindableTransaction.historicalTransactionUSDValue = 0
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<TextView>(R.id.value_when_sent)

                assertThat(view.text).isEqualTo("")
            }
        }

    }

}