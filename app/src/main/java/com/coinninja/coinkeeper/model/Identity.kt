package com.coinninja.coinkeeper.model

import android.os.Parcel
import android.os.Parcelable
import app.dropbit.annotations.Mockable
import app.dropbit.twitter.model.TwitterUser
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.util.Hasher
import kotlinx.android.parcel.Parcelize

@Mockable
@Parcelize
data class Identity(val identityType: IdentityType, val value: String, val hash: String? = null, val displayName: String? = "", val handle: String? = null, val isVerified: Boolean = false, var avatarUrl: String? = null) : Parcelable {

    constructor(contact: Contact) : this(IdentityType.PHONE, contact.getPhoneNumber().toString(), contact.hash, contact.displayName, null, contact.isVerified)
    constructor(twitterUser: TwitterUser) : this(identityType = IdentityType.TWITTER, value = twitterUser.userId.toString(), displayName = twitterUser.name, handle = twitterUser.displayScreenName(), isVerified = false, avatarUrl = twitterUser.profileImage)

    val hashForType: String
        get() {
            return hash ?: generateHashForType()
        }

    val secondaryDisplayName: String
        get() {
            return handle ?: asPhoneNumber().toInternationalDisplayText()
        }

    init {
        when (identityType) {
            IdentityType.PHONE -> {
                if (!PhoneNumber(value).isValid) throw IllegalArgumentException("Value Must be a phone number")
            }
            else -> {
            }
        }
    }

    private fun generateHashForType(): String {
        when (identityType) {
            IdentityType.PHONE -> return Hasher().hash(asPhoneNumber())
            else -> return value
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Identity

        if (identityType != other.identityType) return false
        if (value != other.value) return false
        if (hash != other.hash) return false
        if (displayName != other.displayName) return false
        if (handle != other.handle) return false
        if (avatarUrl != other.avatarUrl) return false

        return true
    }

    private fun asPhoneNumber(): PhoneNumber {
        if (identityType == IdentityType.PHONE)
            return PhoneNumber(value)
        else
            return PhoneNumber()
    }

    override fun describeContents(): Int {
        return 0
    }
}