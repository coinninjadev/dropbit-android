package com.coinninja.coinkeeper.service

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.cn.service.PushNotificationServiceManager
import com.coinninja.coinkeeper.cn.service.YearlyHighSubscription
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class PushNotificationEndpointRegistrationServiceTest {

    private fun createService(): PushNotificationEndpointRegistrationService {
        val service = PushNotificationEndpointRegistrationService()
        service.pushNotificationServiceManager = mock(PushNotificationServiceManager::class.java)
        service.yearlyHighSubscription = mock(YearlyHighSubscription::class.java)
        return service
    }

    @Test
    fun registers_push_notifications_endpoint() {
        val service = createService()
        val orderedOperations = inOrder(service.pushNotificationServiceManager, service.yearlyHighSubscription)
        whenever(service.pushNotificationServiceManager.isRegisteredEndpoint()).thenReturn(true)

        service.onHandleWork(Intent())

        orderedOperations.verify(service.pushNotificationServiceManager).verifyToken()
        orderedOperations.verify(service.pushNotificationServiceManager).subscribeToChannels()
        verifyZeroInteractions(service.yearlyHighSubscription)
    }

    @Test
    fun `subscribes user to yearly high when endpoint is being created`() {
        val service = createService()
        val orderedOperations = inOrder(service.pushNotificationServiceManager, service.yearlyHighSubscription)
        whenever(service.pushNotificationServiceManager.isRegisteredEndpoint()).thenReturn(false)

        service.onHandleWork(Intent())

        orderedOperations.verify(service.pushNotificationServiceManager).verifyToken()
        orderedOperations.verify(service.pushNotificationServiceManager).registerAsEndpoint()
        orderedOperations.verify(service.yearlyHighSubscription).subscribe()
        orderedOperations.verify(service.pushNotificationServiceManager).subscribeToChannels()
    }
}