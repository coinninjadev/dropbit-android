package com.coinninja.coinkeeper.model.dto

import android.os.Parcel
import android.os.Parcelable
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.service.client.model.InvitedContact

@Mockable
data class CompletedInviteDTO(
        var identity: Identity,
        var bitcoinPrice: Long = 0,
        var inviteAmount: Long = 0,
        var inviteFee: Long = 0,
        var memo: String = "",
        var isMemoIsShared: Boolean = false,
        var requestId: String = "",
        val invitedContact: InvitedContact? = null

) : Parcelable {

    val cnId: String? get() = invitedContact?.id

    constructor(parcel: Parcel) : this(
            identity = parcel.readParcelable(Identity::class.java.classLoader),
            bitcoinPrice = parcel.readLong(),
            inviteAmount = parcel.readLong(),
            inviteFee = parcel.readLong(),
            memo = parcel.readString() ?: "",
            isMemoIsShared = parcel.readByte() != 0.toByte(),
            requestId = parcel.readString() ?: "",
            invitedContact = parcel.readParcelable(InvitedContact::class.java.classLoader)) {
    }


    fun hasMemo(): Boolean {
        return memo.isNotEmpty()
    }

    fun hasCnId(): Boolean {
        invitedContact?.let { contact ->
            return contact.id.isNotEmpty()
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CompletedInviteDTO

        if (identity != other.identity) return false
        if (bitcoinPrice != other.bitcoinPrice) return false
        if (inviteAmount != other.inviteAmount) return false
        if (inviteFee != other.inviteFee) return false
        if (memo != other.memo) return false
        if (isMemoIsShared != other.isMemoIsShared) return false
        if (requestId != other.requestId) return false
        if (invitedContact != other.invitedContact) return false

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
        result = 31 * result + (invitedContact?.hashCode() ?: 0)
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(identity, flags)
        parcel.writeLong(bitcoinPrice)
        parcel.writeLong(inviteAmount)
        parcel.writeLong(inviteFee)
        parcel.writeString(memo)
        parcel.writeByte(if (isMemoIsShared) 1 else 0)
        parcel.writeString(requestId)
        parcel.writeParcelable(invitedContact, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CompletedInviteDTO> {
        override fun createFromParcel(parcel: Parcel): CompletedInviteDTO {
            return CompletedInviteDTO(parcel)
        }

        override fun newArray(size: Int): Array<CompletedInviteDTO?> {
            return arrayOfNulls(size)
        }
    }


}
