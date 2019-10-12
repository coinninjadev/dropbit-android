package com.coinninja.coinkeeper.ui.transaction.details

import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.commons.currency.BTCCurrency
import app.dropbit.commons.currency.USDCurrency
import com.coinninja.coinkeeper.R
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.ui.base.TestableActivity
import com.coinninja.coinkeeper.util.DefaultCurrencies
import com.coinninja.coinkeeper.view.ConfirmationsView
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ConfirmationBeadsTest {
    private fun createBindableTransaction(): BindableTransaction = BindableTransaction(
            ApplicationProvider.getApplicationContext(), mock()
    ).also {
        it.sendState = BindableTransaction.SendState.SEND
    }

    private fun createPageAdapter(bindableTransaction: BindableTransaction): TransactionDetailPageAdapter =
            TransactionDetailPageAdapter(mock(), mock(), mock(), mock(), mock()).also {

        whenever(it.transactionAdapterUtil.translateTransaction(any<TransactionsInvitesSummary>())).thenReturn(bindableTransaction)
        whenever(it.walletHelper.latestPrice).thenReturn(USDCurrency(1000.00))
    }

    private fun createScenario(): ActivityScenario<TestableActivity> = ActivityScenario.launch(TestableActivity::class.java).also {
        it.onActivity {
            it.appendLayout(R.layout.page_transaction_detail)
        }
    }

    @Test
    fun confirmation_beads__visible_when_pending() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.historicalTransactionUSDValue = 34000L
        bindableTransaction.txID = "--txid--"
        bindableTransaction.value = 26000000L
        bindableTransaction.confirmationState = BindableTransaction.ConfirmationState.UNCONFIRMED
        bindableTransaction.sendState = BindableTransaction.SendState.RECEIVE
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<ConfirmationsView>(R.id.confirmation_beads)

                assertThat(view.visibility).isEqualTo(View.VISIBLE)
            }
        }
    }

    @Test
    fun confirmation_beads__gone_when_complete() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.historicalInviteUSDValue = 8600L
        bindableTransaction.historicalTransactionUSDValue = 34000L
        bindableTransaction.txID = "--txid--"
        bindableTransaction.value = 26000000L
        bindableTransaction.confirmationState = BindableTransaction.ConfirmationState.CONFIRMED
        bindableTransaction.sendState = BindableTransaction.SendState.RECEIVE
        val adapter = createPageAdapter(bindableTransaction)

        createScenario().onActivity { activity ->
            activity.findViewById<View>(R.id.page).also {
                adapter.bindTo(it, bindableTransaction, 0)

                val view = it.findViewById<ConfirmationsView>(R.id.confirmation_beads)

                assertThat(view.visibility).isEqualTo(View.GONE)
            }
        }
    }
}