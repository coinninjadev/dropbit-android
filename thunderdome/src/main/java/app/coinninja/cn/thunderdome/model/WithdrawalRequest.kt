package app.coinninja.cn.thunderdome.model

import android.os.Parcel
import android.os.Parcelable
import app.dropbit.annotations.Mockable


@Mockable
class WithdrawalRequest(
        val amount: Long = 0,
        val dropbitFee: Long = 0,
        val networkFee: Long = 0,
        var address: String = "",
        var isEstimate: Boolean = false
) : Parcelable {


    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString() ?: "",
            parcel.readInt() == 1
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(amount)
        parcel.writeLong(dropbitFee)
        parcel.writeLong(networkFee)
        parcel.writeString(address)
        parcel.writeInt(if (isEstimate) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WithdrawalRequest

        if (amount != other.amount) return false
        if (dropbitFee != other.dropbitFee) return false
        if (networkFee != other.networkFee) return false
        if (address != other.address) return false
        if (isEstimate != other.isEstimate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount.hashCode()
        result = 31 * result + dropbitFee.hashCode()
        result = 31 * result + networkFee.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + isEstimate.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<WithdrawalRequest> {
        override fun createFromParcel(parcel: Parcel): WithdrawalRequest {
            return WithdrawalRequest(parcel)
        }

        override fun newArray(size: Int): Array<WithdrawalRequest?> {
            return arrayOfNulls(size)
        }
    }

    fun forPost(): WithdrawalPostRequest = WithdrawalPostRequest(
            value = amount,
            address = address,
            estimate = isEstimate
    )

    data class WithdrawalPostRequest(val value: Long, val address: String, val blocks: Int = 0, val estimate: Boolean = false)

}