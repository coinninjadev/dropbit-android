package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.TestCoinKeeperApplication
import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager
import com.coinninja.coinkeeper.util.android.app.JobIntentService.JobServiceScheduler
import com.nhaarman.mockitokotlin2.*
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.robolectric.Robolectric
import androidx.test.ext.truth.content.IntentSubject.assertThat as assertThatIntent

@RunWith(AndroidJUnit4::class)
class PushNotificationEndpointRegistrationServiceTest {

    private val application = ApplicationProvider.getApplicationContext<TestCoinKeeperApplication>()

    private fun createService(): PushNotificationEndpointRegistrationService {
        val service = Robolectric.setupService(PushNotificationEndpointRegistrationService::class.java)
        service.yearlyHighSubscription = mock()
        service.uuid = "--uuid--"
        service.jobServiceScheduler = mock()
        return service
    }

    @Test
    fun registers_observer_for_being_informed_when_token_loaded() {
        val service = createService()

        service.onHandleWork(Intent())

        verify(service.pushNotificationServiceManager).acquireToken(service.pushTokenVerifiedObserver)
    }

    @Test
    fun registers_push_notifications_endpoint() {
        val service = createService()
        val orderedOperations = inOrder(service.pushNotificationServiceManager, service.yearlyHighSubscription)
        whenever(service.pushNotificationServiceManager.hasPushToken()).thenReturn(true)
        whenever(service.pushNotificationServiceManager.isRegisteredEndpoint()).thenReturn(true)
        whenever(service.pushNotificationServiceManager.isRegisteredDevice()).thenReturn(true)

        service.onHandleWork(Intent())

        orderedOperations.verify(service.pushNotificationServiceManager).subscribeToChannels()
        verifyZeroInteractions(service.yearlyHighSubscription)
    }

    @Test
    fun removes_all_endpoints_when_device_is_registered_but_endpoints_are_not() {
        val service = createService()
        val orderedOperations = inOrder(service.pushNotificationServiceManager, service.yearlyHighSubscription)
        whenever(service.pushNotificationServiceManager.hasPushToken()).thenReturn(true)
        whenever(service.pushNotificationServiceManager.isRegisteredEndpoint()).thenReturn(false)
        whenever(service.pushNotificationServiceManager.isRegisteredDevice()).thenReturn(true)

        service.onHandleWork(Intent())

        orderedOperations.verify(service.pushNotificationServiceManager).removeAllDeviceEndpoints()
        orderedOperations.verify(service.pushNotificationServiceManager).registerAsEndpoint()
        orderedOperations.verify(service.yearlyHighSubscription).subscribe()
        orderedOperations.verify(service.pushNotificationServiceManager).subscribeToChannels()
    }

    @Test
    fun subscribes_user_to_yearly_high_when_endpoint_is_being_created() {
        val service = createService()
        val orderedOperations = inOrder(service.pushNotificationServiceManager, service.yearlyHighSubscription)
        whenever(service.pushNotificationServiceManager.hasPushToken()).thenReturn(true)
        whenever(service.pushNotificationServiceManager.isRegisteredEndpoint()).thenReturn(false)
        whenever(service.pushNotificationServiceManager.isRegisteredDevice()).thenReturn(false)

        service.onHandleWork(Intent())

        orderedOperations.verify(service.pushNotificationServiceManager).registerDevice(service.uuid)
        orderedOperations.verify(service.pushNotificationServiceManager).registerAsEndpoint()
        orderedOperations.verify(service.yearlyHighSubscription).subscribe()
        orderedOperations.verify(service.pushNotificationServiceManager).subscribeToChannels()
    }

    @Test
    fun schedules_rerun_when_token_acquired() {
        val argumentCaptor: ArgumentCaptor<Intent> = ArgumentCaptor.forClass(Intent::class.java)
        val service = createService()

        service.pushTokenVerifiedObserver.onTokenAcquired("--token--")

        verify(service.jobServiceScheduler).enqueueWork(
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

    @Module
    class PushNotificationEndpointRegistrationServiceTestModule {
        @Provides
        fun pushNotificationServiceManager(): PushNotificationServiceManager = mock()
    }
}