package com.coinninja.coinkeeper.receiver

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.nhaarman.mockitokotlin2.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ApplicationStartedReceiverTest {

    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()

    private fun createReceiver(): ApplicationStartedReceiver {
        val receiver = ApplicationStartedReceiver()
        val application1 = application
        application1.jobServiceScheduler = mock()
        application1.syncWalletManager = mock()
        application1.cnWalletManager = mock()
        application1.analytics = mock()
        return receiver
    }

    @Test
    fun schedules_push_notification_job_intent_services() {
        val receiver = createReceiver()
        val app = application
        whenever(app.cnWalletManager.hasWallet).thenReturn(true)
        val captor = argumentCaptor<Intent>()

        receiver.onReceive(app, Intent())

        verify(app.jobServiceScheduler).enqueueWork(eq(app),
                eq(PushNotificationEndpointRegistrationService::class.java),
                eq(100), captor.capture())

        val intent = captor.firstValue
        assertThat(intent.component!!.javaClass.name, equalTo(PushNotificationEndpointRegistrationService::class.java.name))
    }

    @Test
    fun skip_when_missing_wallet() {
        val receiver = createReceiver()
        val app = application
        whenever(app.cnWalletManager.hasWallet).thenReturn(false)

        receiver.onReceive(app, Intent())

        verifyZeroInteractions(app.jobServiceScheduler)
        verifyZeroInteractions(app.syncWalletManager)
    }

    @Test
    fun sets_platform_to_android() {
        val receiver = createReceiver()
        val app = application
        whenever(app.cnWalletManager.hasWallet).thenReturn(false)
        verify(app.analytics).setUserProperty(Analytics.PROPERTY_PLATFORM, "Android")
    }
}
