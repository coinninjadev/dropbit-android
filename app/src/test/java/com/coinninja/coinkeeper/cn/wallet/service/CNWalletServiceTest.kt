package com.coinninja.coinkeeper.cn.wallet.service

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.wallet.runner.SaveRecoveryWordsRunner
import com.coinninja.coinkeeper.service.runner.FullSyncWalletRunner
import com.coinninja.coinkeeper.ui.transaction.SyncManagerViewNotifier
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertNotNull
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class CNWalletServiceTest {
    private val valid_words get() = arrayOf("mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse", "mickeymouse")

    fun createService(): CNWalletService {
        val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()
        application.handler = mock(Handler::class.java)
        application.syncManagerViewNotifier = mock(SyncManagerViewNotifier::class.java)
        val service = Robolectric.setupService(CNWalletService::class.java)
        service.fullSyncWalletRunner = mock(FullSyncWalletRunner::class.java)
        service.saveRecoveryWordsRunner = mock(SaveRecoveryWordsRunner::class.java)
        return service
    }

    @Test
    fun executes_sync__only_queues_once() {
        val argumentCaptor = ArgumentCaptor.forClass(Message::class.java)
        val service = createService()
        whenever(service.workHandler.hasMessages(25)).thenReturn(false).thenReturn(true)
        val orderedOperations = inOrder(service.fullSyncWalletRunner, service.syncManagerViewNotifier)

        service.performSync()
        service.performSync()

        verify(service.workHandler).sendMessage(argumentCaptor.capture())

        val message = argumentCaptor.value
        assertNotNull(message)
        assertThat(message.what, equalTo(25))

        message.callback.run()

        orderedOperations.verify(service.syncManagerViewNotifier).isSyncing = true
        orderedOperations.verify(service.fullSyncWalletRunner).run()
        orderedOperations.verify(service.syncManagerViewNotifier).isSyncing = false
    }

    @Test
    fun saves_provided_words__only_queues_one_message() {
        val argumentCaptor = ArgumentCaptor.forClass(Message::class.java)
        val service = createService()
        val saveRecoveryWordsRunner = service.saveRecoveryWordsRunner
        val orderedOperations = inOrder(saveRecoveryWordsRunner)
        whenever(service.workHandler.hasMessages(35)).thenReturn(false).thenReturn(true)

        service.saveSeedWords(valid_words)
        service.saveSeedWords(valid_words)

        verify(service.workHandler).sendMessage(argumentCaptor.capture())

        val message = argumentCaptor.value
        assertNotNull(message)
        assertThat(message.what, equalTo(35))

        message.callback.run()

        orderedOperations.verify(saveRecoveryWordsRunner).setWords(valid_words)
        orderedOperations.verify(saveRecoveryWordsRunner).run()
        orderedOperations.verify(saveRecoveryWordsRunner).setWords(emptyArray())
    }

    @Test
    fun on_bind_return_local_binding_object_test() {
        val service = createService()
        val binder = service.onBind(Intent()) as CNWalletBinder?

        assertThat(binder, equalTo(service.cnWalletBinder))
    }

    @Test
    fun on_destroy_stop_background_threads_test() {
        val looper = mock(Looper::class.java)
        val service = createService()
        whenever(service.workHandler.looper).thenReturn(looper)

        service.onDestroy()

        verify(looper).quitSafely()
    }

}