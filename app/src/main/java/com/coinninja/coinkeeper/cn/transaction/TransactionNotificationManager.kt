package com.coinninja.coinkeeper.cn.transaction

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.transaction.notification.TransactionNotificationMapper
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionNotification
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor
import javax.inject.Inject

@Mockable
class TransactionNotificationManager @Inject
internal constructor(internal val transactionNotificationMapper: TransactionNotificationMapper,
                     internal val messageEncryptor: MessageEncryptor,
                     internal val daoSessionManager: DaoSessionManager,
                     internal val apiClient: SignedCoinKeeperApiClient,
                     internal val dropbitAccountHelper: DropbitAccountHelper,
                     internal val userIdentityHelper: UserIdentityHelper,
                     internal val logger: CNLogger) {

    fun saveTransactionNotificationLocally(inviteTransactionSummary: InviteTransactionSummary, completedInviteDTO: CompletedInviteDTO) {
        if (!completedInviteDTO.hasMemo()) return

        val transactionNotification = createTransactionNotification(completedInviteDTO.memo,
                completedInviteDTO.isMemoIsShared)

        inviteTransactionSummary.transactionNotification = transactionNotification
        inviteTransactionSummary.update()
    }

    fun notifyCnOfFundedInvite(invite: InviteTransactionSummary) {
        val transactionNotification = invite.transactionNotification

        if (null == transactionNotification || null == invite.pubkey) {
            sendNotificationToCN(invite.btcTransactionId, invite.address, invite.toUser.hash, "")
        } else {
            val message = transactionNotificationMapper.toEncryptionMessage(invite)
            val encryption = messageEncryptor.encrypt(message, invite.pubkey)
            sendNotificationToCN(invite.btcTransactionId, invite.address, invite.toUser.hash, encryption)
        }
    }

    fun saveTransactionNotificationLocally(transactionSummary: TransactionSummary,
                                           completedBroadcastActivityDTO: CompletedBroadcastDTO) {
        if (!completedBroadcastActivityDTO.hasMemo()) return

        val transactionNotification = createTransactionNotification(completedBroadcastActivityDTO.memo,
                completedBroadcastActivityDTO.isMemoShared)
        transactionSummary.transactionNotification = transactionNotification
        transactionNotification.txid = completedBroadcastActivityDTO.transactionId
        completedBroadcastActivityDTO.identity?.let { toIdentity ->
            val toUser: UserIdentity = userIdentityHelper.updateFrom(toIdentity)
            transactionNotification.toUser = toUser
            val fromAccount = dropbitAccountHelper.profileForIdentity(toUser)
            fromAccount?.let {
                transactionNotification.fromUser =  userIdentityHelper.updateFrom(fromAccount)
            }
        }
        transactionNotification.update()
        transactionSummary.update()
    }

    fun createTransactionNotification(memo: String?, isShared: Boolean): TransactionNotification {
        val transactionNotification = daoSessionManager.newTransactionNotification()
        transactionNotification.memo = memo
        transactionNotification.isShared = isShared
        daoSessionManager.insert(transactionNotification)
        return transactionNotification
    }

    fun sendTransactionNotificationToReceiver(completedBroadcastDTO: CompletedBroadcastDTO) {
        val message = transactionNotificationMapper.toEncryptionMessage(completedBroadcastDTO)
        val encryption = messageEncryptor.encrypt(message, completedBroadcastDTO.publicKey)

        sendNotificationToCN(completedBroadcastDTO.transactionId,
                completedBroadcastDTO.transactionData.paymentAddress ?: "",
                completedBroadcastDTO.identity?.hashForType ?: "",
                encryption)
    }

    private fun sendNotificationToCN(transactionId: String?, paymentAddress: String,
                                     identityHash: String, encryption: String) {

        val sharedMemo = CNSharedMemo(transactionId, paymentAddress, identityHash, encryption)
        val response = apiClient.postTransactionNotification(sharedMemo)

        if (!response.isSuccessful) {
            logger.logError(javaClass.simpleName, "Transaction Notification Post Failed", response)
        }
    }

}
