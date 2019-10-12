package com.coinninja.coinkeeper.cn.transaction

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.libbitcoin.model.DerivationPath
import app.coinninja.cn.libbitcoin.model.TransactionData
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo
import com.coinninja.coinkeeper.service.client.model.CNTransactionNotificationResponse
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import com.nhaarman.mockitokotlin2.*
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
@RunWith(AndroidJUnit4::class)
class TransactionNotificationManagerTest {

    private fun createManager(): TransactionNotificationManager {
        return TransactionNotificationManager(mock(), mock(), mock(), mock(), mock(), mock(), mock())
    }

    @Test
    fun sends_encrypted_notification_when_invite_with_transaction_notification() {
        val transactionNotificationManager = createManager()
        val json = "--v<*>-json--"
        val encryption = "--encryption--"
        val pubkey = PUBKEY
        val invite: InviteTransactionSummary = mock()
        val transactionNotification: TransactionNotification = mock()
        val toUser: UserIdentity = mock()
        whenever(toUser.hash).thenReturn(HASH)
        whenever(invite.toUser).thenReturn(toUser)
        whenever(invite.btcTransactionId).thenReturn(TXID)
        whenever(invite.address).thenReturn(ADDRESS)
        whenever(invite.pubkey).thenReturn(pubkey)
        whenever(invite.transactionNotification).thenReturn(transactionNotification)
        whenever(transactionNotificationManager.transactionNotificationMapper.toEncryptionMessage(invite)).thenReturn(json)
        whenever(transactionNotificationManager.messageEncryptor.encrypt(json, pubkey)).thenReturn(encryption)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(any()))
                .thenReturn(Response.success<CNTransactionNotificationResponse>(null))

