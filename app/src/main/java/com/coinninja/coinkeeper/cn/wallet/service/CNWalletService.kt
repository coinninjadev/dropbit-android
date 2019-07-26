package com.coinninja.coinkeeper.cn.wallet.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface
import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner
import com.coinninja.coinkeeper.di.interfaces.ThreadHandler
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import dagger.android.AndroidInjection
import javax.inject.Inject

@Mockable
class CNWalletService : Service(), CNWalletServicesInterface {

    internal lateinit var cnWalletBinder: CNWalletBinder

    @Inject
    @field:ThreadHandler
    internal lateinit var workHandler: Handler

    @Inject
    internal lateinit var syncManagerViewNotifier: SyncManagerViewNotifier

    @Inject
    internal lateinit var saveRecoveryWordsRunner: SaveRecoveryWordsRunner

    @Inject
    internal lateinit var fullSyncWalletRunner: FullSyncWalletRunner


    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
        cnWalletBinder = CNWalletBinder(this)
    }

    override fun saveSeedWords(seedWords: Array<String>) {
        if (workHandler.hasMessages(SAVE_WORDS_ID)) return

        val work: Runnable = object : Runnable {
            override fun run() {
                saveRecoveryWordsRunner.setWords(seedWords)
                saveRecoveryWordsRunner.run()
                saveRecoveryWordsRunner.setWords(emptyArray())
            }
        }
        val message = Message.obtain(workHandler, work)
        message.what = SAVE_WORDS_ID
        workHandler.sendMessage(message)
    }

    override fun performSync() {
        if (workHandler.hasMessages(SYNC_MESSAGE_ID)) return
        val work: Runnable = object : Runnable {
            override fun run() {
                syncManagerViewNotifier.isSyncing = true
                fullSyncWalletRunner.run()
                syncManagerViewNotifier.isSyncing = false
            }
        }

        val message = Message.obtain(workHandler, work)
        message.what = SYNC_MESSAGE_ID
        workHandler.sendMessage(message)
    }

    override fun onBind(intent: Intent): IBinder? {
        return cnWalletBinder
    }

    override fun onDestroy() {
        workHandler.looper.quitSafely()
    }

    companion object {
        internal val SYNC_MESSAGE_ID = 25
        internal val SAVE_WORDS_ID = 35
    }
}
