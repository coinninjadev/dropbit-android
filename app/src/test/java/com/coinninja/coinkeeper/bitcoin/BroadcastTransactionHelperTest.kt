package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.Transaction
import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.json.JSONObject
import org.junit.Test

class BroadcastTransactionHelperTest {

    private val transactionData: TransactionData = TransactionData(arrayOf(mock(), mock()), 1000, 10, 100,
            DerivationPath(49, 0, 0, 0, 0), "-- address --")
    private val transaction = Transaction("--raw--", "--txid--")

    private fun createHelper(): BroadcastTransactionHelper {
        val helper = BroadcastTransactionHelper(mock(), mock(), mock(), mock())
        whenever(helper.hdWallet.transactionFrom(any())).thenReturn(transaction)
        return helper
    }

    @Test
    fun sending_successful_broadcast() {
        val helper = createHelper()
        whenever(helper.transactionBroadcaster.broadcast(transaction)).thenReturn(BroadcastResult(responseCode = 200, isSuccess = true, transaction = transaction, message = "OK"))

        val result = helper.broadcast(transactionData)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.responseCode).isEqualTo(200)
        assertThat(result.txid).isEqualTo(transaction.txid)

        val proprietiesCaptor = argumentCaptor<JSONObject>()
        verify(helper.analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_COMPLETE), proprietiesCaptor.capture())
        val jsonObject = proprietiesCaptor.firstValue
        assertThat(jsonObject.getInt(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE)).isEqualTo(200)
        assertThat(jsonObject.getString(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG)).isEqualTo("OK")
    }

    @Test
    fun blockchain_request_failed() {
        val helper = createHelper()
        val message = "Transaction already exists."
        whenever(helper.transactionBroadcaster.broadcast(transaction))
                .thenReturn(BroadcastResult(message = message, transaction = transaction))

        val result = helper.broadcast(transactionData)

        assertThat(result.isSuccess).isEqualTo(false)
        assertThat(result.message).isEqualTo(message)

        val proprietiesCaptor = argumentCaptor<JSONObject>()
        verify(helper.analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_FAILED), proprietiesCaptor.capture())
        val jsonObject = proprietiesCaptor.firstValue
        assertThat(jsonObject.getInt(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_CODE)).isEqualTo(500)
        assertThat(jsonObject.getString(Analytics.EVENT_BROADCAST_JSON_KEY_BLOCK_MSG)).isEqualTo(message)
    }

    @Test
    fun fails_transaction_broadcast_when_tx_data_invalid() {
        val helper = createHelper()

        helper.broadcast(TransactionData(emptyArray(), 0, 0, 0, mock(), ""))

        verify(helper.analytics).trackEvent(eq(Analytics.EVENT_BROADCAST_FAILED), any())
    }

}