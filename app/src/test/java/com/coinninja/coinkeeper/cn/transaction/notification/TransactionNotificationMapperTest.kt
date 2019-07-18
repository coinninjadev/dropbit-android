package com.coinninja.coinkeeper.cn.transaction.notification

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionData
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionNotification
import com.coinninja.coinkeeper.model.db.UserIdentity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.model.encryptedpayload.v2.TransactionNotificationV2
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.UserIdentityHelper
import com.google.gson.Gson
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class TransactionNotificationMapperTest {

    private fun setUpMapper(): TransactionNotificationMapper {
        return TransactionNotificationMapper(
                mock(DaoSessionManager::class.java),
                mock(UserIdentityHelper::class.java),
                mock(DropbitAccountHelper::class.java)
        )
    }

    @Test
    fun `converts V1 messages to TransactionNotification`() {
        val mapper = setUpMapper()
        val fromUser = mock(UserIdentity::class.java)
        val phoneIdentity = mock(DropbitMeIdentity::class.java)
        val toUser = mock(UserIdentity::class.java)
        val transactionNotification = mock(TransactionNotification::class.java)

        whenever(mapper.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)
        whenever(mapper.userIdentityHelper
                .updateFrom(Identity(IdentityType.PHONE, "+13305551122", displayName = "Bob"))).thenReturn(fromUser)
        whenever(mapper.dropbitAccountHelper.identityForType(IdentityType.PHONE)).thenReturn(phoneIdentity)
        whenever(mapper.userIdentityHelper.updateFrom(phoneIdentity)).thenReturn(toUser)

        val notification = mapper.toNotification(TestData.V1_Payload_Decrypted)!!

        assertThat(notification, equalTo(transactionNotification))
        val ordered = inOrder(notification, mapper.daoSessionManager)
        ordered.verify(mapper.daoSessionManager).newTransactionNotification()
        verify(notification).memo = "Here's your 5 dollars \uD83D\uDCB8"
        verify(notification).amount = 500L
        verify(notification).amountCurrency = "USD"
        verify(notification).txid = "--txid--"
        verify(notification).isShared = true
        ordered.verify(mapper.daoSessionManager).insert(notification)
        verify(notification).toUser = toUser
        verify(notification).fromUser = fromUser
        verify(notification).update()
    }

    @Test
    fun `converts V2 messages to TransactionNotifications -- TWITTER`() {
        val mapper = setUpMapper()
        val fromUser = mock(UserIdentity::class.java)
        //       whenever(fromUser.handle).thenReturn("aliceandbob")
        val twitterIdentity = mock(DropbitMeIdentity::class.java)
        val toUser = mock(UserIdentity::class.java)
        val transactionNotification = mock(TransactionNotification::class.java)

        whenever(mapper.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)

        whenever(mapper.userIdentityHelper.getOrCreate(IdentityType.TWITTER, "123456789")).thenReturn(fromUser)
        whenever(mapper.dropbitAccountHelper.identityForType(IdentityType.TWITTER)).thenReturn(twitterIdentity)
        whenever(mapper.userIdentityHelper.updateFrom(twitterIdentity)).thenReturn(toUser)

        val notification = mapper.toNotification(TestData.V2_Payload_Decrypted_Twitter)!!

        assertThat(notification, equalTo(transactionNotification))
        val ordered = inOrder(notification, mapper.daoSessionManager)
        ordered.verify(mapper.daoSessionManager).newTransactionNotification()
        verify(notification).memo = "Here's your 5 dollars \uD83D\uDCB8"
        verify(notification).amount = 500L
        verify(notification).amountCurrency = "USD"
        verify(notification).txid = "--txid--"
        verify(notification).isShared = true
        ordered.verify(mapper.daoSessionManager).insert(notification)
        verify(notification).toUser = toUser
        verify(notification).fromUser = fromUser
        verify(notification).update()

        verify(fromUser).displayName = "Alice Bob"
//        verify(fromUser).avatar = "aW5zZXJ0IGF2YXRhciBoZXJlCg=="
        verify(fromUser).update()
    }

    @Test
    fun `converts V2 messages to TransactionNotifications -- PHONE`() {
        val mapper = setUpMapper()
        val fromUser = mock(UserIdentity::class.java)
        whenever(fromUser.handle).thenReturn("")
        val phoneIdentity = mock(DropbitMeIdentity::class.java)
        val toUser = mock(UserIdentity::class.java)
        whenever(toUser.handle).thenReturn("")
        val transactionNotification = mock(TransactionNotification::class.java)

        whenever(mapper.daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification)

        whenever(mapper.userIdentityHelper.getOrCreate(IdentityType.PHONE, "+13305551122")).thenReturn(fromUser)
        whenever(mapper.dropbitAccountHelper.identityForType(IdentityType.PHONE)).thenReturn(phoneIdentity)
        whenever(mapper.userIdentityHelper.updateFrom(phoneIdentity)).thenReturn(toUser)

        val notification = mapper.toNotification(TestData.V2_Payload_Decrypted_Phone)!!

        assertThat(notification, equalTo(transactionNotification))
        val ordered = inOrder(notification, mapper.daoSessionManager)
        ordered.verify(mapper.daoSessionManager).newTransactionNotification()
        verify(notification).memo = "Here's your 5 dollars \uD83D\uDCB8"
        verify(notification).amount = 500L
        verify(notification).amountCurrency = "USD"
        verify(notification).txid = "--txid--"
        verify(notification).isShared = true
        ordered.verify(mapper.daoSessionManager).insert(notification)
        verify(notification).toUser = toUser
        verify(notification).fromUser = fromUser
        verify(notification).update()

        verify(fromUser).displayName = "Alice Bob"
        verify(fromUser).avatar = "aW5zZXJ0IGF2YXRhciBoZXJlCg=="
        verify(fromUser).update()
    }

    @Test
    fun `clears memo when not shared -- Transaction`() {
        val mapper = setUpMapper()
        val toUser = Identity(identityType = IdentityType.TWITTER,
                value = "12345321",
                displayName = "Mr. Anderson",
                handle = "neo")
        val transactionData = TransactionData(
                emptyArray(),
                10000000L,
                100L,
                400000L,
                mock(DerivationPath::class.java),
                "--pay-address--")
        val completedBroadcastDTO = CompletedBroadcastDTO(
                transactionData,
                false, "--memo--",
                toUser,
                "--pubkey--",
                "--txid--")
        completedBroadcastDTO.identity!!.avatarUrl = "--avatar--"
        val myAccount = mock(DropbitMeIdentity::class.java)
        val toUserIdentity = mock(UserIdentity::class.java)
        whenever(mapper.userIdentityHelper.updateFrom(toUser)).thenReturn(toUserIdentity)
        whenever(mapper.dropbitAccountHelper.profileForIdentity(toUserIdentity)).thenReturn(myAccount)
        val fromUserIdentity = mock(UserIdentity::class.java)
        whenever(mapper.userIdentityHelper.updateFrom(myAccount)).thenReturn(fromUserIdentity)
        whenever(fromUserIdentity.identity).thenReturn("987654321")
        whenever(fromUserIdentity.handle).thenReturn("MyTwitterHandle")
        whenever(fromUserIdentity.type).thenReturn(IdentityType.TWITTER)
        whenever(fromUserIdentity.displayName).thenReturn("My Name")

        val json = mapper.toEncryptionMessage(completedBroadcastDTO)
        val v2 = Gson().fromJson(json, TransactionNotificationV2::class.java)

        val info = v2.info!!


        assertThat(info.memo, equalTo(""))
    }

    @Test
    fun `test to V2 from completed broadcast`() {
        val mapper = setUpMapper()
        val toUser = Identity(identityType = IdentityType.TWITTER,
                value = "12345321",
                displayName = "Mr. Anderson",
                handle = "neo")
        val transactionData = TransactionData(
                emptyArray(),
                10000000L,
                100L,
                400000L,
                mock(DerivationPath::class.java),
                "--pay-address--")
        val completedBroadcastDTO = CompletedBroadcastDTO(
                transactionData,
                true, "--memo--",
                toUser,
                "--pubkey--",
                "--txid--")
        completedBroadcastDTO.identity!!.avatarUrl = "--avatar--"
        val myAccount = mock(DropbitMeIdentity::class.java)
        val toUserIdentity = mock(UserIdentity::class.java)
        whenever(mapper.userIdentityHelper.updateFrom(toUser)).thenReturn(toUserIdentity)
        whenever(mapper.dropbitAccountHelper.profileForIdentity(toUserIdentity)).thenReturn(myAccount)
        val fromUserIdentity = mock(UserIdentity::class.java)
        whenever(mapper.userIdentityHelper.updateFrom(myAccount)).thenReturn(fromUserIdentity)
        whenever(fromUserIdentity.identity).thenReturn("987654321")
        whenever(fromUserIdentity.handle).thenReturn("MyTwitterHandle")
        whenever(fromUserIdentity.type).thenReturn(IdentityType.TWITTER)
        whenever(fromUserIdentity.displayName).thenReturn("My Name")

        val json = mapper.toEncryptionMessage(completedBroadcastDTO)
        val v2 = Gson().fromJson(json, TransactionNotificationV2::class.java)

        val meta = v2.meta!!
        val info = v2.info!!
        val profile = v2.profile!!
        assertThat(meta.version, equalTo(2))
        assertThat(v2.txid, equalTo("--txid--"))
        assertThat(info.currency, equalTo("USD"))
        assertThat(info.amount, equalTo(completedBroadcastDTO.transactionData.amount))
        assertThat(info.memo, equalTo("--memo--"))
        assertNull(profile.avatar)
        assertThat(profile.displayName, equalTo("My Name"))
        assertThat(profile.type, equalTo("twitter"))
        assertThat(profile.identity, equalTo("987654321:MyTwitterHandle"))
    }

    @Test
    fun `create V2 from Phone Identity removing memo if not shared invite`() {
        val mapper = setUpMapper()
        val notification = mock(TransactionNotification::class.java)
        whenever(notification.memo).thenReturn("--memo--")
        whenever(notification.isShared).thenReturn(false)
        val invite = mock(InviteTransactionSummary::class.java)
        whenever(invite.transactionNotification).thenReturn(notification)
        whenever(invite.btcTransactionId).thenReturn("--txid--")
        whenever(invite.valueSatoshis).thenReturn(10000L)
        val fromUser = mock(UserIdentity::class.java)
        whenever(invite.fromUser).thenReturn(fromUser)
        whenever(fromUser.type).thenReturn(IdentityType.PHONE)
        whenever(fromUser.identity).thenReturn("+13305551122")
        whenever(fromUser.displayName).thenReturn("Alice")

        val json = mapper.toEncryptionMessage(invite)
        val v2 = Gson().fromJson(json, TransactionNotificationV2::class.java)

        val meta = v2.meta!!
        val info = v2.info!!
        val profile = v2.profile!!
        assertThat(meta.version, equalTo(2))
        assertThat(v2.txid, equalTo("--txid--"))
        assertThat(info.currency, equalTo("USD"))
        assertThat(info.amount, equalTo(invite.valueSatoshis))
        assertThat(info.memo, equalTo(""))
        assertNull(profile.avatar)
        assertThat(profile.displayName, equalTo("Alice"))
        assertThat(profile.type, equalTo("phone"))
        assertThat(profile.identity, equalTo("+13305551122"))
    }

    @Test
    fun `create V2 from Phone Identity invite`() {
        val mapper = setUpMapper()
        val notification = mock(TransactionNotification::class.java)
        whenever(notification.memo).thenReturn("--memo--")
        whenever(notification.isShared).thenReturn(true)
        val invite = mock(InviteTransactionSummary::class.java)
        whenever(invite.transactionNotification).thenReturn(notification)
        whenever(invite.btcTransactionId).thenReturn("--txid--")
        whenever(invite.valueSatoshis).thenReturn(10000L)
        val fromUser = mock(UserIdentity::class.java)
        whenever(invite.fromUser).thenReturn(fromUser)
        whenever(fromUser.type).thenReturn(IdentityType.PHONE)
        whenever(fromUser.identity).thenReturn("+13305551122")
        whenever(fromUser.displayName).thenReturn("Alice")

        val json = mapper.toEncryptionMessage(invite)
        val v2 = Gson().fromJson(json, TransactionNotificationV2::class.java)

        val meta = v2.meta!!
        val info = v2.info!!
        val profile = v2.profile!!
        assertThat(meta.version, equalTo(2))
        assertThat(v2.txid, equalTo("--txid--"))
        assertThat(info.currency, equalTo("USD"))
        assertThat(info.amount, equalTo(invite.valueSatoshis))
        assertThat(info.memo, equalTo("--memo--"))
        assertNull(profile.avatar)
        assertThat(profile.displayName, equalTo("Alice"))
        assertThat(profile.type, equalTo("phone"))
        assertThat(profile.identity, equalTo("+13305551122"))
    }

    @Test
    fun `invite from Twitter Identity Invite`() {
        val mapper = setUpMapper()
        val notification = mock(TransactionNotification::class.java)
        whenever(notification.memo).thenReturn("--memo--")
        whenever(notification.isShared).thenReturn(true)
        val invite = mock(InviteTransactionSummary::class.java)
        whenever(invite.transactionNotification).thenReturn(notification)
        whenever(invite.btcTransactionId).thenReturn("--txid--")
        whenever(invite.valueSatoshis).thenReturn(10000L)
        val fromUser = mock(UserIdentity::class.java)
        whenever(invite.fromUser).thenReturn(fromUser)
        whenever(fromUser.type).thenReturn(IdentityType.TWITTER)
        whenever(fromUser.identity).thenReturn("12344321")
        whenever(fromUser.handle).thenReturn("aliceandbob")
        whenever(fromUser.displayName).thenReturn("Alice")
        whenever(fromUser.avatar).thenReturn("--avatar--")

        val json = mapper.toEncryptionMessage(invite)
        val v2 = Gson().fromJson(json, TransactionNotificationV2::class.java)

        val meta = v2.meta!!
        val info = v2.info!!
        val profile = v2.profile!!
        assertThat(meta.version, equalTo(2))
        assertThat(v2.txid, equalTo("--txid--"))
        assertThat(info.currency, equalTo("USD"))
        assertThat(info.amount, equalTo(invite.valueSatoshis))
        assertThat(info.memo, equalTo("--memo--"))
        assertThat(profile.type, equalTo("twitter"))
        assertThat(profile.identity, equalTo("12344321:aliceandbob"))
        assertThat(profile.displayName, equalTo("Alice"))
        assertThat(profile.avatar, equalTo("--avatar--"))
    }

}