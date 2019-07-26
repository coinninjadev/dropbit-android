package com.coinninja.coinkeeper.cn.wallet.service

import android.content.ComponentName
import com.coinninja.coinkeeper.util.DropbitIntents
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class CNServiceConnectionTest {

    private fun createServiceConnection(): CNServiceConnection {
        return CNServiceConnection(mock(LocalBroadCastUtil::class.java))
    }

    @Test
    fun when_service_connected_mark_self_as_bonded_test() {
        val mockCNWalletService: CNWalletService? = null
        val cnServiceConnection = createServiceConnection()
        val binder = mock(CNWalletBinder::class.java)
        whenever(binder.service).thenReturn(mockCNWalletService)

        cnServiceConnection.onServiceConnected(mock(ComponentName::class.java), binder)

        assertTrue(cnServiceConnection.isBounded)
    }

    @Test
    fun when_service_connected_set_CNWalletService_object_test() {
        val mockCNWalletService: CNWalletService? = null
        val cnServiceConnection = createServiceConnection()
        val binder = mock(CNWalletBinder::class.java)
        whenever(binder.service).thenReturn(mockCNWalletService)

        cnServiceConnection.onServiceConnected(mock(ComponentName::class.java), binder)
        val currentService = cnServiceConnection.cnWalletServicesInterface

        assertTrue(currentService === mockCNWalletService)
    }

    @Test
    fun when_service_connected_broadcast_local_message_test() {
        val mockCNWalletService: CNWalletService? = null
        val cnServiceConnection = createServiceConnection()
        val binder = mock(CNWalletBinder::class.java)
        whenever(binder.service).thenReturn(mockCNWalletService)

        cnServiceConnection.onServiceConnected(mock(ComponentName::class.java), binder)

        verify(cnServiceConnection.localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED)
    }
}