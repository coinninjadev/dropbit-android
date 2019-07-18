package com.coinninja.coinkeeper.service.client.model

import android.os.Parcelable
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.service.runner.SentInvitesStatusGetter
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.util.*

@Mockable
@Parcelize
data class AddressLookupResult(
        @SerializedName("phone_number_hash")
        var phoneNumberHash: String? = null,
        var address: String? = null,
        @SerializedName("address_pubkey")
        var addressPubKey: String? = null) : Parcelable {


    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as AddressLookupResult?
        return phoneNumberHash == that?.phoneNumberHash &&
                address == that?.address &&
                addressPubKey == that?.addressPubKey
    }

    override fun hashCode(): Int {
        return Objects.hash(phoneNumberHash, address, addressPubKey)
    }
}
