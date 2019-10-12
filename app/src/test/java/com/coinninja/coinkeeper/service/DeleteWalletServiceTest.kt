package com.coinninja.coinkeeper.service

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.coinninja.cn.thunderdome.repository.ThunderDomeRepository
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
        service.cnWalletManager = mock()
        service.localBroadCastUtil = mock()
        service.analytics = mock()
        service.syncWalletManager = mock()
        service.apiClient = mock()
        service.pushNotificationDeviceManager = mock()
        return service
    }

    @Test
    fun disables_sync() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.syncWalletManager).cancelAllScheduledSync()
    }

    @Test
    fun flushes_analytics() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).flush()
    }

    @Test
    fun tracks_disabling_account() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_DROPBIT_ME_ENABLED, false)
    }

    @Test
    fun sets_property_for_user_to_no_longer_has_a_balance() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_BTC_BALANCE, false)
    }

    @Test
    fun sets_property_for_user_to_no_longer_have_backed_up_wallet() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET_BACKUP, false)
    }

    @Test
    fun sets_property_for_user_to_no_longer_be_verified() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_PHONE_VERIFIED, false)
    }

    @Test
    fun identifies_user_property_to_not_have_a_wallet() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).setUserProperty(Analytics.PROPERTY_HAS_WALLET, false)
    }

    @Test
    fun instructs_CN_to_reset_wallet() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.apiClient).resetWallet()
    }

    @Test
    fun resets_all_data() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.cnWalletManager).deleteWallet()
    }

    @Test
    fun notifies_delete_completed() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_ON_WALLET_DELETED)
    }

    @Test
    fun send_analytics() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.analytics).trackEvent(Analytics.EVENT_WALLET_DELETE)
    }

    @Test
    fun removes_endpoint_locally_when_endpoint_is_present() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager).removeEndpoint()

    }

    @Test
    fun does_not_remove_endpoint_locally_when_endpoint_is_not_present() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager, times(0)).removeEndpoint()

    }

    @Test
    fun removes_endpoint_remote_when_endpoint_is_present() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(true)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager).unRegister()
    }

    @Test
    fun does_not_removes_endpoint_remote_when_endpoint_is_not_present() {
        val service = setUp()

        whenever(service.pushNotificationEndpointManager.hasEndpoint()).thenReturn(false)

        service.onHandleIntent(null)

        verify(service.pushNotificationEndpointManager, times(0)).unRegister()
    }

    @Test
    fun removes_cn_device_id_locally() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.pushNotificationDeviceManager).removeCNDevice()
    }

    @Test
    fun deletes_lightning_wallet() {
        val service = setUp()

        service.onHandleIntent(null)

        verify(service.thunderDomeRepository).deleteAll()
    }

    @Module
    class DeleteWalletServiceTestModule {
        @Provides
        fun pushNotificationEndpointManager(): PushNotificationEndpointManager = mock()

        @Provides
        fun thunderDome(): ThunderDomeRepository = mock()

        @Provides
        fun pushNotificationDeviceManager(): PushNotificationDeviceManager = mock()
    }
}