package com.coinninja.coinkeeper.service.client.model

import com.google.gson.annotations.SerializedName

data class AddAddressBodyRequest(
        val address: String = "generate",

        @SerializedName("wallet_address_request_id")
        val walletAddressRequestId: String? = null,

        @SerializedName("address_pubkey")
        val pubKey: String = "",

        @SerializedName("address_type")
        val addressType: String = "btc"
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddAddressBodyRequest

        if (address != other.address) return false
        if (walletAddressRequestId != other.walletAddressRequestId) return false
        if (pubKey != other.pubKey) return false
        if (addressType != other.addressType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + (walletAddressRequestId?.hashCode() ?: 0)
        result = 31 * result + pubKey.hashCode()
        result = 31 * result + addressType.hashCode()
        return result
    }
}
