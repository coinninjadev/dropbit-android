package com.coinninja.coinkeeper.service.client.model

import com.google.gson.annotations.SerializedName

data class ReplaceWalletRequest(
        @SerializedName("public_key_string")
        val publicKey: String,
        val flags: Long,
        val timestamp: String,
        val signature: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReplaceWalletRequest

        if (publicKey != other.publicKey) return false
        if (flags != other.flags) return false
        if (timestamp != other.timestamp) return false
        if (signature != other.signature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + flags.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + signature.hashCode()
        return result
    }
}
