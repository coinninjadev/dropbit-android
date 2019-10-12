package com.coinninja.coinkeeper.service

import android.content.Intent

import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner

import javax.inject.Inject
import androidx.core.app.JobIntentService
import dagger.android.AndroidInjection

class WalletTransactionRetrieverService : JobIntentService() {

    @Inject
    internal lateinit var fullSyncWalletRunner: FullSyncWalletRunner

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleWork(intent: Intent) {
        fullSyncWalletRunner.run()
    }
}
