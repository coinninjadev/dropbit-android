package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.Transaction
import okhttp3.ResponseBody
import retrofit2.Response

interface BroadcastingClient {
    fun broadcastTransaction(transaction: Transaction): Response<ResponseBody>?
    fun broadcastProvider(): BroadcastProvider

}
