package com.coinninja.coinkeeper.service

import android.app.IntentService
import android.content.Intent
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.util.DropbitIntents
import dagger.android.AndroidInjection
import javax.inject.Inject

class SaveInviteService @JvmOverloads constructor(name: String = TAG) : IntentService(name) {

    @Inject
    internal lateinit var transactionNotificationManager: TransactionNotificationManager

    @Inject
    internal lateinit var inviteTransactionSummaryHelper: InviteTransactionSummaryHelper

    @Inject
    internal lateinit var cnWalletManager: CNWalletManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.hasExtra(DropbitIntents.EXTRA_COMPLETED_INVITE_DTO)) {
            val completedInviteDTO: CompletedInviteDTO = intent.getParcelableExtra(DropbitIntents.EXTRA_COMPLETED_INVITE_DTO)
            val invite = inviteTransactionSummaryHelper.acknowledgeInviteTransactionSummary(completedInviteDTO)
            invite?.let {
                transactionNotificationManager.saveTransactionNotificationLocally(invite, completedInviteDTO)
                cnWalletManager.updateBalances()
            }
        }
    }

    companion object {
        val TAG = SaveInviteService::class.java.name
    }
}
