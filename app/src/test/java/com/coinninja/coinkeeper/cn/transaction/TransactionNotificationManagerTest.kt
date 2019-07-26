package com.coinninja.coinkeeper.cn.transaction

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionData
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.cn.transaction.notification.TransactionNotificationMapper
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.*
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNSharedMemo
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class TransactionNotificationManagerTest {

    private fun createManager(): TransactionNotificationManager {
        return TransactionNotificationManager(
                mock(TransactionNotificationMapper::class.java),
                mock(MessageEncryptor::class.java),
                mock(DaoSessionManager::class.java),
                mock(SignedCoinKeeperApiClient::class.java),
                mock(DropbitAccountHelper::class.java),
                mock(UserIdentityHelper::class.java),
                mock(CNLogger::class.java)
        )
    }

    @Test
    fun `sends encrypted notification when invite with transaction notification`() {
        val transactionNotificationManager = createManager()
        val json = "--v<*>-json--"
        val encryption = "--encryption--"
        val pubkey = PUBKEY
        val invite = mock(InviteTransactionSummary::class.java)
        val transactionNotification = mock(TransactionNotification::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.hash).thenReturn(HASH)
        whenever(invite.toUser).thenReturn(toUser)
        whenever(invite.btcTransactionId).thenReturn(TXID)
        whenever(invite.address).thenReturn(ADDRESS)
        whenever(invite.pubkey).thenReturn(pubkey)
        whenever(invite.transactionNotification).thenReturn(transactionNotification)
        whenever(transactionNotificationManager.transactionNotificationMapper.toEncryptionMessage(invite)).thenReturn(json)
        whenever(transactionNotificationManager.messageEncryptor.encrypt(json, pubkey)).thenReturn(encryption)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(ArgumentMatchers.any(CNSharedMemo::class.java))).thenReturn(Response.success<Any>(null))

        transactionNotificationManager.notifyCnOfFundedInvite(invite)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(TXID, ADDRESS,
                HASH, encryption))
    }

    @Test
    fun `sends empty notification when invite with transaction notification but no pubkey`() {
        val transactionNotificationManager = createManager()
        val pubkey: String? = null
        val invite = mock(InviteTransactionSummary::class.java)
        val transactionNotification = mock(TransactionNotification::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.hash).thenReturn(HASH)
        whenever(invite.btcTransactionId).thenReturn(TXID)
        whenever(invite.address).thenReturn(ADDRESS)
        whenever(invite.pubkey).thenReturn(pubkey)
        whenever(invite.transactionNotification).thenReturn(transactionNotification)
        whenever(invite.toUser).thenReturn(toUser)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(ArgumentMatchers.any(CNSharedMemo::class.java))).thenReturn(Response.success<Any>(null))

        transactionNotificationManager.notifyCnOfFundedInvite(invite)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(TXID, ADDRESS,
                HASH, ""))
    }

    @Test
    fun `sends empty notification when invite has no transaction notification`() {
        val transactionNotificationManager = createManager()
        val invite = mock(InviteTransactionSummary::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.hash).thenReturn(HASH)
        whenever(invite.btcTransactionId).thenReturn(TXID)
        whenever(invite.address).thenReturn(ADDRESS)
        whenever(invite.transactionNotification).thenReturn(null)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(ArgumentMatchers.any(CNSharedMemo::class.java))).thenReturn(Response.success<Any>(null))
        whenever(invite.toUser).thenReturn(toUser)

        transactionNotificationManager.notifyCnOfFundedInvite(invite)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(TXID, ADDRESS,
                HASH, ""))
    }

    @Test
    fun `only saves notification for invite when memo exists`() {
        val transactionNotificationManager = createManager()
        val inviteSummary = mock(InviteTransactionSummary::class.java)
        val completedInviteDTO = createCompletedInviteDTO()
        val transactionNotification = mock(TransactionNotification::class.java)
        completedInviteDTO.memo = ""

        transactionNotificationManager.saveTransactionNotificationLocally(inviteSummary, completedInviteDTO)

        verify(transactionNotificationManager.daoSessionManager, times(0)).insert(ArgumentMatchers.any(TransactionNotification::class.java))
        verify(inviteSummary, times(0)).transactionNotification = transactionNotification
        verify(inviteSummary, times(0)).update()
    }

    @Test
    fun `saves transaction locally for given invite`() {
        val transactionNotificationManager = createManager()
        val completedInviteDTO = createCompletedInviteDTO()
        val inviteSummary = mock(InviteTransactionSummary::class.java)
        val transactionNotification = mock(TransactionNotification::class.java)
        whenever(transactionNotificationManager.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)

        transactionNotificationManager.saveTransactionNotificationLocally(inviteSummary, completedInviteDTO)

        verify(transactionNotification).isShared = true
        verify(transactionNotification).memo = "--memo--"
        verify(transactionNotificationManager.daoSessionManager).insert(transactionNotification)
        verify(inviteSummary).transactionNotification = transactionNotification
        verify(inviteSummary).update()
    }

    @Test
    fun `saves transaction locally for given transaction`() {
        val transactionNotificationManager = createManager()
        val completedBroadcastDTO = createCompletedBroadCastDTO()
        completedBroadcastDTO.transactionId = TXID
        val transactionSummary = mock(TransactionSummary::class.java)
        val transactionNotification = mock(TransactionNotification::class.java)
        whenever(transactionNotificationManager.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)
        val toUser = mock(UserIdentity::class.java)
        whenever(transactionNotificationManager.userIdentityHelper.updateFrom(completedBroadcastDTO.identity!!)).thenReturn(toUser)
        val fromAccount = mock(DropbitMeIdentity::class.java)
        val fromUser = mock(UserIdentity::class.java)
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
    fun `sends transaction notification to cn`() {
        val transactionNotificationManager = createManager()
        val completedBroadcastDTO = createCompletedBroadCastDTO()
        val json = "--transaction-v*-as-json--"
        val encryption = "--encryption--"
        whenever(transactionNotificationManager.transactionNotificationMapper.toEncryptionMessage(completedBroadcastDTO)).thenReturn(json)
        whenever(transactionNotificationManager.messageEncryptor.encrypt(json, completedBroadcastDTO.publicKey)).thenReturn(encryption)
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(ArgumentMatchers.any(CNSharedMemo::class.java))).thenReturn(Response.success(200))

        transactionNotificationManager.sendTransactionNotificationToReceiver(completedBroadcastDTO)

        verify(transactionNotificationManager.apiClient).postTransactionNotification(CNSharedMemo(
                txid = completedBroadcastDTO.transactionId,
                encrypted_payload = encryption,
                identity_hash = completedBroadcastDTO.identity!!.hash,
                address = completedBroadcastDTO.transactionData.paymentAddress
        ))

    }

    @Test
    fun `failures in sending memos are logged`() {
        val transactionNotificationManager = createManager()
        val completedBroadcastDTO = createCompletedBroadCastDTO()
        val json = "--transaction-v*-as-json--"
        val encryption = "--encryption--"
        whenever(transactionNotificationManager.transactionNotificationMapper.toEncryptionMessage(completedBroadcastDTO)).thenReturn(json)
        whenever(transactionNotificationManager.messageEncryptor.encrypt(json, completedBroadcastDTO.publicKey)).thenReturn(encryption)
        val error = Response.error<Any>(400, ResponseBody.create(MediaType.get("plain/text"), ""))
        whenever(transactionNotificationManager.apiClient.postTransactionNotification(ArgumentMatchers.any(CNSharedMemo::class.java))).thenReturn(error)

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
                mock(DerivationPath::class.java),
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