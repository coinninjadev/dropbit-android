package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.UserIdentityDao
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.encryptedpayload.v2.TransactionNotificationV2
import com.coinninja.coinkeeper.service.client.model.InviteMetadata
import javax.inject.Inject

@Mockable
class UserIdentityHelper @Inject constructor(internal val daoSessionManager: DaoSessionManager) {

    val twitterIdentities: List<UserIdentity>
        get() = daoSessionManager.userIdentityDao
                .queryBuilder()
                .where(UserIdentityDao.Properties.Type.eq(IdentityType.TWITTER.id))
                .list()

    val all: List<UserIdentity> get() = daoSessionManager.userIdentityDao.loadAll()

    val namelessPhoneIdentities: List<UserIdentity>
        get() =
            daoSessionManager.userIdentityDao.queryBuilder()
                    .where(UserIdentityDao.Properties.Type.eq(IdentityType.PHONE.id))
                    .whereOr(
                            UserIdentityDao.Properties.DisplayName.isNull,
                            UserIdentityDao.Properties.DisplayName.eq("")
                    ).list()

    fun getOrCreate(type: IdentityType, identity: String): UserIdentity {
        var userIdentity = daoSessionManager.userIdentityDao.queryBuilder()
                .where(UserIdentityDao.Properties.Type.eq(type.id),
                        UserIdentityDao.Properties.Identity.eq(identity)
                ).unique()

        if (userIdentity == null) {
            userIdentity = daoSessionManager.newUserIdentity()
            userIdentity.type = type
            userIdentity.identity = identity
            daoSessionManager.insert(userIdentity)
        }

        return userIdentity
    }

    fun updateFrom(inviteContact: InviteMetadata.MetadataContact): UserIdentity {
        val type = IdentityType.from(inviteContact.type)
        var identity = inviteContact.identity
        var hash: String? = null

        if (type == IdentityType.PHONE) {
            identity = "+$identity"
            hash = PhoneNumber(identity).toHash()
        }

        if (type == IdentityType.TWITTER) {
            hash = identity
        }

        val userIdentity = getOrCreate(type, identity)
        userIdentity.apply {
            handle = stripAtSymbolFromHandleIfNecessary(inviteContact.handle)
            this.hash = hash
            update()
        }
        return userIdentity
    }

    fun updateFrom(dropbitMeIdentity: DropbitMeIdentity): UserIdentity {
        val userIdentity = getOrCreate(dropbitMeIdentity.type, dropbitMeIdentity.identity)
        userIdentity.apply {
            hash = dropbitMeIdentity.hash
            handle = stripAtSymbolFromHandleIfNecessary(dropbitMeIdentity.handle)
            update()
        }
        return userIdentity
    }

    fun updateFrom(identity: Identity): UserIdentity {
        val userIdentity = getOrCreate(identity.identityType, identity.value)
        val handle = stripAtSymbolFromHandleIfNecessary(identity.handle ?: "")
        userIdentity.apply {
            hash = identity.hashForType
            displayName = identity.displayName
            this.handle = handle
            update()
        }

        return userIdentity
    }

    fun stripAtSymbolFromHandleIfNecessary(handle: String?): String {
        handle?.let { handle ->
            if (!handle.startsWith("@")) { return handle }
            return handle.replaceFirst("@", "")
        }

        return ""
    }
}

