package com.coinninja.coinkeeper.service.client.model

data class CNSharedMemo(
        var txid: String? = null,
        var address: String? = null,
        var identity_hash: String? = null,
        var encrypted_payload: String? = null,
        var encrypted_format: String? = "2"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CNSharedMemo

        if (txid != other.txid) return false
        if (address != other.address) return false
        if (identity_hash != other.identity_hash) return false
        if (encrypted_payload != other.encrypted_payload) return false
        if (encrypted_format != other.encrypted_format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = txid?.hashCode() ?: 0
        result = 31 * result + (address?.hashCode() ?: 0)
        result = 31 * result + (identity_hash?.hashCode() ?: 0)
        result = 31 * result + (encrypted_payload?.hashCode() ?: 0)
        result = 31 * result + (encrypted_format?.hashCode() ?: 0)
        return result
    }
}
