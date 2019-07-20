package com.coinninja.coinkeeper.model.helpers

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.db.Account
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.DropbitMeIdentityDao
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.enums.AccountStatus
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.service.client.CNUserIdentity
import com.coinninja.coinkeeper.service.client.model.CNUserPatch
import javax.inject.Inject

@Mockable
class DropbitAccountHelper @Inject
internal constructor(internal val daoSessionManager: DaoSessionManager,
                     internal val walletHelper: WalletHelper
) {

    val hasVerifiedAccount: Boolean get() = numVerifiedIdentities > 0

    val preferredIdentity: DropbitMeIdentity?
        get() {
            if (numVerifiedIdentities == 0) return null

            return if (isTwitterVerified) twitterIdentity() else phoneIdentity()
        }

    val hasPrivateAccount: Boolean
        get() = walletHelper.userAccount.isPrivate

    val numVerifiedIdentities: Int
        get() {
            return daoSessionManager.dropbitMeIdentityDao.loadAll().size
        }

    val isPhoneVerified: Boolean
        get() = hasVerifiedAccount &&
                containsIdentityType(IdentityType.PHONE) &&
                phoneIdentity()?.status == AccountStatus.VERIFIED

    val isTwitterVerified: Boolean
        get() = hasVerifiedAccount && containsIdentityType(IdentityType.TWITTER)

    fun updateUserAccount(cnUserPatch: CNUserPatch?) {
        val account = walletHelper.userAccount ?: return
        if (cnUserPatch != null) {
            account.isPrivate = cnUserPatch.isPrivate
            account.update()
        }
    }

    fun updateVerifiedAccount(cnUserAccount: CNUserAccount) {
        val account = walletHelper.userAccount ?: return
        account.isPrivate = cnUserAccount.isPrivate
        account.status = AccountStatus.VERIFIED
        account.update()

        val dropbitMeIdentity = daoSessionManager.newDropbitMeIdentity()
        dropbitMeIdentity.hash = account.phoneNumberHash
        dropbitMeIdentity.identity = account.phoneNumber.toString()
        dropbitMeIdentity.type = IdentityType.PHONE
        dropbitMeIdentity.account = account
        dropbitMeIdentity.handle = account.phoneNumberHash.substring(0, 12)
        daoSessionManager.insert(dropbitMeIdentity)

    }

    fun phoneIdentity(): DropbitMeIdentity? {
        return identityForType(IdentityType.PHONE)
    }

    fun twitterIdentity(): DropbitMeIdentity? {
        return identityForType(IdentityType.TWITTER)
    }

    fun identityForType(identityType: IdentityType): DropbitMeIdentity? {
        return daoSessionManager.dropbitMeIdentityDao.queryBuilder()
                .where(DropbitMeIdentityDao.Properties.Type.eq(identityType.id)).unique()
    }

    private fun containsIdentityType(identityType: IdentityType): Boolean {
        return identityForType(identityType) != null
    }

    fun delete(identity: DropbitMeIdentity?) {
        if (identity == null) return
        val buildDelete = daoSessionManager.dropbitMeIdentityDao.queryBuilder().where(DropbitMeIdentityDao.Properties.Id.eq(identity.id)).buildDelete()
        buildDelete.executeDeleteWithoutDetachingEntities()
    }

    fun clearIdentitiesNotIn(currentIdentities: List<String>) {
        daoSessionManager.dropbitMeIdentityDao.queryBuilder()
                .where(DropbitMeIdentityDao.Properties.ServerId.notIn(currentIdentities))
                .buildDelete().executeDeleteWithoutDetachingEntities()
    }

    fun newFrom(identity: CNUserIdentity) {
        val account = walletHelper.userAccount
        val dropbitMeIdentity = daoSessionManager.newDropbitMeIdentity()
        dropbitMeIdentity.identity = identity.identity
        dropbitMeIdentity.serverId = identity.id
        dropbitMeIdentity.type = IdentityType.from(identity.type)
        dropbitMeIdentity.account = account
        dropbitMeIdentity.handle = identity.handle
        dropbitMeIdentity.hash = identity.hash
        dropbitMeIdentity.status = AccountStatus.from(identity.status)
        daoSessionManager.insert(dropbitMeIdentity)
        if (!AccountStatus.VERIFIED.equals(account.status)) {
            account.status = dropbitMeIdentity.status
        }
        account.update()
    }

    fun updateOrCreateFrom(cnAccount: CNUserAccount): Account {
        val account = walletHelper.userAccount
        account.status = AccountStatus.from(cnAccount.status)
        account.isPrivate = cnAccount.isPrivate
        account.cnUserId = cnAccount.id
        account.update()

        cnAccount.identities?.forEach { identity ->
            updateOrCreateFrom(identity)
        }
        return account
    }

    fun updateOrCreateFrom(cnIdentity: CNUserIdentity) {
        if (AccountStatus.from(cnIdentity.status) == AccountStatus.VERIFIED) {
            val dropbitMeIdentity = identityForType(IdentityType.from(cnIdentity.type))
            if (dropbitMeIdentity == null) {
                newFrom(cnIdentity)
            } else {
                dropbitMeIdentity.apply {
                    cnIdentity.identity?.let {
                        identity = if (it.isEmpty()) identity else it
                    }
                    cnIdentity.handle?.let {
                        handle = it
                    }
                    cnIdentity.status?.let {
                        status = AccountStatus.from(it)
                    }
                    cnIdentity.id?.let {
                        serverId = it
                    }
                    cnIdentity.hash?.let {
                        hash = it
                    }
                    update()
                }
            }
        }
    }

    fun profileForIdentity(toUser: UserIdentity): DropbitMeIdentity? {
        if (toUser.type == IdentityType.PHONE && isPhoneVerified) {
            return phoneIdentity()
        } else {
            return twitterIdentity()
        }
    }

}
