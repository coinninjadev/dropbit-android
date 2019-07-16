package com.coinninja.coinkeeper.receiver

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.service.PushNotificationEndpointRegistrationService
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import com.nhaarman.mockitokotlin2.eq
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import androidx.test.ext.truth.content.IntentSubject.assertThat as assertThatIntent

@RunWith(AndroidJUnit4::class)
class WalletCreatedBroadCastReceiverTest {

    private val application: TestCoinKeeperApplication get() = ApplicationProvider.getApplicationContext()

    private fun createWalletReceiver(): WalletCreatedBroadCastReceiver {
        val receiver = WalletCreatedBroadCastReceiver()
        receiver.onReceive(application, Intent())
        return receiver
    }

    @Test
    fun reports_that_user_has_wallet() {
        verify(createWalletReceiver().analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET, true)
    }

    @Test
    fun executes_first_sync() {
        verify(createWalletReceiver().syncWalletManager).syncNow()
    }

    @Test
    fun schedules_30_second_sync() {
        verify(createWalletReceiver().syncWalletManager).schedule60SecondSync()
    }

    @Test
    fun schedules_hourly_sync() {
        verify(createWalletReceiver().syncWalletManager).scheduleHourlySync()
    }

    @Test
    fun executes_push_registration_service() {
        val argumentCaptor: ArgumentCaptor<Intent> = ArgumentCaptor.forClass(Intent::class.java)
        verify(createWalletReceiver().jobServiceScheduler).enqueueWork(
                eq(application),
                eq(PushNotificationEndpointRegistrationService::class.java),
                eq(JobServiceScheduler.ENDPOINT_REGISTRATION_SERVICE_JOB_ID),
                argumentCaptor.capture()
        )

        val intent = argumentCaptor.value
        assertThatIntent(intent)
                .hasComponent(application.packageName,
                        PushNotificationEndpointRegistrationService::class.java.name)
    }
}