        transactionNotificationManager.notifyCnOfFundedInvite(invite)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(TXID, ADDRESS,
                HASH, encryption))
    }

    @Test
    fun sends_empty_notification_when_invite_with_transaction_notification_but_no_pubkey() {
        val transactionNotificationManager = createManager()
        val pubkey: String? = null
        val invite: InviteTransactionSummary = mock()
        val transactionNotification: TransactionNotification = mock()
        val toUser: UserIdentity = mock()
        whenever(toUser.hash).thenReturn(HASH)
        whenever(invite.btcTransactionId).thenReturn(TXID)
        whenever(invite.address).thenReturn(ADDRESS)
        whenever(invite.pubkey).thenReturn(pubkey)
        whenever(invite.transactionNotification).thenReturn(transactionNotification)
        whenever(invite.toUser).thenReturn(toUser)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(any()))
                .thenReturn(Response.success<CNTransactionNotificationResponse>(null))

        transactionNotificationManager.notifyCnOfFundedInvite(invite)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(TXID, ADDRESS,
                HASH, ""))
    }

    @Test
    fun sends_empty_notification_when_invite_has_no_transaction_notification() {
        val transactionNotificationManager = createManager()
        val invite: InviteTransactionSummary = mock()
        val toUser: UserIdentity = mock()
        whenever(toUser.hash).thenReturn(HASH)
        whenever(invite.btcTransactionId).thenReturn(TXID)
        whenever(invite.address).thenReturn(ADDRESS)
        whenever(invite.transactionNotification).thenReturn(null)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(any()))
                .thenReturn(Response.success<CNTransactionNotificationResponse>(null))
        whenever(invite.toUser).thenReturn(toUser)

        transactionNotificationManager.notifyCnOfFundedInvite(invite)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(TXID, ADDRESS,
                HASH, ""))
    }

    @Test
    fun only_saves_notification_for_invite_when_memo_exists() {
        val transactionNotificationManager = createManager()
        val inviteSummary: InviteTransactionSummary = mock()
        val completedInviteDTO = createCompletedInviteDTO()
        val transactionNotification: TransactionNotification = mock()
        completedInviteDTO.memo = ""

        transactionNotificationManager.saveTransactionNotificationLocally(inviteSummary, completedInviteDTO)

        verify(transactionNotificationManager.daoSessionManager, times(0)).insert(any<TransactionNotification>())
        verify(inviteSummary, times(0)).transactionNotification = transactionNotification
        verify(inviteSummary, times(0)).update()
    }

    @Test
    fun saves_transaction_locally_for_given_invite() {
        val transactionNotificationManager = createManager()
        val completedInviteDTO = createCompletedInviteDTO()
        val inviteSummary: InviteTransactionSummary = mock()
        val transactionNotification: TransactionNotification = mock()
        whenever(transactionNotificationManager.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)

        transactionNotificationManager.saveTransactionNotificationLocally(inviteSummary, completedInviteDTO)

        verify(transactionNotification).isShared = true
        verify(transactionNotification).memo = "--memo--"
        verify(transactionNotificationManager.daoSessionManager).insert(transactionNotification)
        verify(inviteSummary).transactionNotification = transactionNotification
        verify(inviteSummary).update()
    }

    @Test
    fun saves_transaction_locally_for_given_transaction() {
        val transactionNotificationManager = createManager()
        val completedBroadcastDTO = createCompletedBroadCastDTO()
        completedBroadcastDTO.transactionId = TXID
        val transactionSummary: TransactionSummary = mock()
        val transactionNotification: TransactionNotification = mock()
        whenever(transactionNotificationManager.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)
        val toUser: UserIdentity = mock()
        whenever(transactionNotificationManager.userIdentityHelper.updateFrom(completedBroadcastDTO.identity!!)).thenReturn(toUser)
        val fromAccount: DropbitMeIdentity = mock()
        val fromUser: UserIdentity = mock()
        whenever(transactionNotificationManager.dropbitAccountHelper.profileForIdentity(toUser)).thenReturn(fromAccount)
        whenever(transactionNotificationManager.userIdentityHelper.updateFrom(fromAccount)).thenReturn(fromUser)

        transactionNotificationManager.saveTransactionNotificationLocally(transactionSummary, completedBroadcastDTO)

        verify(transactionNotification).isShared = true
        verify(transactionNotification).memo = "--memo--"
        verify(transactionNotificationManager.daoSessionManager).insert(transactionNotification)
        verify(transactionSummary).transactionNotification = transactionNotification
        verify(transactionSummary).update()
        verify(transactionNotification).txid = TXID
        verify(transactionNotification).toUser = toUser
        verify(transactionNotification).fromUser = fromUser
        verify(transactionNotification).update()
    }

    @Test
    fun sends_transaction_notification_to_cn() {
        val transactionNotificationManager = createManager()
        val completedBroadcastDTO = createCompletedBroadCastDTO()
        val json = "--transaction-v*-as-json--"
        val encryption = "--encryption--"
        whenever(transactionNotificationManager.transactionNotificationMapper.toEncryptionMessage(completedBroadcastDTO)).thenReturn(json)
        whenever(transactionNotificationManager.messageEncryptor.encrypt(json, completedBroadcastDTO.publicKey)).thenReturn(encryption)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(any()))
                .thenReturn(Response.success(200) as Response<CNTransactionNotificationResponse>)

        transactionNotificationManager.sendTransactionNotificationToReceiver(completedBroadcastDTO)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(
                txid = completedBroadcastDTO.transactionId,
                encrypted_payload = encryption,
                identity_hash = completedBroadcastDTO.identity!!.hash,
                address = completedBroadcastDTO.transactionData.paymentAddress
        ))

    }

    @Test
    fun failures_in_sending_memos_are_logged() {
        val transactionNotificationManager = createManager()
        val completedBroadcastDTO = createCompletedBroadCastDTO()
        val json = "--transaction-v*-as-json--"
        val encryption = "--encryption--"
        whenever(transactionNotificationManager.transactionNotificationMapper.toEncryptionMessage(completedBroadcastDTO)).thenReturn(json)
        whenever(transactionNotificationManager.messageEncryptor.encrypt(json, completedBroadcastDTO.publicKey)).thenReturn(encryption)
        val error = Response.error<CNTransactionNotificationResponse>(400, ResponseBody.create(MediaType.get("plain/text"), ""))
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(any())).thenReturn(error)

        transactionNotificationManager.sendTransactionNotificationToReceiver(completedBroadcastDTO)

        verify(transactionNotificationManager.logger).logError(TransactionNotificationManager::class.java.simpleName,
                "Transaction Notification Post Failed", error)

    }

    private fun createCompletedBroadCastDTO(): CompletedBroadcastDTO {
        val transactionData = TransactionData(
                emptyArray(),
                10000000L,
                100L,
                400000L,
                mock<DerivationPath>(),
                "--pay-address--")
        val broadcastTransactionDTO = BroadcastTransactionDTO(
                transactionData,
                true,
                "--memo--",
                Identity(IdentityType.PHONE, receiverPhone, "--hash--", "Joe"),
                PUBKEY
        )
        return CompletedBroadcastDTO(broadcastTransactionDTO, TXID)
    }

    private fun createCompletedInviteDTO(): CompletedInviteDTO {
        val identity = Identity(IdentityType.PHONE, receiverPhone, "--hash--", "Joe Blow")
        val invitedContact = InvitedContact(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                HASH,
                "",
                ""
        )

        return CompletedInviteDTO(
                identity,
                340000L,
                100000L,
                100L,
                "--memo--",
                true,
                "",
                invitedContact)
    }

    companion object {
        private val HASH = "--hash--"
        private val TXID = "--txid--"
        private val PUBKEY = "--pubkey--"
        private val ADDRESS = "--address--"
        private var receiverPhone = "+13305551111"
    }
}