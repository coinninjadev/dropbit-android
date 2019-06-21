package com.coinninja.coinkeeper.cn.transaction.notification

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionNotification
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.encryptedpayload.EncryptionPaylaod
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1
import com.coinninja.coinkeeper.model.encryptedpayload.v2.InfoV2
import com.coinninja.coinkeeper.model.encryptedpayload.v2.MetaV2
import com.coinninja.coinkeeper.model.encryptedpayload.v2.ProfileV2
import com.coinninja.coinkeeper.model.encryptedpayload.v2.TransactionNotificationV2
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import com.google.gson.Gson
import javax.inject.Inject

@Mockable
class TransactionNotificationMapper @Inject
constructor(
        internal val daoSessionManager: DaoSessionManager,
        internal val userIdentityHelper: UserIdentityHelper,
        internal val dropbitAccountHelper: DropbitAccountHelper
) {

    fun toNotification(decryptedMessage: String): TransactionNotification? {
        val gson = Gson()
        val payload = gson.fromJson(decryptedMessage, EncryptionPaylaod::class.java)
        var transactionNotification: TransactionNotification? = null

        when (payload.version) {
            1 -> {
                transactionNotification = fromV1(gson.fromJson(decryptedMessage, TransactionNotificationV1::class.java))
            }
            2 -> {
                transactionNotification = fromV2(gson.fromJson(decryptedMessage, TransactionNotificationV2::class.java))
            }
        }

        return transactionNotification
    }

    fun toEncryptionMessage(completedBroadcastDTO: CompletedBroadcastDTO): String {
        completedBroadcastDTO.identity?.let { toUser ->
            val myIdentity = dropbitAccountHelper.profileForIdentity(
                    userIdentityHelper.updateFrom(toUser))

            myIdentity?.let {
                return buildNotificationString(
                        userIdentityHelper.updateFrom(myIdentity), completedBroadcastDTO)
            }
        }
        return ""
    }

    fun toEncryptionMessage(invite: InviteTransactionSummary): String {
        val transactionNotification = invite.transactionNotification
        val fromUser = invite.fromUser
        val identity = if (fromUser.type == IdentityType.PHONE) fromUser.identity
        else "${fromUser.identity}:${fromUser.handle}"
        val memo: String = if (transactionNotification.isShared) (transactionNotification.memo
                ?: "") else ""

        return TransactionNotificationV2(
                meta = MetaV2(),
                txid = invite.btcTransactionId,
                info = InfoV2(
                        memo = memo,
                        amount = invite.valueSatoshis
                ),
                profile = ProfileV2(
                        identity = identity,
                        type = fromUser.type.asString(),
                        displayName = fromUser.displayName,
                        avatar = fromUser.avatar
                )
        ).toString()
    }

    internal fun fromV1(v1: TransactionNotificationV1): TransactionNotification? {
        if (v1.info == null || v1.info.memo.isNullOrEmpty()) {
            return null
        }
        val info = v1.info
        val profile = v1.profile
        val transactionNotification = daoSessionManager.newTransactionNotification()
        transactionNotification.apply {
            txid = v1.txid
            memo = info.memo
            amount = info.amount
            amountCurrency = info.currency
            isShared = true
        }

        daoSessionManager.insert(transactionNotification)

        profile?.let { profile ->
            val phoneNumber = PhoneNumber(profile.countryCode, profile.phoneNumber).toString()
            transactionNotification.fromUser = userIdentityHelper.updateFrom(
                    Identity(IdentityType.PHONE, phoneNumber, displayName = profile.displayName)
            )
            val dropbitMeIdentity = dropbitAccountHelper.identityForType(IdentityType.PHONE)
            dropbitMeIdentity?.let {
                transactionNotification.toUser = userIdentityHelper.updateFrom(dropbitMeIdentity)
            }

            transactionNotification.update()
        }

        return transactionNotification
    }

    internal fun fromV2(v2: TransactionNotificationV2): TransactionNotification? {
        if (v2.info == null || v2.info?.memo == null) {
            return null
        }
        val info = v2.info
        val profile = v2.profile
        val type = profile?.type
        var identity = profile?.identity
        val identityType = IdentityType.from(type)
        if (type.isNullOrEmpty() || identity.isNullOrEmpty() || identityType == IdentityType.UNKNOWN) return null

        val transactionNotification = daoSessionManager.newTransactionNotification()

        transactionNotification.apply {
            txid = v2.txid
            info?.let { info ->
                memo = info.memo
                amount = info.amount
                amountCurrency = info.currency
            }
            isShared = true
        }

        daoSessionManager.insert(transactionNotification)

        val dropbitMeIdentity = dropbitAccountHelper.identityForType(identityType)
        dropbitMeIdentity?.let {
            transactionNotification.toUser = userIdentityHelper.updateFrom(it)
        }

        var handle: String? = null
        if (identityType == IdentityType.TWITTER) {
            val parts = identity.split(":")
            identity = parts[0]
            handle = parts[1]
        }

        val fromUser = userIdentityHelper.getOrCreate(IdentityType.from(type), identity)
        fromUser.apply {
            displayName = profile.displayName
            this.handle = userIdentityHelper.stripAtSymbolFromHandleIfNecessary(handle ?: "")
            profile.avatar?.let {
                avatar = profile.avatar
            }
            update()
        }

        transactionNotification.fromUser = fromUser
        transactionNotification.update()

        return transactionNotification
    }

    private fun buildNotificationString(identity: UserIdentity, completedBroadcastDTO: CompletedBroadcastDTO): String {
        val memo: String =
                if (completedBroadcastDTO.shouldShareMemo() && completedBroadcastDTO.hasMemo())
                    (completedBroadcastDTO.memo ?: "")
                else ""

        val identityValue =
                if (identity.type == IdentityType.PHONE) identity.identity
                else "${identity.identity}:${identity.handle}"

        return TransactionNotificationV2(
                meta = MetaV2(),
                txid = completedBroadcastDTO.transactionId,
                info = InfoV2(
                        memo = memo,
                        amount = completedBroadcastDTO.transactionData.amount
                ),
                profile = ProfileV2(
                        type = identity.type.asString(),
                        identity = identityValue,
                        displayName = identity.displayName
                )
        ).toString()
    }
}
