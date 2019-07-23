package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test

class TransactionInviteSummaryHelperTest {

    @Test
    fun inserts_new_join_record_for_provided_transaction_summary_when_one_does_not_exist() {
        val transactionSummary: TransactionSummary = mock()
        val transactionsInvitesSummary: TransactionsInvitesSummary = mock()
        val transactionInviteSummaryHelper = TransactionInviteSummaryHelper(mock(), mock(), mock())
        val now = System.currentTimeMillis()
        whenever(transactionSummary.txTime).thenReturn(now)
        whenever(transactionInviteSummaryHelper.transactionInviteSummaryQueryManager.getTransactionInviteSummaryByTransactionSummary(transactionSummary)).thenReturn(null)
        whenever(transactionInviteSummaryHelper.daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary)

        val orderedOperation = inOrder(transactionInviteSummaryHelper.daoSessionManager, transactionsInvitesSummary)

        val summary = transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transactionSummary)

        // check values
        verify(transactionsInvitesSummary).transactionSummary = transactionSummary
        verify(transactionInviteSummaryHelper.daoSessionManager).insert(transactionsInvitesSummary)
        verify(summary).btcTxTime = now
        // check order
        orderedOperation.verify(transactionInviteSummaryHelper.daoSessionManager).newTransactionInviteSummary()
        orderedOperation.verify(transactionsInvitesSummary).transactionSummary = transactionSummary
        orderedOperation.verify(transactionInviteSummaryHelper.daoSessionManager).insert(transactionsInvitesSummary)

        assertThat(summary).isNotNull()
    }

    @Test
    fun populates_with_invite() {
        val helper = TransactionInviteSummaryHelper(mock(), mock(), mock())
        val invite: InviteTransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(invite.fromUser).thenReturn(mock())
        whenever(invite.toUser).thenReturn(mock())
        whenever(invite.btcTransactionId).thenReturn("--txid--")
        whenever(invite.btcState).thenReturn(BTCState.FULFILLED)
        whenever(invite.sentDate).thenReturn(System.currentTimeMillis())
        whenever(invite.transactionsInvitesSummary).thenReturn(settlement)
        helper.populateWith(settlement, invite)

        val ordered = inOrder(settlement)

        ordered.verify(settlement).inviteTransactionSummary = invite
        ordered.verify(settlement).fromUser = invite.fromUser
        ordered.verify(settlement).toUser = invite.toUser
        ordered.verify(settlement).transactionTxID = invite.btcTransactionId
        ordered.verify(settlement).update()
        ordered.verify(settlement).btcTxTime = invite.sentDate
        ordered.verify(settlement).inviteTime = 0
        ordered.verify(settlement).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__UNFULFILLED() {
        val transactionInviteSummaryHelper = TransactionInviteSummaryHelper(mock(), mock(), mock())
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.UNFULFILLED)
        transactionInviteSummaryHelper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 100
        verify(transactionsInvitesSummary).btcTxTime = 0
        verify(transactionsInvitesSummary).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__EXPIRED() {
        val transactionInviteSummaryHelper = TransactionInviteSummaryHelper(mock(), mock(), mock())
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.EXPIRED)
        transactionInviteSummaryHelper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 0
        verify(transactionsInvitesSummary).btcTxTime = 100

        verify(transactionsInvitesSummary).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__CANCELED() {
        val transactionInviteSummaryHelper = TransactionInviteSummaryHelper(mock(), mock(), mock())
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.CANCELED)
        transactionInviteSummaryHelper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 0
        verify(transactionsInvitesSummary).btcTxTime = 100

        verify(transactionsInvitesSummary).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__FULFILLED() {
        val transactionInviteSummaryHelper = TransactionInviteSummaryHelper(mock(), mock(), mock())
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.FULFILLED)
        transactionInviteSummaryHelper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 0
        verify(transactionsInvitesSummary).btcTxTime = 100

        verify(transactionsInvitesSummary).update()
    }
}