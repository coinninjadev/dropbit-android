package com.coinninja.coinkeeper.service

import android.app.IntentService
import android.content.Intent
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO
import com.coinninja.coinkeeper.service.runner.SaveTransactionRunner
import com.coinninja.coinkeeper.util.DropbitIntents
import dagger.android.AndroidInjection
import javax.inject.Inject

class BroadcastTransactionService @JvmOverloads constructor(name: String = TAG) : IntentService(name) {

    @Inject
    internal lateinit var runner: SaveTransactionRunner

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    public override fun onHandleIntent(intent: Intent?) {
        intent?.let { intent ->
            if (intent.hasExtra(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO)) {
                val completedBroadcastActivityDTO = intent.getParcelableExtra<CompletedBroadcastDTO>(DropbitIntents.EXTRA_COMPLETED_BROADCAST_DTO)
                runner.setCompletedBroadcastActivityDTO(completedBroadcastActivityDTO)
                runner.run()
            }
        }
    }

    companion object {
        val TAG = BroadcastTransactionService::class.java.name
    }
}
