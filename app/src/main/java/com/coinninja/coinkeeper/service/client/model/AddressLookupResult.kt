package com.coinninja.coinkeeper.service.client.model

import android.os.Parcelable
import app.dropbit.annotations.Mockable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Mockable
@Parcelize
data class AddressLookupResult(
        @SerializedName("phone_number_hash")
        var phoneNumberHash: String = "",
        var address: String = "",
        @SerializedName("address_pubkey")
        var addressPubKey: String = "",
        @SerializedName("address_type")
        var addressType: String = ""
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddressLookupResult

        if (phoneNumberHash != other.phoneNumberHash) return false
        if (address != other.address) return false
        if (addressPubKey != other.addressPubKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = phoneNumberHash.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + addressPubKey.hashCode()
        return result
    }

    fun isBlockChain(): Boolean = addressType == "btc"
    fun isLightning(): Boolean = addressType == "lightning"
}
