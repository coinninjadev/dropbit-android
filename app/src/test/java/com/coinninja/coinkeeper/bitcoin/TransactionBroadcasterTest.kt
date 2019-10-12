package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.Transaction
import com.coinninja.coinkeeper.service.client.BlockchainClient
import com.coinninja.coinkeeper.service.client.BlockstreamClient
import com.coinninja.failedHttpResponse
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import retrofit2.Response

class TransactionBroadcasterTest {

    @Test
    fun allows_clients_to_be_added() {
        val broadcaster = TransactionBroadcaster()

        broadcaster.add(mock<BlockchainClient>())
        broadcaster.add(mock<BlockstreamClient>())

        assertThat(broadcaster.clients.size).isEqualTo(2)
    }

    @Test
    fun delegates_broadcast() {
        val broadcaster = TransactionBroadcaster()
        broadcaster.add(mock<BlockchainClient>())
        broadcaster.add(mock<BlockstreamClient>())

        val transaction = Transaction("--txid--", "--raw--")
        broadcaster.broadcast(transaction)

        verify(broadcaster.clients[0]).broadcastTransaction(transaction)
        verify(broadcaster.clients[1]).broadcastTransaction(transaction)
    }

    @Test
    fun builds_single_response__second_failed() {
        val broadcaster = TransactionBroadcaster()
        val transaction = Transaction("--txid--", "--raw--")
        broadcaster.add(mock<BlockchainClient>())
        whenever(broadcaster.clients[0].broadcastTransaction(transaction)).thenReturn(Response.success(ResponseBody.create(MediaType.parse("plain/text"), "OK")))
        whenever(broadcaster.clients[0].broadcastProvider()).thenReturn(BroadcastProvider.BLOCKCHAIN_INFO)
        broadcaster.add(mock<BlockstreamClient>())
        whenever(broadcaster.clients[1].broadcastTransaction(transaction)).thenReturn(failedHttpResponse())
        whenever(broadcaster.clients[1].broadcastProvider()).thenReturn(BroadcastProvider.BLOCK_STREAM)

        val result = broadcaster.broadcast(transaction)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.responseCode).isEqualTo(200)
        assertThat(result.message).isEqualTo("OK")
        assertThat(result.transaction.txid).isEqualTo("--txid--")
        assertThat(result.provider).isEqualTo(BroadcastProvider.BLOCKCHAIN_INFO)
    }

    @Test
    fun builds_single_response__both_succeeded() {
        val broadcaster = TransactionBroadcaster()
        val transaction = Transaction("--txid--", "--raw--")
        broadcaster.add(mock<BlockchainClient>())
        whenever(broadcaster.clients[0].broadcastTransaction(transaction)).thenReturn(Response.success(ResponseBody.create(MediaType.parse("plain/text"), "OK")))
        whenever(broadcaster.clients[0].broadcastProvider()).thenReturn(BroadcastProvider.BLOCKCHAIN_INFO)
        broadcaster.add(mock<BlockstreamClient>())
        whenever(broadcaster.clients[0].broadcastTransaction(transaction)).thenReturn(Response.success(ResponseBody.create(MediaType.parse("plain/text"), "OK")))
        whenever(broadcaster.clients[1].broadcastProvider()).thenReturn(BroadcastProvider.BLOCK_STREAM)

        val result = broadcaster.broadcast(transaction)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.responseCode).isEqualTo(200)
        assertThat(result.message).isEqualTo("OK")
        assertThat(result.provider).isEqualTo(BroadcastProvider.BLOCKCHAIN_INFO)
    }

    @Test
    fun builds_single_response__both_failed() {
        val broadcaster = TransactionBroadcaster()
        val transaction = Transaction("--txid--", "--raw--")
        broadcaster.add(mock<BlockchainClient>())
        whenever(broadcaster.clients[0].broadcastTransaction(transaction)).thenReturn(failedHttpResponse())
        whenever(broadcaster.clients[0].broadcastProvider()).thenReturn(BroadcastProvider.BLOCKCHAIN_INFO)
        broadcaster.add(mock<BlockstreamClient>())
        whenever(broadcaster.clients[1].broadcastTransaction(transaction)).thenReturn(failedHttpResponse("transaction failed"))
        whenever(broadcaster.clients[1].broadcastProvider()).thenReturn(BroadcastProvider.BLOCK_STREAM)

        val result = broadcaster.broadcast(transaction)

        assertThat(result.isSuccess).isFalse()
        assertThat(result.responseCode).isEqualTo(500)
        assertThat(result.message).isEqualTo("transaction failed")
        assertThat(result.provider).isEqualTo(BroadcastProvider.BLOCK_STREAM)
    }
}