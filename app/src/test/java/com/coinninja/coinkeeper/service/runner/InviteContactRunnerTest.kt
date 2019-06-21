package com.coinninja.coinkeeper.service.runner

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.Identity
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.InviteUserPayload
import com.coinninja.coinkeeper.service.client.model.InvitedContact
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.DropbitIntents
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class InviteContactRunnerTest {

    fun createRunner(): InviteContactRunner {
        val inviteContactRunner = InviteContactRunner(
                mock(SignedCoinKeeperApiClient::class.java),
                mock(DropbitAccountHelper::class.java),
                mock(InviteTransactionSummaryHelper::class.java),
                mock(CNLogger::class.java)
        )

        val uuid = "23742734-23742734-23472734-23742734"
        val identity = Identity(identityType = IdentityType.PHONE, hash = "--hash--", value = "+13305551111")
        val pendingInviteDTO = PendingInviteDTO(identity, 50000L, 10000L, 100L, "", false, uuid)
        val phoneIdentity = mock(DropbitMeIdentity::class.java)
        whenever(phoneIdentity.type).thenReturn(IdentityType.PHONE)
        whenever(phoneIdentity.identity).thenReturn("+13305551111")
        whenever(inviteContactRunner.dropbitAccountHelper.phoneIdentity()).thenReturn(phoneIdentity)
        inviteContactRunner.setPendingInviteDTO(pendingInviteDTO)
        return inviteContactRunner
    }


    @Test
    fun send_invite_to_contact_test() {
        val runner = createRunner()
        val contact = mock(InvitedContact::class.java)
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Some User")
        val mockResponse = Response.success(contact)
        whenever(runner.client.inviteUser(ArgumentMatchers.any(InviteUserPayload::class.java))).thenReturn(mockResponse)
        runner.setOnInviteListener(mock(InviteContactRunner.OnInviteListener::class.java))

        runner.execute(identity)

        verify(runner.onInviteListener!!).onInviteSuccessful(contact)
    }

    @Test
    fun send_invite_to_contact_fail() {
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Some User")
        val runner = createRunner()

        val body = ResponseBody.create(MediaType.parse("text"), "bad request")
        val mockResponse = Response.error<Any>(400, body)

        whenever(runner.client.inviteUser(ArgumentMatchers.any(InviteUserPayload::class.java))).thenReturn(mockResponse)
        runner.setOnInviteListener(mock(InviteContactRunner.OnInviteListener::class.java))

        runner.execute(identity)

        verify(runner.onInviteListener!!).onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_UNKNOWN, "bad request")
    }

    @Test
    fun send_dropbit_rate_limit_error() {
        val identity = Identity(IdentityType.PHONE, "+13305551111", "--hash--", "Some User")
        val runner = createRunner()

        val body = ResponseBody.create(MediaType.parse("text"), "rate limit error")
        val mockResponse = Response.error<Any>(429, body)

        whenever(runner.client.inviteUser(ArgumentMatchers.any(InviteUserPayload::class.java))).thenReturn(mockResponse)
        runner.setOnInviteListener(mock(InviteContactRunner.OnInviteListener::class.java))

        runner.execute(identity)

        verify(runner.onInviteListener!!).onInviteError(DropbitIntents.ACTION_DROPBIT__ERROR_RATE_LIMIT, "rate limit error")
    }

    @Test
    fun invites_twitter_with_handle_from_twitter() {
        val runner = createRunner()
        whenever(runner.client.inviteUser(ArgumentMatchers.any(InviteUserPayload::class.java))).thenReturn(Response.success(mock(InvitedContact::class.java)))
        val captor = ArgumentCaptor.forClass(InviteUserPayload::class.java)
        whenever(runner.dropbitAccountHelper.isTwitterVerified).thenReturn(true)
        val twitterIdentity = mock(DropbitMeIdentity::class.java)
        whenever(runner.dropbitAccountHelper.twitterIdentity()).thenReturn(twitterIdentity)
        whenever(twitterIdentity.handle).thenReturn("MyHandle")
        whenever(twitterIdentity.identity).thenReturn("5432112345")
        whenever(twitterIdentity.type).thenReturn(IdentityType.TWITTER)
        val identity = Identity(IdentityType.TWITTER, "1234567890", "1234567890", "Some User")
        runner.execute(identity)

        verify(runner.client).inviteUser(captor.capture())
        val (amount, sender, receiver) = captor.value

        assertThat(amount.btc, equalTo(10000L))
        assertThat(amount.usd, equalTo(5L))

        assertThat<String>(receiver.handle, equalTo("Some User"))
        assertThat(receiver.identity, equalTo("1234567890"))
        assertThat(receiver.type, equalTo("twitter"))

        assertThat<String>(sender.handle, equalTo("MyHandle"))
        assertThat(sender.identity, equalTo("5432112345"))
        assertThat(sender.type, equalTo("twitter"))
    }

    @Test
    fun invites_phone_with_out_handle_from_phone() {
        val runner = createRunner()
        whenever(runner.client.inviteUser(ArgumentMatchers.any(InviteUserPayload::class.java))).thenReturn(Response.success(mock(InvitedContact::class.java)))
        val captor = ArgumentCaptor.forClass(InviteUserPayload::class.java)
        whenever(runner.dropbitAccountHelper.isPhoneVerified).thenReturn(true)
        val phoneIdentity = mock(DropbitMeIdentity::class.java)
        whenever(runner.dropbitAccountHelper.phoneIdentity()).thenReturn(phoneIdentity)
        whenever(phoneIdentity.identity).thenReturn("+13305551111")
        whenever(phoneIdentity.type).thenReturn(IdentityType.PHONE)
        val identity = Identity(IdentityType.PHONE, "+13305550000", "--hash--", null)
        runner.execute(identity)

        verify<SignedCoinKeeperApiClient>(runner.client).inviteUser(captor.capture())
        val (amount, sender, receiver) = captor.value

        assertThat(amount.btc, equalTo(10000L))
        assertThat(amount.usd, equalTo(5L))

        assertNull(receiver.handle)
        assertThat(receiver.identity, equalTo("13305550000"))
        assertThat(receiver.type, equalTo("phone"))

        assertNull(sender.handle)
        assertThat(sender.identity, equalTo("13305551111"))
        assertThat(sender.type, equalTo("phone"))
    }

    @Test
    fun `invites phone from twitter when phone is not verified`() {
        val runner = createRunner()
        whenever(runner.client.inviteUser(ArgumentMatchers.any(InviteUserPayload::class.java))).thenReturn(Response.success(mock(InvitedContact::class.java)))
        val captor = ArgumentCaptor.forClass(InviteUserPayload::class.java)
        val twitterIdentity = mock(DropbitMeIdentity::class.java)
        whenever(runner.dropbitAccountHelper.isPhoneVerified).thenReturn(false)
        whenever(runner.dropbitAccountHelper.twitterIdentity()).thenReturn(twitterIdentity)
        whenever(twitterIdentity.handle).thenReturn("MyHandle")
        whenever(twitterIdentity.identity).thenReturn("5432112345")
        whenever(twitterIdentity.type).thenReturn(IdentityType.TWITTER)
        val identity = Identity(IdentityType.PHONE, "+13305550000", "--hash--", null)
        runner.execute(identity)

        verify<SignedCoinKeeperApiClient>(runner.client).inviteUser(captor.capture())
        val (amount, sender, receiver) = captor.value

        assertThat(amount.btc, equalTo(10000L))
        assertThat(amount.usd, equalTo(5L))

        assertNull(receiver.handle)
        assertThat(receiver.identity, equalTo("13305550000"))
        assertThat(receiver.type, equalTo("phone"))

        assertThat(sender.handle, equalTo("MyHandle"))
        assertThat(sender.identity, equalTo("5432112345"))
        assertThat(sender.type, equalTo("twitter"))
    }
}