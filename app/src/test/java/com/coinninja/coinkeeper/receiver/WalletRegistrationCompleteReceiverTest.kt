package com.coinninja.coinkeeper.receiver

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.service.CNGlobalMessagingService
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import androidx.test.ext.truth.content.IntentSubject.assertThat as assertThatIntent

@RunWith(AndroidJUnit4::class)
class WalletRegistrationCompleteReceiverTest {

    private val application: TestCoinKeeperApplication = ApplicationProvider.getApplicationContext()

    private fun createReceiver(): WalletRegistrationCompleteReceiver {
        val receiver = WalletRegistrationCompleteReceiver()
        receiver.onReceive(application, Intent())
        return receiver
    }

    @Test
    fun starts_global_messaging_services() {
        val captor: ArgumentCaptor<Intent> = ArgumentCaptor.forClass(Intent::class.java)
        val receiver = createReceiver()

        verify(receiver.jobServiceScheduler).enqueueWork(
                eq(application),
                eq(CNGlobalMessagingService::class.java),
                eq(JobServiceScheduler.GLOBAL_MESSAGING_SERVICE_JOB_ID),
                captor.capture())

        val intent = captor.value

        assertThatIntent(intent).hasComponent(application.packageName, CNGlobalMessagingService::class.java.name)
    }
}

