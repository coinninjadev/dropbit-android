package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class TransactionDetail(
        @SerializedName("txid")
        var transactionId: String = "",
        var hash: String? = null,
        var size: Int = 0,
        var vsize: Int = 0,
        var weight: Long = 0,
        var version: Int = 0,
        var locktime: Long = 0,
        @SerializedName("coinbase")
        var isCoinbase: Boolean = false,
        @SerializedName("txinwitness")
        var witnesses: Array<String> = emptyArray(),
        var blockhash: String? = null,
        var height: Int = 0,
        var blockheight: Int = 0,
        var time: Long = 0,
        var blocktime: Long = 0,
        @SerializedName("vin")
        var vInList: List<VIn> = emptyList(),
        @SerializedName("vout")
        var vOutList: List<VOut> = emptyList(),
        @SerializedName("received_time")
        var receivedTime: Long = 0
) {
    val isInBlock: Boolean get() = !blockhash.isNullOrEmpty()
    val blocktimeMillis: Long get() = blocktime * 1000
    val receivedTimeMillis: Long get() = receivedTime * 1000
    val timeMillis: Long get() = time * 1000

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionDetail

        if (transactionId != other.transactionId) return false
        if (hash != other.hash) return false
        if (size != other.size) return false
        if (vsize != other.vsize) return false
        if (weight != other.weight) return false
        if (version != other.version) return false
        if (locktime != other.locktime) return false
        if (isCoinbase != other.isCoinbase) return false
        if (!witnesses.contentEquals(other.witnesses)) return false
        if (blockhash != other.blockhash) return false
        if (height != other.height) return false
        if (blockheight != other.blockheight) return false
        if (time != other.time) return false
        if (blocktime != other.blocktime) return false
        if (vInList != other.vInList) return false
        if (vOutList != other.vOutList) return false
        if (receivedTime != other.receivedTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transactionId.hashCode()
        result = 31 * result + (hash?.hashCode() ?: 0)
        result = 31 * result + size
        result = 31 * result + vsize
        result = 31 * result + weight.hashCode()
        result = 31 * result + version
        result = 31 * result + locktime.hashCode()
        result = 31 * result + isCoinbase.hashCode()
        result = 31 * result + witnesses.contentHashCode()
        result = 31 * result + (blockhash?.hashCode() ?: 0)
        result = 31 * result + height
        result = 31 * result + blockheight
        result = 31 * result + time.hashCode()
        result = 31 * result + blocktime.hashCode()
        result = 31 * result + vInList.hashCode()
        result = 31 * result + vOutList.hashCode()
        result = 31 * result + receivedTime.hashCode()
        return result
    }
}
