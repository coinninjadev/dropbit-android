package com.coinninja.coinkeeper.service.client.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TransactionFee(@SerializedName("slow")
                          val slow: Double,
                          @SerializedName("med")
                          val med: Double,
                          @SerializedName("fast")
                          val fast: Double) : Parcelable {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TransactionFee

        if (slow != other.slow) return false
        if (med != other.med) return false
        if (fast != other.fast) return false

        return true
    }

    override fun hashCode(): Int {
        var result = slow.hashCode()
        result = 31 * result + med.hashCode()
        result = 31 * result + fast.hashCode()
        return result
    }
}
