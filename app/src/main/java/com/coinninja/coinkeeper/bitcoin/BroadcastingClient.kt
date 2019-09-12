package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.Transaction
import retrofit2.Response

interface BroadcastingClient {
    fun broadcastTransaction(transaction: Transaction): Response<Any>?
    fun broadcastProvider(): BroadcastProvider

}
