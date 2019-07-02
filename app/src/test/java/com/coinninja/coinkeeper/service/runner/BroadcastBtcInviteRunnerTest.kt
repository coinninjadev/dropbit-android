package com.coinninja.coinkeeper.service.runner

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coinninja.bindings.DerivationPath
import com.coinninja.bindings.TransactionBroadcastResult
import com.coinninja.bindings.TransactionData
import com.coinninja.bindings.UnspentTransactionOutput
import com.coinninja.coinkeeper.bitcoin.BroadcastTransactionHelper
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager
import com.coinninja.coinkeeper.cn.wallet.tx.TransactionFundingManager
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.coinninja.coinkeeper.model.helpers.TransactionHelper
import com.coinninja.coinkeeper.util.analytics.Analytics
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class BroadcastBtcInviteRunnerTest {
    private fun createBroadcastInviteRunner(): BroadcastBtcInviteRunner {
        return BroadcastBtcInviteRunner(
                ApplicationProvider.getApplicationContext(),
                mock(CNWalletManager::class.java),
                mock(TransactionFundingManager::class.java),
                mock(TransactionNotificationManager::class.java),
                mock(InviteTransactionSummaryHelper::class.java),
                mock(TransactionHelper::class.java),
                mock(BroadcastBtcInviteHelper::class.java),
                mock(BroadcastTransactionHelper::class.java),
                mock(SyncWalletManager::class.java),
                mock(ExternalNotificationHelper::class.java),
                mock(Analytics::class.java)
        )
    }

    @Test
    fun full_funding_run_successful_test() {
        val runner = createBroadcastInviteRunner()
        val transactionBroadcastResult = mock(TransactionBroadcastResult::class.java)
        whenever(transactionBroadcastResult.isSuccess).thenReturn(true)
        whenever(runner.broadcastHelper.broadcast(any())).thenReturn(transactionBroadcastResult)
        runner.invite = mock(InviteTransactionSummary::class.java)
        whenever(runner.invite!!.valueFeesSatoshis).thenReturn(100)
        whenever(runner.invite!!.valueSatoshis).thenReturn(10000)
        val transactionsInvitesSummary = mock(TransactionsInvitesSummary::class.java)
        whenever(runner.invite!!.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)
        whenever(runner.invite!!.address).thenReturn("--address--")
        val transactionData = TransactionData(
                arrayOf(mock(UnspentTransactionOutput::class.java)),
                runner.invite!!.valueSatoshis,
                runner.invite!!.valueFeesSatoshis,
                0,
                mock(DerivationPath::class.java),
                ""
        )

        whenever(runner.transactionFundingManager.buildFundedTransactionDataForDropBit(runner.invite!!.address,
                runner.invite!!.valueSatoshis, runner.invite!!.valueFeesSatoshis)).thenReturn(transactionData)

        runner.run()

        assertThat(transactionData.paymentAddress, equalTo<String>("--address--"))
        verify(runner.broadcastHelper).broadcast(transactionData)
        verify(runner.inviteTransactionSummaryHelper).updateFulfilledInvite(transactionsInvitesSummary, transactionBroadcastResult)
        verify(runner.transactionNotificationManager).notifyCnOfFundedInvite(runner.invite!!)
        verify(runner.syncWalletManager).syncNow()
        verify(runner.analytics).trackEvent(Analytics.EVENT_DROPBIT_COMPLETED)
    }

    @Test
    fun broadcast_TX_To_Btc_Network_error_test() {
        val runner = createBroadcastInviteRunner()
        runner.invite = mock(InviteTransactionSummary::class.java)
        whenever(runner.invite!!.valueFeesSatoshis).thenReturn(100)
        whenever(runner.invite!!.valueSatoshis).thenReturn(10000)
        whenever(runner.invite!!.transactionsInvitesSummary).thenReturn(mock(TransactionsInvitesSummary::class.java))
        whenever(runner.invite!!.address).thenReturn("--address--")
        val transactionData = TransactionData(
                arrayOf(mock(UnspentTransactionOutput::class.java)),
                runner.invite!!.valueSatoshis,
                runner.invite!!.valueFeesSatoshis,
                0,
                mock(DerivationPath::class.java),
                ""
        )
        whenever(runner.transactionFundingManager.buildFundedTransactionDataForDropBit(runner.invite!!.address,
                runner.invite!!.valueSatoshis, runner.invite!!.valueFeesSatoshis)).thenReturn(transactionData)

        val result = mock(TransactionBroadcastResult::class.java)
        whenever(result.isSuccess).thenReturn(false)
        whenever(runner.broadcastHelper.broadcast(transactionData)).thenReturn(result)

        runner.run()

        verifyZeroInteractions(runner.inviteTransactionSummaryHelper)
    }

    @Test
    fun on_error_funding_cancels_transaction_test() {
        val runner = createBroadcastInviteRunner()
        runner.invite = mock(InviteTransactionSummary::class.java)
        whenever(runner.invite!!.valueFeesSatoshis).thenReturn(100)
        whenever(runner.invite!!.valueSatoshis).thenReturn(10000)
        whenever(runner.invite!!.transactionsInvitesSummary).thenReturn(mock(TransactionsInvitesSummary::class.java))
        whenever(runner.invite!!.address).thenReturn("--address--")
        whenever(runner.invite!!.serverId).thenReturn("--server-id--")
        val transactionData = TransactionData(
                emptyArray(),
                runner.invite!!.valueSatoshis,
                runner.invite!!.valueFeesSatoshis,
                0,
                mock(DerivationPath::class.java),
                ""
        )
        whenever(runner.transactionFundingManager.buildFundedTransactionDataForDropBit(runner.invite!!.address,
                runner.invite!!.valueSatoshis, runner.invite!!.valueFeesSatoshis)).thenReturn(transactionData)

        whenever(runner.invite!!.localeFriendlyDisplayIdentityForReceiver).thenReturn("2565245258")

        runner.run()

        verify(runner.externalNotificationHelper).saveNotification(
                "DropBit to 2565245258 has been canceled", runner.invite!!.serverId)
        verify(runner.cnWalletManager).updateBalances()
        verify(runner.transactionHelper).updateInviteAsCanceled(runner.invite!!.serverId)
    }
}