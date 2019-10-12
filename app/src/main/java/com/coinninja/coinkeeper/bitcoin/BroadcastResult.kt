package com.coinninja.coinkeeper.bitcoin

import app.coinninja.cn.libbitcoin.model.Transaction
import app.dropbit.annotations.Mockable

@Mockable
data class BroadcastResult(
        var responseCode: Int = 500,
        var isSuccess: Boolean = false,
        var message: String = "",
        var transaction: Transaction = Transaction("", ""),
        var provider: BroadcastProvider = BroadcastProvider.NONE
) {
    val txid: String get() = transaction.txid
    val rawTx: String get() = transaction.encodedTransaction

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BroadcastResult

        if (responseCode != other.responseCode) return false
        if (isSuccess != other.isSuccess) return false
        if (message != other.message) return false
        if (transaction != other.transaction) return false
        if (provider != other.provider) return false

        return true
    }

    override fun hashCode(): Int {
        var result = responseCode
        result = 31 * result + isSuccess.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + transaction.hashCode()
        result = 31 * result + provider.hashCode()
        return result
    }

}


