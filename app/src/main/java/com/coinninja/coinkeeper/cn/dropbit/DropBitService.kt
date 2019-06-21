package com.coinninja.coinkeeper.cn.dropbit

import android.app.IntentService
import android.content.Intent
import com.coinninja.coinkeeper.model.PhoneNumber
import com.coinninja.coinkeeper.util.DropbitIntents.*
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import dagger.android.AndroidInjection
import javax.inject.Inject

class DropBitService : IntentService {

    @Inject
    internal lateinit var dropBitMeServiceManager: DropBitMeServiceManager
    @Inject
    internal lateinit var dropBitCancellationManager: DropBitCancellationManager
    @Inject
    internal lateinit var localBroadCastUtil: LocalBroadCastUtil
    @Inject
    internal lateinit var dropBitAddMemoManager: DropBitAddMemoManager

    constructor(name: String) : super(name) {}

    constructor() : super(DropBitService::class.java.name) {}

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleIntent(intent: Intent?) {
        intent?.let { intent ->
            when (intent.action) {
                ACTION_CANCEL_DROPBIT -> onCancelDropbit(intent)
                ACTION_DROPBIT_ME_ENABLE_ACCOUNT -> dropBitMeServiceManager.enableAccount()
                ACTION_DROPBIT_ME_DISABLE_ACCOUNT -> dropBitMeServiceManager.disableAccount()
                ACTION_DEVERIFY_PHONE_NUMBER -> dropBitMeServiceManager.deVerifyPhoneNumber()
                ACTION_DEVERIFY_TWITTER -> dropBitMeServiceManager.deVerifyTwitterAccount()
                ACTION_VERIFY_TWITTER -> verifyTwitter(intent)
                ACTION_VERIFY_PHONE_NUMBER -> verifyPhoneNumber(intent)
                ACTION_CREATE_NOTIFICATION -> createNotification(intent)
                ACTION_RESEND_PHONE_CONFIRMATION -> resendPhoneConfirmation(intent)
                ACTION_VERIFY_PHONE_NUMBER_CODE -> verifyPhoneConfirmationCode(intent)
                else -> return
            }
        }
    }

    private fun verifyPhoneConfirmationCode(intent: Intent) {
        if (intent.hasExtra(EXTRA_PHONE_NUMBER_CODE)) {
            val code = intent.getStringExtra(EXTRA_PHONE_NUMBER_CODE)
            dropBitMeServiceManager.verifyPhoneConfirmationCode(code)
        }
    }

    private fun resendPhoneConfirmation(intent: Intent) {
        if (intent.hasExtra(EXTRA_PHONE_NUMBER)) {
            val phoneNumber = intent.getParcelableExtra<PhoneNumber>(EXTRA_PHONE_NUMBER)
            dropBitMeServiceManager.resendPhoneConfirmation(phoneNumber)
        }
    }

    private fun verifyPhoneNumber(intent: Intent) {
        if (intent.hasExtra(EXTRA_PHONE_NUMBER)) {
            val phoneNumber = intent.getParcelableExtra<PhoneNumber>(EXTRA_PHONE_NUMBER)
            dropBitMeServiceManager.verifyPhoneNumber(phoneNumber)
        }
    }

    private fun verifyTwitter(intent: Intent) {
        if (intent.hasExtra(EXTRA_TWITTER_SNOWFLAKE)) {
            val snowflake = intent.getLongExtra(EXTRA_TWITTER_SNOWFLAKE, 0)
            dropBitMeServiceManager.verifyTwitter(snowflake)
        }
    }

    private fun createNotification(intent: Intent) {
        if (!intent.hasExtra(EXTRA_DROPBIT_MEMO) || !intent.hasExtra(EXTRA_DROPBIT_TXID))
            return

        dropBitAddMemoManager.createMemo(intent.getStringExtra(EXTRA_DROPBIT_TXID), intent.getStringExtra(EXTRA_DROPBIT_MEMO))
        localBroadCastUtil.sendBroadcast(ACTION_TRANSACTION_DATA_CHANGED)
    }

    private fun onCancelDropbit(intent: Intent) {
        if (!intent.hasExtra(EXTRA_INVITATION_ID)) return

        dropBitCancellationManager.markAsCanceled(intent.getStringExtra(EXTRA_INVITATION_ID))
        localBroadCastUtil.sendBroadcast(ACTION_TRANSACTION_DATA_CHANGED)
    }

}
