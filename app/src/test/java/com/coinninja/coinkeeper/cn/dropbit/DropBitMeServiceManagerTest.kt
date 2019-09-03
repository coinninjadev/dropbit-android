package com.coinninja.coinkeeper.cn.dropbit

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.dropbit.twitter.Twitter
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.Account
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.enums.AccountStatus
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.service.client.CNUserIdentity
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNUserPatch
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
@RunWith(AndroidJUnit4::class)
class DropBitMeServiceManagerTest {

    private fun buildServiceManager(twitter: Twitter = mock(Twitter::class.java)): DropBitMeServiceManager {
        return DropBitMeServiceManager(
                mock(DropbitAccountHelper::class.java),
                mock(SignedCoinKeeperApiClient::class.java),
                twitter,
                mock(LocalBroadCastUtil::class.java),
                mock(CNWalletManager::class.java),
                mock(RemoteAddressCache::class.java),
                mock(Analytics::class.java),
                mock(CNLogger::class.java)
        )
    }

    @Test
    fun disables_account() {
        val cnUserPatch = CNUserPatch(false)
        val response = Response.success(cnUserPatch)
        val serviceManager = buildServiceManager()
        whenever(serviceManager.apiClient.disableDropBitMeAccount()).thenReturn(response)

        serviceManager.disableAccount()

        verify(serviceManager.dropbitAccountHelper).updateUserAccount(cnUserPatch)
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED)
        verify(serviceManager.analytics).trackEvent(Analytics.EVENT_DROPBIT_ME_DISABLED)
        verify(serviceManager.analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
    }

    @Test
    fun disables_account__logs_non_success() {
        val response = Response.error<CNUserPatch>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        val serviceManager = buildServiceManager()
        whenever(serviceManager.apiClient.disableDropBitMeAccount()).thenReturn(response)

        serviceManager.disableAccount()

        verifyZeroInteractions(serviceManager.localBroadCastUtil)
        verify(serviceManager.logger).logError(DropBitMeServiceManager.TAG, "-- Failed to disable account", response)
        verifyZeroInteractions(serviceManager.analytics)
    }

