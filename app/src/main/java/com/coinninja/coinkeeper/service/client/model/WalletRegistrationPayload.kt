package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName

@Mockable
data class WalletRegistrationPayload(
        @SerializedName("public_key_string")
        val pubKey: String,

        val flags: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WalletRegistrationPayload

        if (pubKey != other.pubKey) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pubKey.hashCode()
        result = 31 * result + flags.hashCode()
        return result
    }
}
