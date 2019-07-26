package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class VIn(
        var txid: String = "",
        var vOut: Int = 0,
        var scriptSig: ScriptSig = ScriptSig(),
        @SerializedName("txinwitness")
        var transactionWitnesses: Array<String> = emptyArray(),
        var sequence: Long = 0,
        @SerializedName("previousoutput")
        var previousOutput: VOut = VOut()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VIn

        if (txid != other.txid) return false
        if (vOut != other.vOut) return false
        if (scriptSig != other.scriptSig) return false
        if (!transactionWitnesses.contentEquals(other.transactionWitnesses)) return false
        if (sequence != other.sequence) return false
        if (previousOutput != other.previousOutput) return false

        return true
    }

    override fun hashCode(): Int {
        var result = txid.hashCode()
        result = 31 * result + vOut
        result = 31 * result + scriptSig.hashCode()
        result = 31 * result + transactionWitnesses.contentHashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + previousOutput.hashCode()
        return result
    }
}
