package com.coinninja.coinkeeper.service

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.mockito.InOrder
import org.mockito.Mockito.inOrder

class SyncDropBitServiceTest {
    @Test
    fun run_all_dropbit_sync_runners_in_order_test() {
        val service = SyncDropBitService()
        service.accountManager = mock()
        service.walletHelper = mock()
        service.syncIncomingInvitesRunner = mock()
        service.receivedInvitesStatusRunner = mock()
        service.fulfillSentInvitesRunner = mock()
        whenever(service.walletHelper.primaryWallet).thenReturn(mock())
        val inOrder: InOrder = inOrder(service.accountManager, service.syncIncomingInvitesRunner, service.fulfillSentInvitesRunner, service.receivedInvitesStatusRunner)

        service.onHandleIntent(null)

        inOrder.verify(service.accountManager).cacheAddresses(service.walletHelper.primaryWallet)
        inOrder.verify(service.syncIncomingInvitesRunner)!!.run()
        inOrder.verify(service.fulfillSentInvitesRunner)!!.run()
        inOrder.verify(service.receivedInvitesStatusRunner)!!.run()
    }
}