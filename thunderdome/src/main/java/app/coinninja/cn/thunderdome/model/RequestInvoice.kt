package app.coinninja.cn.thunderdome.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class RequestInvoice(
        val destination: String = "",

        @SerializedName("payment_hash")
        val paymentHash: String = "",

        @SerializedName("num_satoshis")
        val numSatoshis: Long = 0,

        @SerializedName("time_stamp")
        val timestamp: String = "",

        @SerializedName("expiry")
        val expiry: String = "",

        val description: String = "",

        @SerializedName("description_hash")
        val descriptionHash: String = "",

        @SerializedName("fallback_addr")
        val fallbackAddr: String = "",

        @SerializedName("cltv_expiry")
        val cltvExpiry: String = ""
) : Parcelable {
    var encoded: String = ""

    constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "") {

        encoded = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(destination)
        parcel.writeString(paymentHash)
        parcel.writeLong(numSatoshis)
        parcel.writeString(timestamp)
        parcel.writeString(expiry)
        parcel.writeString(description)
        parcel.writeString(descriptionHash)
        parcel.writeString(fallbackAddr)
        parcel.writeString(cltvExpiry)
        parcel.writeString(encoded)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestInvoice

        if (destination != other.destination) return false
        if (paymentHash != other.paymentHash) return false
        if (numSatoshis != other.numSatoshis) return false
        if (timestamp != other.timestamp) return false
        if (expiry != other.expiry) return false
        if (description != other.description) return false
        if (descriptionHash != other.descriptionHash) return false
        if (fallbackAddr != other.fallbackAddr) return false
        if (cltvExpiry != other.cltvExpiry) return false
        if (encoded != other.encoded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = destination.hashCode()
        result = 31 * result + paymentHash.hashCode()
        result = 31 * result + numSatoshis.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + expiry.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + descriptionHash.hashCode()
        result = 31 * result + fallbackAddr.hashCode()
        result = 31 * result + cltvExpiry.hashCode()
        result = 31 * result + encoded.hashCode()
        return result
    }


    companion object CREATOR : Parcelable.Creator<RequestInvoice> {
        override fun createFromParcel(parcel: Parcel): RequestInvoice {
            return RequestInvoice(parcel)
        }

        override fun newArray(size: Int): Array<RequestInvoice?> {
            return arrayOfNulls(size)
        }
    }
}

