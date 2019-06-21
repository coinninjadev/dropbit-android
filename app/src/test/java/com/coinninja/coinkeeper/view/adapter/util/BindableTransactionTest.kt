package com.coinninja.coinkeeper.view.adapter.util

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.util.currency.USDCurrency
import com.coinninja.coinkeeper.view.adapter.util.BindableTransaction.SendState
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class BindableTransactionTest {

    private fun createBindableTransaction(): BindableTransaction {
        val bindableTransaction = BindableTransaction(ApplicationProvider.getApplicationContext(), mock(WalletHelper::class.java))
        whenever(bindableTransaction.walletHelper.latestPrice).thenReturn(USDCurrency(1000.00))
        return bindableTransaction
    }

    @Test
    fun `identifiable target is send to self for transfers`() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.targetAddress = "--address--"
        bindableTransaction.sendState = SendState.TRANSFER
        bindableTransaction.identity = "some body"

        assertThat(bindableTransaction.identifiableTarget, equalTo("Sent to myself"))

        bindableTransaction.sendState = SendState.SEND
        assertThat(bindableTransaction.identifiableTarget, equalTo("some body"))

        bindableTransaction.identity = ""
        assertThat(bindableTransaction.identifiableTarget, equalTo("--address--"))
    }

    @Test
    fun calculatesTotalCryptoForSendState() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.fee = 100L
        bindableTransaction.value = 1000L

        bindableTransaction.sendState = SendState.SEND
        assertThat(bindableTransaction.totalCryptoForSendState().toLong(), equalTo(1100L))

        bindableTransaction.sendState = SendState.RECEIVE
        assertThat(bindableTransaction.totalCryptoForSendState().toLong(), equalTo(1000L))

        bindableTransaction.sendState = SendState.TRANSFER
        assertThat(bindableTransaction.totalCryptoForSendState().toLong(), equalTo(100L))
    }

    @Test
    fun calculatesTotalFiatForSendState() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.fee = 1000L
        bindableTransaction.value = 100000L

        bindableTransaction.sendState = SendState.SEND
        assertThat(bindableTransaction.totalFiatForSendState().toFormattedString(), equalTo("1.01"))

        bindableTransaction.sendState = SendState.RECEIVE
        assertThat(bindableTransaction.totalFiatForSendState().toFormattedString(), equalTo("1"))

        bindableTransaction.sendState = SendState.TRANSFER
        assertThat(bindableTransaction.totalFiatForSendState().toFormattedString(), equalTo("0.01"))
    }

    @Test
    fun simplifies_send_state__basic_direction() {
        val bindableTransaction = createBindableTransaction()
        bindableTransaction.sendState = SendState.SEND
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.SEND))
        bindableTransaction.sendState = SendState.SEND_CANCELED
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.SEND))
        bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_SEND
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.SEND))

        bindableTransaction.sendState = SendState.RECEIVE
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.RECEIVE))
        bindableTransaction.sendState = SendState.RECEIVE_CANCELED
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.RECEIVE))
        bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_RECEIVE
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.RECEIVE))

        bindableTransaction.sendState = SendState.TRANSFER
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.TRANSFER))
        bindableTransaction.sendState = SendState.FAILED_TO_BROADCAST_TRANSFER
        assertThat(bindableTransaction.basicDirection, equalTo(SendState.TRANSFER))
    }
}