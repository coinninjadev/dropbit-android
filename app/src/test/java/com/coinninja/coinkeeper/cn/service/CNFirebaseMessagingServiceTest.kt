package com.coinninja.coinkeeper.cn.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class CNFirebaseMessagingServiceTest {

    @Test
    fun updates_user_token_with_manager_when_change() {
        val token = "-- new token --"
        val service = Robolectric.setupService(CNFirebaseMessagingService::class.java)

        service.onNewToken(token)

        verify(service.pushNotificationServiceManager).saveToken(token)
    }

    @Module
    class CNFirebaseMessagingServiceTestModule {
        @Provides
        fun pushNotificationServiceManager(): PushNotificationServiceManager = mock()

    }
}