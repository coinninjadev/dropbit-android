package com.coinninja.coinkeeper.cn.dropbit

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class DropBitServiceTest {

    fun setUp(): DropBitService {
        MockitoAnnotations.initMocks(this)
        val service: DropBitService = Robolectric.setupService(DropBitService::class.java)
        service.dropBitCancellationManager = mock(DropBitCancellationManager::class.java)
        service.localBroadCastUtil = mock(LocalBroadCastUtil::class.java)
        service.dropBitMeServiceManager = mock(DropBitMeServiceManager::class.java)
        service.dropBitAddMemoManager = mock(DropBitAddMemoManager::class.java)
        return service
    }

    @Test
    fun cancels_DropBit_by_id() {
        val service = setUp()
        val intent = Intent(DropbitIntents.ACTION_CANCEL_DROPBIT)
        val inviteId = "--invite id"
        intent.action = DropbitIntents.ACTION_CANCEL_DROPBIT
        intent.putExtra(DropbitIntents.EXTRA_INVITATION_ID, inviteId)

        service.onHandleIntent(intent)

        verify(service.dropBitCancellationManager).markAsCanceled(inviteId)
        verify(service.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_TRANSACTION_DATA_CHANGED)
    }

    @Test
    fun null_noop_without_invitation_id() {
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_CANCEL_DROPBIT
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitCancellationManager, times(0))
                .markAsCanceled(anyString())
    }

    @Test
    fun disables_dropbit_me() {
        val intent = Intent(DropbitIntents.ACTION_DROPBIT_ME_DISABLE_ACCOUNT)
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).disableAccount()
    }

    @Test
    fun enables_dropbit_me() {
        val intent = Intent(DropbitIntents.ACTION_DROPBIT_ME_ENABLE_ACCOUNT)
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).enableAccount()
    }

    @Test
    fun null_noop_without_memo_string() {
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_CREATE_NOTIFICATION
        intent.putExtra(DropbitIntents.EXTRA_DROPBIT_TXID, "")
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitAddMemoManager, times(0)).createMemo(any(), ArgumentMatchers.anyString())
    }

    @Test
    fun null_noop_without_txid() {
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_CREATE_NOTIFICATION
        intent.putExtra(DropbitIntents.EXTRA_DROPBIT_MEMO, "")
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitAddMemoManager, times(0)).createMemo(any(), ArgumentMatchers.anyString())
    }

    @Test
    fun `deVerify twitter`() {
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_DEVERIFY_TWITTER
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).deVerifyTwitterAccount()
    }

    @Test
    fun `deVerify Phone Number`() {
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_DEVERIFY_PHONE_NUMBER
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).deVerifyPhoneNumber()
    }

    @Test
    fun `adds phone verification`() {
        val number = PhoneNumber("+13305551111")
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_VERIFY_PHONE_NUMBER
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, number)
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).verifyPhoneNumber(number)
    }

    @Test
    fun `resend phone confirmation`() {
        val number = PhoneNumber("+13305551111")
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_RESEND_PHONE_CONFIRMATION
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, number)
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).resendPhoneConfirmation(number)
    }

    @Test
    fun `verify confirmation code`() {
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_VERIFY_PHONE_NUMBER_CODE
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_CODE, "123456")
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).verifyPhoneConfirmationCode("123456")
    }

    @Test
    fun `adds twitter verification`() {
        val snowflake = 1234567890L
        val intent = Intent()
        intent.action = DropbitIntents.ACTION_VERIFY_TWITTER
        intent.putExtra(DropbitIntents.EXTRA_TWITTER_SNOWFLAKE, snowflake)
        val service = setUp()

        service.onHandleIntent(intent)

        verify(service.dropBitMeServiceManager).verifyTwitter(snowflake)
    }
}