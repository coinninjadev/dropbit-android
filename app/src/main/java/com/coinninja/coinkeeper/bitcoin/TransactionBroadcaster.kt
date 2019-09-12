package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.Transaction
import app.dropbit.annotations.Mockable
import okhttp3.ResponseBody
import retrofit2.Response

@Mockable
class TransactionBroadcaster {
    internal val clients: MutableList<BroadcastingClient> = mutableListOf()

    fun add(broadcastingClient: BroadcastingClient) {
        clients.add(broadcastingClient)
    }

    fun broadcast(transaction: Transaction): BroadcastResult {
        val result = BroadcastResult(transaction = transaction)
        clients.forEach {
            assembleResponses(result, it.broadcastTransaction(transaction), it.broadcastProvider())
        }

        return result
    }

    private fun assembleResponses(broadcastResult: BroadcastResult, nullableResponse: Response<Any>?, broadcastProvider: BroadcastProvider) {
        if (broadcastResult.isSuccess) return
        nullableResponse?.let { response ->
            broadcastResult.provider = broadcastProvider
            broadcastResult.responseCode = response.code()
            if (response.isSuccessful) {
                broadcastResult.isSuccess = true
                try {
                    broadcastResult.message = (response.body() as ResponseBody).string()
                } catch (e: java.lang.Exception) {
                    broadcastResult.message = response.message()
                }
            } else {
                try {
                    broadcastResult.message = response.errorBody()?.string()
                            ?: response.message() ?: ""
                } catch (e: java.lang.Exception) {
                    broadcastResult.message = response.message()
                }
            }

        }

    }


}
