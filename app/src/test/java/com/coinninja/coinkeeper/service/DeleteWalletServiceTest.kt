package com.coinninja.coinkeeper.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.coinkeeper.cn.service.PushNotificationDeviceManager
import com.coinninja.coinkeeper.cn.service.PushNotificationEndpointManager
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dagger.Module
import dagger.Provides
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class DeleteWalletServiceTest {

    fun setUp(): DeleteWalletService {
        val service = Robolectric.setupService(DeleteWalletService::class.java)
        service.daoSessionManager = mock()
        service.localBroadCastUtil = mock()
        service.analytics = mock()
        service.syncWalletManager = mock()
        service.apiClient = mock()
        service.pushNotificationDeviceManager = mock()
        service.preferenceUtil = mock()
        return service
    }

    @Test
    fun `disables sync`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.syncWalletManager).cancelAllScheduledSync()
    }

    @Test
    fun `flushes analytics`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).flush()
    }

    @Test
    fun `tracks disabling account`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
    }

    @Test
    fun `sets property for user to no longer has a balance`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
    }

    @Test
    fun `sets property for user to no longer have backed up wallet`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, false)
    }

    @Test
    fun `sets property for user to no longer be verified`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false)
    }

    @Test
    fun `identifies user property to not have a wallet`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET, false)
    }

    @Test
    fun `instructs CN to reset wallet`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.apiClient).resetWallet()
    }

    @Test
    fun `resets all data`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.daoSessionManager).resetAll()
    }

    @Test
    fun `notifies delete completed`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_ON_WALLET_DELETED)
    }

    @Test
    fun `send analytics`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).trackEvent(Analytics.EVENT_WALLET_DELETE)
    }

    @Test
    fun `removes endpoint locally when endpoint is present`() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager).removeEndpoint()

    }

    @Test
    fun `does not remove endpoint locally when endpoint is not present`() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager, times(0)).removeEndpoint()

    }

    @Test
    fun `removes endpoint remote when endpoint is present`() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager).unRegister()
    }

    @Test
    fun `does not removes endpoint remote when endpoint is not present`() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager, times(0)).unRegister()
    }

    @Test
    fun `removes cn device id locally`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.pushNotificationDeviceManager).removeCNDevice()
    }

    @Test
    fun `removes all saved preferences`() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.preferenceUtil).removeAll()
    }

    @Module
    class DeleteWalletServiceTestModule {
        @Provides
        fun pushNotificationEndpointManager(): PushNotificationEndpointManager = mock()

        @Provides
        fun pushNotificationDeviceManager(): PushNotificationDeviceManager = mock()
    }
}