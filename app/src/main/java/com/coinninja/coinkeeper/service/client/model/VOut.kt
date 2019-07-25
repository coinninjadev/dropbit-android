package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class VOut(
        var value: Long = 0,
        @SerializedName("n")
        var index: Int = 0,
        var scriptPubKey: ScriptPubKey = ScriptPubKey()

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VOut

        if (value != other.value) return false
        if (index != other.index) return false
        if (scriptPubKey != other.scriptPubKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.hashCode()
        result = 31 * result + index
        result = 31 * result + scriptPubKey.hashCode()
        return result
    }
}
