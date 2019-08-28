package com.coinninja.coinkeeper.service.client.model

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.WalletFlags
import com.google.gson.annotations.SerializedName

@Mockable
data class CNWallet(
        var id: String = "",

        @SerializedName("public_key_string")
        var publicKeyString: String = "",

        @SerializedName("created_at")
        internal var createdDate: Long = 0,

        @SerializedName("updated_at")
        internal var updatedDate: Long = 0,

        var flags: Long = 0
) {
    val updatedAtMillis: Long get() = updatedDate * 1000
    val createdAtMillis: Long get() = createdDate * 1000
    val walletFlags: WalletFlags get() = WalletFlags(flags)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CNWallet

        if (id != other.id) return false
        if (publicKeyString != other.publicKeyString) return false
        if (createdDate != other.createdDate) return false
        if (updatedDate != other.updatedDate) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + publicKeyString.hashCode()
        result = 31 * result + createdDate.hashCode()
        result = 31 * result + updatedDate.hashCode()
        result = 31 * result + flags.hashCode()
        return result
    }
}
