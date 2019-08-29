package com.coinninja.coinkeeper.cn.dropbit

import app.dropbit.annotations.Mockable
import app.dropbit.twitter.Twitter
import com.coinninja.coinkeeper.cn.account.RemoteAddressCache
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity
import com.coinninja.coinkeeper.model.db.enums.AccountStatus
import com.coinninja.coinkeeper.model.db.enums.IdentityType
import com.coinninja.coinkeeper.model.helpers.DropbitAccountHelper
import com.coinninja.coinkeeper.service.client.CNUserAccount
import com.coinninja.coinkeeper.service.client.CNUserIdentity
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient
import com.coinninja.coinkeeper.service.client.model.CNPhoneNumber
import com.coinninja.coinkeeper.service.client.model.CNUserPatch
import com.coinninja.coinkeeper.util.CNLogger
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import retrofit2.Response
import javax.inject.Inject

@Mockable
class DropBitMeServiceManager @Inject
constructor(internal val dropbitAccountHelper: DropbitAccountHelper,
            internal val apiClient: SignedCoinKeeperApiClient,
            internal val twitter: Twitter,
            internal val localBroadCastUtil: LocalBroadCastUtil,
            internal val cnWalletManager: CNWalletManager,
            internal val remoteAddressCache: RemoteAddressCache,
            internal val analytics: Analytics,
            internal val logger: CNLogger) {

    companion object {
        val TAG = DropBitMeServiceManager::class.java.name
    }

    fun enableAccount() {
        val response = apiClient.enableDropBitMeAccount()

        if (response.isSuccessful) {
            updateUserAccount(response)
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_ENABLED)
            analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true)
            analytics.trackEvent(Analytics.EVENT_DROPBIT_ME_ENABLED)
        } else {
            logger.logError(TAG, "-- Failed to enable account", response)
        }
    }

    fun disableAccount() {
        val response = apiClient.disableDropBitMeAccount()

        if (response.isSuccessful) {
            updateUserAccount(response)
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DROPBIT_ME_ACCOUNT_DISABLED)
            analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
            analytics.trackEvent(Analytics.EVENT_DROPBIT_ME_DISABLED)
        } else {
            logger.logError(TAG, "-- Failed to disable account", response)
        }
    }

    fun deVerifyPhoneNumber() {
        val identity = dropbitAccountHelper.identityForType(IdentityType.PHONE)
        val response = deleteIdentity(identity)

        if (response.isSuccessful) {
            dropbitAccountHelper.delete(identity)
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED)
        } else {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER_FAILED)
            logger.logError(TAG, "|------------ Failed to deVerify phone number", response)
        }

    }

    fun deVerifyTwitterAccount() {
        val identity = dropbitAccountHelper.identityForType(IdentityType.TWITTER)
        val response = deleteIdentity(identity)

        if (response.isSuccessful) {
            dropbitAccountHelper.delete(identity)
            twitter.clear()
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DEVERIFY_TWITTER_COMPLETED)
        } else {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_DEVERIFY_TWITTER_FAILED)
            logger.logError(TAG, "|------------ Failed to deVerify twitter", response)
        }

    }

    fun syncIdentities() {
        val response = apiClient.identities
        val ids = mutableListOf<String>()
        if (response.isSuccessful) {
            val identities: List<CNUserIdentity> = response.body() as List<CNUserIdentity>
            identities.forEach { identity ->
                identity.id?.let { id ->
                    ids.add(id)
                    dropbitAccountHelper.updateOrCreateFrom(identity)
                }
            }
            dropbitAccountHelper.clearIdentitiesNotIn(ids)
        }
    }

    fun verifyPhoneNumber(phone: PhoneNumber) {
        dropbitAccountHelper.phoneIdentity()?.delete()
        var identity = CNUserIdentity(type = "phone", identity = "${phone.countryCode}${phone.nationalNumber}")
        val response = createUserOrAddIdentity(identity)
        if (response.isSuccessful) {
            val body = response.body()
            if (body is CNUserIdentity) {
                identity = body
            }
            identity.identity = phone.toString()
            identity.status = AccountStatus.asString(AccountStatus.PENDING_VERIFICATION)

            if (response.code() == 200) {
                resendPhoneConfirmation(phone)
            } else {
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
            }

            dropbitAccountHelper.newFrom(identity)
        } else if (response.code().equals(424)) {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR)
        } else {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
        }
    }

    fun verifyTwitter(snowflake: Long) {
        val identity = CNUserIdentity(identity = snowflake.toString(), type = "twitter")
        var response = createUserOrAddIdentity(identity)
        if (response.isSuccessful) {
            identity.code = "${twitter.authToken}:${twitter.authSecret}"
            response = apiClient.verifyIdentity(identity)
            if (response.isSuccessful) {
                dropbitAccountHelper.updateOrCreateFrom(response.body() as CNUserAccount)
                localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_VERIFY_TWITTER_COMPLETED)
            }
        }
    }

    fun verifyPhoneConfirmationCode(code: String) {
        val identity = dropbitAccountHelper.phoneIdentity()
        identity?.let {
            val cnIdentity = CNUserIdentity(identity = CNPhoneNumber(identity.identity).toString(),
                    type = identity.type?.asString(), code = code)
            val response = apiClient.verifyIdentity(cnIdentity)
            when (response.code()) {
                200 -> {
                    dropbitAccountHelper.updateOrCreateFrom(response.body() as CNUserAccount)
                    remoteAddressCache.cacheAddresses()
                    analytics.setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, true)
                    analytics.setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, true)
                    analytics.flush()
                    localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS)
                }
                400 -> localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE)
                409 -> localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE)
                else -> localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
            }
        }
    }

    fun resendPhoneConfirmation(phoneNumber: PhoneNumber) {
        val response = apiClient.resendVerification(phoneNumber.toCNPhoneNumber())

        if (response.isSuccessful) {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT)
        } else if (response.code() == 429) {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR)
        } else if (response.code() == 424) {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR)
        } else {
            localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR)
            logger.logError(this::class.java.name, "|-- failed to resend confirmation code", response)
        }

    }

    private fun deleteIdentity(identity: DropbitMeIdentity?): Response<out Any> {
        return if (dropbitAccountHelper.numVerifiedIdentities > 1) {
            apiClient.deleteIdentity(identity)
        } else {
            resetWallet()
        }
    }

    private fun updateUserAccount(response: Response<*>) {
        val userPatch = response.body() as CNUserPatch?
        dropbitAccountHelper.updateUserAccount(userPatch)
    }

    private fun resetWallet(): Response<Void> {
        val response = apiClient.resetWallet()

        if (response.isSuccessful)
            cnWalletManager.deVerifyAccount()

        return response
    }

    private fun createUserOrAddIdentity(identity: CNUserIdentity): Response<*> {
        return if (dropbitAccountHelper.hasVerifiedAccount) {
            apiClient.addIdentity(identity)
        } else {
            val response = apiClient.createUserFromIdentity(identity)
            if (response.isSuccessful) {
                val account = response.body() as CNUserAccount
                val userAccount = dropbitAccountHelper.updateOrCreateFrom(account)
                if (IdentityType.from(identity.type) == IdentityType.PHONE && response.code() == 200) {
                    userAccount.status = AccountStatus.PENDING_VERIFICATION
                    userAccount.update()
                }
            }
            response
        }
    }
}
