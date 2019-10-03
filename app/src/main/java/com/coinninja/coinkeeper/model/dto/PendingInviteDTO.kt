package com.coinninja.coinkeeper.model.dto

import android.os.Parcel
import android.os.Parcelable
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.service.client.model.InvitedContact

@Mockable
data class PendingInviteDTO(
        var identity: Identity,
        var bitcoinPrice: Long = 0,
        var inviteAmount: Long = 0,
        var inviteFee: Long = 0,
        var memo: String = "",
        var isMemoIsShared: Boolean = false,
        var requestId: String = ""

) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Identity::class.java.classLoader),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString()?: "",
            parcel.readByte() != 0.toByte(),
            parcel.readString() ?: "")

    fun hasMemo(): Boolean {
        return memo.isNotEmpty()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(identity, flags)
        parcel.writeLong(bitcoinPrice)
        parcel.writeLong(inviteAmount)
        parcel.writeLong(inviteFee)
        parcel.writeString(memo)
        parcel.writeByte(if (isMemoIsShared) 1 else 0)
        parcel.writeString(requestId)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PendingInviteDTO

        if (identity != other.identity) return false
        if (bitcoinPrice != other.bitcoinPrice) return false
        if (inviteAmount != other.inviteAmount) return false
        if (inviteFee != other.inviteFee) return false
        if (memo != other.memo) return false
        if (isMemoIsShared != other.isMemoIsShared) return false
        if (requestId != other.requestId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identity.hashCode()
        result = 31 * result + bitcoinPrice.hashCode()
        result = 31 * result + inviteAmount.hashCode()
        result = 31 * result + inviteFee.hashCode()
        result = 31 * result + memo.hashCode()
        result = 31 * result + isMemoIsShared.hashCode()
        result = 31 * result + requestId.hashCode()
        return result
    }

    fun completeInviteWith(invitedContact: InvitedContact): CompletedInviteDTO {
        return CompletedInviteDTO(identity, bitcoinPrice, inviteAmount,
                inviteFee, memo, isMemoIsShared, requestId, invitedContact)
    }

    companion object CREATOR : Parcelable.Creator<PendingInviteDTO> {
        override fun createFromParcel(parcel: Parcel): PendingInviteDTO {
            return PendingInviteDTO(parcel)
        }

        override fun newArray(size: Int): Array<PendingInviteDTO?> {
            return arrayOfNulls(size)
        }
    }

}
