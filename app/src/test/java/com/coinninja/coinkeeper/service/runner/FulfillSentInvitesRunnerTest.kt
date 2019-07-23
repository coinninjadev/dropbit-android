package com.coinninja.coinkeeper.service.runner

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class FulfillSentInvitesRunnerTest {

    private fun createRunner(): FulfillSentInvitesRunner {
        return FulfillSentInvitesRunner(
                mock(InviteTransactionSummaryHelper::class.java),
                mock(SentInvitesStatusGetter::class.java),
                mock(SentInvitesStatusSender::class.java),
                mock(BroadcastBtcInviteRunner::class.java)
        )
    }

    @Test
    fun successful_full_flow() {
        val runner = createRunner()
        val invite = mock(InviteTransactionSummary::class.java)
        whenever(invite.btcTransactionId).thenReturn(null)
        whenever(runner.inviteTransactionSummaryHelper.unfulfilledSentInvites).thenReturn(listOf(invite))

        val ordered = inOrder(runner.inviteTransactionSummaryHelper, runner.sentInvitesStatusGetter, runner.sentInvitesStatusSender, runner.broadcastBtcInviteRunner)
        runner.run()

        //Step 1. grab all sent invites from server, save/update the ones that now have an address
        ordered.verify(runner.sentInvitesStatusGetter).run()

        //Step 2. addressForPubKey any sent invites that do not have a tx id but have an address
        ordered.verify(runner.inviteTransactionSummaryHelper).unfulfilledSentInvites

        //Step 3. report to coinninja server of any invites that have been newly fulfilled (with TX ID)
        ordered.verify(runner.broadcastBtcInviteRunner).invite = invite
        ordered.verify(runner.broadcastBtcInviteRunner).run()

        ordered.verify(runner.sentInvitesStatusSender).run()
    }

    @Test
    fun one_unfulfilled_test() {
        val runner = createRunner()
        val unfulfilled = mock(InviteTransactionSummary::class.java)
        val unfulfilledTransactions = listOf(unfulfilled)
        whenever(runner.inviteTransactionSummaryHelper.unfulfilledSentInvites).thenReturn(unfulfilledTransactions)

        runner.run()

        verify(runner.broadcastBtcInviteRunner).run()
    }

    @Test
    fun three_or_more_unfulfilled_test() {
        val runner = createRunner()
        val unfulfilled1 = mock(InviteTransactionSummary::class.java)
        val unfulfilled2 = mock(InviteTransactionSummary::class.java)
        val unfulfilled3 = mock(InviteTransactionSummary::class.java)
        val unfulfilledTransactions = listOf(unfulfilled1, unfulfilled2, unfulfilled3)
        whenever(runner.inviteTransactionSummaryHelper.unfulfilledSentInvites).thenReturn(unfulfilledTransactions)

        runner.run()

        verify(runner.broadcastBtcInviteRunner, times(3)).run()
    }

    @Test
    fun skip_invites_with_tx_id_test() {
        val runner = createRunner()
        val unfulfilled1 = mock(InviteTransactionSummary::class.java)
        val fulfilled2 = mock(InviteTransactionSummary::class.java)
        val unfulfilled3 = mock(InviteTransactionSummary::class.java)
        val unfulfilledTransactions = listOf(unfulfilled1, fulfilled2, unfulfilled3)
        whenever(fulfilled2.btcTransactionId).thenReturn("--txid--")
        whenever(runner.inviteTransactionSummaryHelper.unfulfilledSentInvites).thenReturn(unfulfilledTransactions)

        runner.run()

        verify(runner.broadcastBtcInviteRunner, times(2)).run()
    }

    @Test
    fun check_any_invite_to_see_if_it_has_a_tx_id_test() {
        val runner = createRunner()
        val sampleTxID = "some tx id"
        val fulfilled = mock(InviteTransactionSummary::class.java)
        whenever(fulfilled.btcTransactionId).thenReturn(sampleTxID)

        val alreadyHasTxId = runner.alreadyHasTxId(fulfilled)

        assertTrue(alreadyHasTxId)
    }

    @Test
    fun check_any_invite_to_see_if_it_has_a_null_tx_id_test() {
        val runner = createRunner()
        val sampleTxID: String? = null
        val fulfilled = mock(InviteTransactionSummary::class.java)
        whenever(fulfilled.btcTransactionId).thenReturn(sampleTxID)

        val alreadyHasTxId = runner.alreadyHasTxId(fulfilled)

        assertFalse(alreadyHasTxId)
    }

    @Test
    fun check_any_invite_to_see_if_it_has_a_empty_tx_id_test() {
        val runner = createRunner()
        val sampleTxID = ""
        val fulfilled = mock(InviteTransactionSummary::class.java)
        whenever(fulfilled.btcTransactionId).thenReturn(sampleTxID)

        val alreadyHasTxId = runner.alreadyHasTxId(fulfilled)

        assertFalse(alreadyHasTxId)
    }
}