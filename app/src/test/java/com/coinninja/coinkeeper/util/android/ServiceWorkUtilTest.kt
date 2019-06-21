package com.coinninja.coinkeeper.util.android

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.cn.dropbit.DropBitService
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletAddressRequestService
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.matchers.IntentMatcher.equalTo
import com.google.i18n.phonenumbers.Phonenumber
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class ServiceWorkUtilTest {

    @Test
    fun starts_cn_wallet_address_lookup_service() {
        val phoneNumberHash = "--hash--"
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, CNWalletAddressRequestService::class.java)
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_HASH, phoneNumberHash)

        serviceWorkUtil.lookupAddressForPhoneNumberHash(phoneNumberHash)

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun starts_service_to_register_users_phone() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val phoneNumber = Phonenumber.PhoneNumber()
        phoneNumber.nationalNumber = 3305555555L
        phoneNumber.countryCode = 1
        val number = PhoneNumber(phoneNumber)
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_VERIFY_PHONE_NUMBER
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, number)

        serviceWorkUtil.registerUsersPhone(number)

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun disables_dropbit_me() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DROPBIT_ME_DISABLE_ACCOUNT

        serviceWorkUtil.disableDropBitMe()

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun enables_dropbit_me() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DROPBIT_ME_ENABLE_ACCOUNT

        serviceWorkUtil.enableDropBitMe()

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun `deVerifies Twitter`() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DEVERIFY_TWITTER

        serviceWorkUtil.deVerifyTwitter()

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun `deVerifies Phone Number`() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER

        serviceWorkUtil.deVerifyPhoneNumber()

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))

    }

    @Test
    fun `adds verified twitter account`() {
        val snowflake = 1234567890L
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_VERIFY_TWITTER
        intent.putExtra(DropbitIntents.EXTRA_TWITTER_SNOWFLAKE, snowflake)

        serviceWorkUtil.addVerifiedTwitterAccount(snowflake)

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun `resend phone verification`() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val phoneNumber = PhoneNumber("+13305551111")
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_RESEND_PHONE_CONFIRMATION
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber)

        serviceWorkUtil.resendPhoneVerification(phoneNumber)

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))
    }

    @Test
    fun `verifies phone number confirmation code`() {
        val serviceWorkUtil = ServiceWorkUtil(ApplicationProvider.getApplicationContext())
        val intent = Intent(serviceWorkUtil.context, DropBitService::class.java)
        intent.action = DropbitIntents.ACTION_VERIFY_PHONE_NUMBER_CODE
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_CODE, "123456")

        serviceWorkUtil.validatePhoneNumberConfirmationCode("123456")

        val startedService = shadowOf(serviceWorkUtil.context as Application).peekNextStartedService()
        assertThat(startedService, equalTo(intent))

    }
}