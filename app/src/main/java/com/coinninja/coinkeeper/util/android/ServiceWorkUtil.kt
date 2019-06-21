package com.coinninja.coinkeeper.util.android

import android.content.Context
import android.content.Intent
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.dropbit.DropBitService
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.util.DropbitIntents
import javax.inject.Inject

@Mockable
class ServiceWorkUtil @Inject
internal constructor(@ApplicationContext internal val context: Context) {

    fun lookupAddressForPhoneNumberHash(phoneNumberHash: String) {
        val intent = Intent(context, CNWalletAddressRequestService::class.java)
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_HASH, phoneNumberHash)
        context.startService(intent)
    }

    fun registerUsersPhone(phoneNumber: PhoneNumber) {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_VERIFY_PHONE_NUMBER
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber)
        context.startService(intent)
    }

    fun disableDropBitMe() {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DROPBIT_ME_DISABLE_ACCOUNT
        context.startService(intent)
    }

    fun enableDropBitMe() {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DROPBIT_ME_ENABLE_ACCOUNT
        context.startService(intent)
    }

    fun deVerifyPhoneNumber() {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER
        context.startService(intent)
    }

    fun deVerifyTwitter() {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DEVERIFY_TWITTER
        context.startService(intent)
    }

    fun addVerifiedTwitterAccount(snowflake: Long) {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_VERIFY_TWITTER
        intent.putExtra(DropbitIntents.EXTRA_TWITTER_SNOWFLAKE, snowflake)
        context.startService(intent)
    }

    fun resendPhoneVerification(phoneNumber: PhoneNumber) {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_RESEND_PHONE_CONFIRMATION
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber)
        context.startService(intent)
    }

    fun validatePhoneNumberConfirmationCode(code: String) {
        val intent = Intent(context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_VERIFY_PHONE_NUMBER_CODE
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_CODE, code)
        context.startService(intent)
    }
}
