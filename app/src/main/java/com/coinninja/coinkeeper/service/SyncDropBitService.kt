package com.coinninja.coinkeeper.service

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.coinninja.coinkeeper.cn.account.AccountManager
import com.coinninja.coinkeeper.model.helpers.WalletHelper
import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner
import dagger.android.AndroidInjection
import javax.inject.Inject

class SyncDropBitService @JvmOverloads constructor(name: String = SyncDropBitService::class.java.name) : IntentService(name) {
    @Inject
    lateinit var accountManager: AccountManager
    @Inject
    lateinit var walletHelper: WalletHelper
    @Inject
    lateinit var syncIncomingInvitesRunner: SyncIncomingInvitesRunner
    @Inject
    lateinit var receivedInvitesStatusRunner: ReceivedInvitesStatusRunner
    @Inject
    lateinit var fulfillSentInvitesRunner: FulfillSentInvitesRunner

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleIntent(intent: Intent?) {
        accountManager.cacheAddresses(walletHelper.primaryWallet)
        Log.d("SYNC", "Incoming invites -- Invites Sent to me")
        syncIncomingInvitesRunner.run()
        Log.d("SYNC", "Fulfill invites -- Invites I sent")
        fulfillSentInvitesRunner.run()
        Log.d("SYNC", "Get Fulfilled invites -- Invites Sent to me")
        receivedInvitesStatusRunner.run()
    }
}