    @Test
    fun enables_account() {
        val cnUserPatch = CNUserPatch(true)
        val response = Response.success(cnUserPatch)
        val serviceManager = buildServiceManager()
        whenever(serviceManager.apiClient.enableDropBitMeAccount()).thenReturn(response)

        serviceManager.enableAccount()

        verify(serviceManager.dropbitAccountHelper).updateUserAccount(cnUserPatch)
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED)
        verify(serviceManager.analytics).trackEvent(Analytics.EVENT_DROPBIT_ME_ENABLED)
        verify(serviceManager.analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true)
    }

    @Test
    fun enables_account__logs_non_success() {
        val response = Response.error<CNUserPatch>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        val serviceManager = buildServiceManager()
        whenever(serviceManager.apiClient.enableDropBitMeAccount()).thenReturn(response)

        serviceManager.enableAccount()

        verifyZeroInteractions(serviceManager.localBroadCastUtil)
        verify(serviceManager.logger).logError(DropBitMeServiceManager::class.java.name, "-- Failed to enable account", response)
        verifyZeroInteractions(serviceManager.analytics)
    }

    @Test
    fun `deVerifying Phone Number removes identity locally and remotely`() {
        val serviceManager = buildServiceManager()
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(2)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(serviceManager.dropbitAccountHelper.identityForType(IdentityType.PHONE)).thenReturn(identity)
        val response = Response.success(ResponseBody.create(MediaType.parse("plain/text"), "")) as Response<Void>
        whenever(serviceManager.apiClient.deleteIdentity(identity)).thenReturn(response)

        serviceManager.deVerifyPhoneNumber()

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED)
        verify(serviceManager.dropbitAccountHelper).delete(identity)
    }

    @Test
    fun `notifies when deVerifying phone number fails`() {
        val serviceManager = buildServiceManager()
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(2)
        val response = Response.error<Void>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.deleteIdentity(ArgumentMatchers.any())).thenReturn(response)

        serviceManager.deVerifyPhoneNumber()

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED)
        verify(serviceManager.dropbitAccountHelper, times(0)).delete(ArgumentMatchers.any())
    }

    @Test
    fun `deVerifying Twitter removes identity locally and remotely`() {
        val twitter = mock(Twitter::class.java)
        val serviceManager = buildServiceManager(twitter)
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(2)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(serviceManager.dropbitAccountHelper.identityForType(IdentityType.TWITTER)).thenReturn(identity)
        val response = Response.success(ResponseBody.create(MediaType.parse("plain/text"), "")) as Response<Void>
        whenever(serviceManager.apiClient.deleteIdentity(identity)).thenReturn(response)

        serviceManager.deVerifyTwitterAccount()

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED)
        verify(twitter).clear()
        verify(serviceManager.dropbitAccountHelper).delete(identity)
    }

    @Test
    fun `notifies when deVerifying Twitter fails`() {
        val serviceManager = buildServiceManager()
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(2)
        val response = Response.error<Void>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.deleteIdentity(ArgumentMatchers.any())).thenReturn(response)

        serviceManager.deVerifyTwitterAccount()

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_DEVERIFY_TWITTER_FAILED)
        verify(serviceManager.dropbitAccountHelper, times(0)).delete(ArgumentMatchers.any())
    }

    @Test
    fun `deVerifying last identity (phone) removes user locally & remotely`() {
        val serviceManager = buildServiceManager()
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(1)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(serviceManager.dropbitAccountHelper.identityForType(IdentityType.PHONE)).thenReturn(identity)
        val response = Response.success(204, ResponseBody.create(MediaType.parse("plain/text"), "")) as Response<Void>
        whenever(serviceManager.apiClient.resetWallet()).thenReturn(response)

        serviceManager.deVerifyPhoneNumber()

        verify(serviceManager.cnWalletManager).deVerifyAccount()
    }

    @Test
    fun `deVerifying last identity (twitter) removes user locally & remotely`() {
        val serviceManager = buildServiceManager()
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(1)
        val identity = mock(DropbitMeIdentity::class.java)
        whenever(serviceManager.dropbitAccountHelper.identityForType(IdentityType.TWITTER)).thenReturn(identity)
        val response = Response.success(ResponseBody.create(MediaType.parse("plain/text"), "")) as Response<Void>
        whenever(serviceManager.apiClient.resetWallet()).thenReturn(response)

        serviceManager.deVerifyTwitterAccount()

        verify(serviceManager.cnWalletManager).deVerifyAccount()
    }

    @Test
    fun `syncing identities refreshes local with server`() {
        val serviceManager = buildServiceManager()
        val identity1 = CNUserIdentity(id = "id 1")
        val identity2 = CNUserIdentity(id = "id 2")
        val identities = listOf(identity1, identity2)
        val response: Response<List<CNUserIdentity>> = Response.success(identities)
        whenever(serviceManager.apiClient.identities).thenReturn(response)

        serviceManager.syncIdentities()

        verify(serviceManager.dropbitAccountHelper).clearIdentitiesNotIn(listOf("id 1", "id 2"))
        verify(serviceManager.dropbitAccountHelper).updateOrCreateFrom(identity1)
        verify(serviceManager.dropbitAccountHelper).updateOrCreateFrom(identity2)
    }

    @Test
    fun `only updates identities with success`() {
        val serviceManager = buildServiceManager()
        val response = Response.error<List<CNUserIdentity>>(500, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.identities).thenReturn(response)

        serviceManager.syncIdentities()

        verifyZeroInteractions(serviceManager.dropbitAccountHelper)
    }

    @Test
    fun `add and verify Twitter identity when user already verified`() {
        val argumentCaptor = argumentCaptor<CNUserIdentity>()
        val snowflake = 1234567890L
        val serviceManager = buildServiceManager()
        val pendingUserIdentity = CNUserIdentity()
        whenever(serviceManager.twitter.authToken).thenReturn("authToken")
        whenever(serviceManager.twitter.authSecret).thenReturn("authSecret")
        val pendingVerificationResponse = Response.success(pendingUserIdentity)
        val identity = CNUserIdentity(identity = snowflake.toString(), type = "twitter")
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)
        whenever(serviceManager.apiClient.addIdentity(identity)).thenReturn(pendingVerificationResponse)

        val verifiedAccount = CNUserAccount()
        val verifiedIdentityResponse = Response.success(verifiedAccount)
        val verified = CNUserIdentity(identity = snowflake.toString(), type = "twitter")
        verified.code = "${serviceManager.twitter.authToken}:${serviceManager.twitter.authSecret}"
        whenever(serviceManager.apiClient.verifyIdentity(verified)).thenReturn(verifiedIdentityResponse)

        serviceManager.verifyTwitter(snowflake)

        verify(serviceManager.apiClient).verifyIdentity(argumentCaptor.capture())
        val identityToVerify = argumentCaptor.firstValue
        assertThat(identityToVerify.code, equalTo("authToken:authSecret"))
        verify(serviceManager.dropbitAccountHelper).updateOrCreateFrom(verifiedAccount)
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED)
    }

    @Test
    fun `create user and verify with Twitter when user is not already verified`() {
        val argumentCaptor = argumentCaptor<CNUserIdentity>()
        val snowflake = 1234567890L
        val serviceManager = buildServiceManager()
        whenever(serviceManager.twitter.authToken).thenReturn("authToken")
        whenever(serviceManager.twitter.authSecret).thenReturn("authSecret")
        val userAccount = CNUserAccount()
        val userAccountResponse = Response.success(userAccount)

        // Create user from identity rather than add identity ---
        whenever(serviceManager.dropbitAccountHelper.numVerifiedIdentities).thenReturn(0)
        val identity = CNUserIdentity(identity = snowflake.toString(), type = "twitter")
        whenever(serviceManager.apiClient.createUserFromIdentity(identity)).thenReturn(userAccountResponse)

        val verifiedAccount = CNUserAccount()
        val verifiedIdentityResponse = Response.success(verifiedAccount)
        val verified = CNUserIdentity(identity = snowflake.toString(), type = "twitter")
        verified.code = "${serviceManager.twitter.authToken}:${serviceManager.twitter.authSecret}"
        whenever(serviceManager.apiClient.verifyIdentity(verified)).thenReturn(verifiedIdentityResponse)

        serviceManager.verifyTwitter(snowflake)

        verify(serviceManager.apiClient).verifyIdentity(argumentCaptor.capture())
        val identityToVerify = argumentCaptor.firstValue
        assertThat(identityToVerify.code, equalTo("authToken:authSecret"))
        verify(serviceManager.dropbitAccountHelper, times(2)).updateOrCreateFrom(verifiedAccount)
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED)
    }

    @Test
    fun `verifies users phone number (Creating new User with identity) `() {
        val serviceManager = buildServiceManager()
        val identity = CNUserIdentity(identity = "13305551111", type = "phone")
        val accountResponse = CNUserAccount()
        val savedIdentity = CNUserIdentity(identity = "+13305551111", type = "phone",
                status = AccountStatus.asString(AccountStatus.PENDING_VERIFICATION))
        val response = Response.success(201, accountResponse)
        whenever(serviceManager.apiClient.createUserFromIdentity(identity)).thenReturn(response)
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)

        serviceManager.verifyPhoneNumber(PhoneNumber("+13305551111"))

        verify(serviceManager.dropbitAccountHelper).newFrom(savedIdentity)
        verify(serviceManager.dropbitAccountHelper).updateOrCreateFrom(accountResponse)
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
    }

    @Test
    fun `verifies a number from another verified account and request new verification code`() {
        val phone = PhoneNumber("+13305551111")
        val serviceManager = buildServiceManager()
        val identity = CNUserIdentity(identity = "13305551111", type = "phone")
        val accountResponse = CNUserAccount()
        val savedIdentity = CNUserIdentity(identity = "+13305551111", type = "phone",
                status = AccountStatus.asString(AccountStatus.PENDING_VERIFICATION))
        val response = Response.success(200, accountResponse)
        whenever(serviceManager.apiClient.createUserFromIdentity(identity)).thenReturn(response)
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)
        val userAccount: Account = mock()
        whenever(serviceManager.dropbitAccountHelper.updateOrCreateFrom(accountResponse)).thenReturn(userAccount)
        whenever(serviceManager.apiClient.resendVerification(phone.toCNPhoneNumber())).thenReturn(Response.success("") as Response<CNUserAccount>)

        serviceManager.verifyPhoneNumber(phone)

        verify(serviceManager.dropbitAccountHelper).newFrom(savedIdentity)
        verify(serviceManager.dropbitAccountHelper).updateOrCreateFrom(accountResponse)
        verify(userAccount).status = AccountStatus.PENDING_VERIFICATION
        verify(userAccount).update()
        verify(serviceManager.apiClient).resendVerification(phone.toCNPhoneNumber())
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
    }

    @Test
    fun `verifies users phone number (adding identity to existing User Account) `() {
        val serviceManager = buildServiceManager()
        val identity = CNUserIdentity(identity = "13305551111", type = "phone")
        val identityResponse = CNUserIdentity(identity = "13305551111", type = "phone", hash = "--hash--", id = "--server-id--")
        val savedIdentity = identityResponse
        savedIdentity.identity = "+13305551111"

        val response = Response.success(201, identityResponse)
        whenever(serviceManager.apiClient.addIdentity(identity)).thenReturn(response)
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        serviceManager.verifyPhoneNumber(PhoneNumber("+13305551111"))

        verify(serviceManager.dropbitAccountHelper).newFrom(identityResponse)
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
    }

    @Test
    fun `deletes pending verification if one exists`() {
        val serviceManager = buildServiceManager()
        val identity = CNUserIdentity(identity = "13305551111", type = "phone")
        val identityResponse = CNUserIdentity(identity = "13305551111", type = "phone", hash = "--hash--", id = "--server-id--")
        val accountResponse = CNUserAccount()
        accountResponse.identities = listOf(identityResponse)

        val response = Response.success(201, accountResponse)
        whenever(serviceManager.apiClient.createUserFromIdentity(identity)).thenReturn(response)
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(false)
        val phoneIdentity: DropbitMeIdentity = mock()
        whenever(serviceManager.dropbitAccountHelper.phoneIdentity()).thenReturn(phoneIdentity)

        serviceManager.verifyPhoneNumber(PhoneNumber("+13305551111"))

        verify(phoneIdentity).delete()
    }

    @Test
    fun `notifies about country blacklist when verifying phone number`() {
        val serviceManager = buildServiceManager()
        val identity = CNUserIdentity(identity = "13305551111", type = "phone")
        val response = Response.error<CNUserIdentity>(424, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.addIdentity(identity)).thenReturn(response)
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        serviceManager.verifyPhoneNumber(PhoneNumber("+13305551111"))

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR)
    }

    @Test
    fun `notifies about general error when verifying phone number`() {
        val serviceManager = buildServiceManager()
        val identity = CNUserIdentity(identity = "13305551111", type = "phone")
        val response = Response.error<CNUserIdentity>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.addIdentity(identity)).thenReturn(response)
        whenever(serviceManager.dropbitAccountHelper.hasVerifiedAccount).thenReturn(true)

        serviceManager.verifyPhoneNumber(PhoneNumber("+13305551111"))

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
    }

    @Test
    fun `resending phone confirmation sends completed broadcast`() {
        val serviceManager = buildServiceManager()
        val phoneNumber = PhoneNumber("+13305551111").toCNPhoneNumber()
        whenever(serviceManager.apiClient.resendVerification(phoneNumber)).thenReturn(Response.success("") as Response<CNUserAccount>)

        serviceManager.resendPhoneConfirmation(PhoneNumber("+13305551111"))

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
    }

    @Test
    fun `resending phone confirmation sends rate limit  broadcast`() {
        val serviceManager = buildServiceManager()
        val phoneNumber = PhoneNumber("+13305551111").toCNPhoneNumber()
        val error = Response.error<CNUserAccount>(429, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.resendVerification(phoneNumber)).thenReturn(error)

        serviceManager.resendPhoneConfirmation(PhoneNumber("+13305551111"))

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR)
    }

    @Test
    fun `resending phone confirmation sends country blacklisted  broadcast`() {
        val serviceManager = buildServiceManager()
        val phoneNumber = PhoneNumber("+13305551111").toCNPhoneNumber()
        val error = Response.error<CNUserAccount>(424, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.resendVerification(phoneNumber)).thenReturn(error)

        serviceManager.resendPhoneConfirmation(PhoneNumber("+13305551111"))

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR)
    }

    @Test
    fun `resending phone confirmation sends http error  broadcast`() {
        val serviceManager = buildServiceManager()
        val phoneNumber = PhoneNumber("+13305551111").toCNPhoneNumber()
        val error = Response.error<CNUserAccount>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.apiClient.resendVerification(phoneNumber)).thenReturn(error)

        serviceManager.resendPhoneConfirmation(PhoneNumber("+13305551111"))

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
    }

    @Test
    fun `verifying confirmation code communicates success`() {
        val serviceManager = buildServiceManager()
        val code = "123456"
        val account = CNUserAccount()
        val identity = DropbitMeIdentity()
        identity.identity = "+13305551111"
        identity.type = IdentityType.PHONE
        val cnIdentity = CNUserIdentity(type = "phone", code = code, identity = "13305551111")
        whenever(serviceManager.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        whenever(serviceManager.apiClient.verifyIdentity(cnIdentity)).thenReturn(Response.success(account))

        serviceManager.verifyPhoneConfirmationCode(code)

        verify(serviceManager.dropbitAccountHelper).updateOrCreateFrom(account)
        verify(serviceManager.remoteAddressCache).cacheAddresses()
        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS)
        verify(serviceManager.analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, true)
        verify(serviceManager.analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true)
        verify(serviceManager.analytics).flush()
    }

    @Test
    fun `verifying confirmation code communicates http error`() {
        val serviceManager = buildServiceManager()
        val code = "123456"
        val identity = DropbitMeIdentity()
        identity.identity = "+13305551111"
        identity.type = IdentityType.PHONE
        val cnIdentity = CNUserIdentity(type = "phone", code = code, identity = "13305551111")
        whenever(serviceManager.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        val error = Response.error<CNUserAccount>(404, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        whenever(serviceManager.apiClient.verifyIdentity(cnIdentity)).thenReturn(error)

        serviceManager.verifyPhoneConfirmationCode(code)

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
    }

    @Test
    fun `verifying confirmation code communicates code is expired error`() {
        val serviceManager = buildServiceManager()
        val code = "123456"
        val identity = DropbitMeIdentity()
        identity.identity = "+13305551111"
        identity.type = IdentityType.PHONE
        val cnIdentity = CNUserIdentity(type = "phone", code = code, identity = "13305551111")
        val error = Response.error<CNUserAccount>(409, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        whenever(serviceManager.apiClient.verifyIdentity(cnIdentity)).thenReturn(error)

        serviceManager.verifyPhoneConfirmationCode(code)

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE)
    }

    @Test
    fun `verifying confirmation code communicates code is invalid error`() {
        val serviceManager = buildServiceManager()
        val code = "123456"
        val identity = DropbitMeIdentity()
        identity.identity = "+13305551111"
        identity.type = IdentityType.PHONE
        val cnIdentity = CNUserIdentity(type = "phone", code = code, identity = "13305551111")
        val error = Response.error<CNUserAccount>(400, ResponseBody.create(MediaType.parse("plain/text"), ""))
        whenever(serviceManager.dropbitAccountHelper.phoneIdentity()).thenReturn(identity)
        whenever(serviceManager.apiClient.verifyIdentity(cnIdentity)).thenReturn(error)

        serviceManager.verifyPhoneConfirmationCode(code)

        verify(serviceManager.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE)
    }
}