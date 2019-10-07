package com.coinninja.coinkeeper.service.runner

import app.coinninja.cn.thunderdome.model.LedgerInvoice
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.*

class FulfillSentInvitesRunnerTest {

    private fun createRunner(): FulfillSentInvitesRunner =
            FulfillSentInvitesRunner(mock(), mock(), mock(), mock(), mock(), mock())

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
    fun broadcast_invites_on_thunderdome() {
        val runner = createRunner()
        val pendingInvite: InviteTransactionSummary = mock()
        whenever(pendingInvite.serverId).thenReturn("--server-id--")
        val invite: InviteTransactionSummary = mock()
        whenever(invite.serverId).thenReturn("--server-id--")
        whenever(invite.address).thenReturn("--ln-invoice-id--")
        whenever(invite.valueSatoshis).thenReturn(100_000)
        whenever(runner.thunderDomeRepository.pay("--ln-invoice-id--", 100_000))
                .thenReturn(LedgerInvoice(value = 100_000, id = "--txid--"))

        runner.broadcastInvitesOnThunderDome(listOf(pendingInvite, invite))

        val ordered = inOrder(runner.thunderDomeRepository, invite, runner.cnClient)

        ordered.verify(runner.thunderDomeRepository).pay("--ln-invoice-id--", 100_000)
        ordered.verify(invite).btcState = BTCState.FULFILLED
        ordered.verify(invite).update()
        ordered.verify(runner.cnClient).updateInviteStatusCompleted("--server-id--", "--txid--")
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