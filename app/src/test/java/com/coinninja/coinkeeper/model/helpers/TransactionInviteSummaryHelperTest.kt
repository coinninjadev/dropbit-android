package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
import com.coinninja.coinkeeper.model.db.enums.BTCState
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import org.junit.Test

class TransactionInviteSummaryHelperTest {

    private fun createHelper(): TransactionInviteSummaryHelper {
        return TransactionInviteSummaryHelper(mock(), mock(), mock())
    }

    @Test
    fun gets_or_creates_parent_settlement_for_transaction__fetches_when_invite_already_attached_to_a_settlement() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(transaction.transactionsInvitesSummary).thenReturn(settlement)

        assertThat(helper.getOrCreateParentSettlementFor(transaction)).isEqualTo(settlement)
    }

    @Test
    fun gets_or_creates_parent_settlement_for_transaction__returns_transactions_settlement_for_invite_that_shares_txid() {
        val txid = "--txid--"
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val invite: InviteTransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(transaction.txid).thenReturn(txid)
        whenever(transaction.txTime).thenReturn(100L)
        whenever(transaction.transactionsInvitesSummary).thenReturn(null).thenReturn(settlement)
        whenever(invite.transactionsInvitesSummary).thenReturn(settlement)
        whenever(helper.inviteSummaryQueryManager.getInviteSummaryByTxid(txid)).thenReturn(invite)

        assertThat(helper.getOrCreateParentSettlementFor(transaction)).isEqualTo(settlement)

        val ordered = inOrder(transaction, settlement)
        ordered.verify(transaction).transactionsInvitesSummary = settlement
        ordered.verify(transaction).update()
        ordered.verify(settlement).transactionSummary = transaction
        ordered.verify(settlement).transactionTxID = transaction.txid
        ordered.verify(settlement).update()
        ordered.verify(settlement).inviteTime = 0
        ordered.verify(settlement).btcTxTime = transaction.txTime
        ordered.verify(settlement, atLeast(1)).update()
    }

    @Test
    fun gets_or_creates_parent_settlement_for_transaction__returns_newly_saved_and_created_settlement_for_transaction() {
        val txid = "--txid--"
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(transaction.txid).thenReturn(txid)
        whenever(transaction.txTime).thenReturn(100L)
        whenever(transaction.transactionsInvitesSummary).thenReturn(null).thenReturn(settlement)
        whenever(helper.daoSessionManager.newTransactionInviteSummary()).thenReturn(settlement)

        assertThat(helper.getOrCreateParentSettlementFor(transaction)).isEqualTo(settlement)

        val ordered = inOrder(helper.daoSessionManager, transaction, settlement)
        ordered.verify(helper.daoSessionManager).insert(settlement)
        ordered.verify(transaction).transactionsInvitesSummary = settlement
        ordered.verify(transaction).update()
        ordered.verify(settlement).transactionSummary = transaction
        ordered.verify(settlement).transactionTxID = transaction.txid
        ordered.verify(settlement).update()
        ordered.verify(settlement).inviteTime = 0
        ordered.verify(settlement).btcTxTime = transaction.txTime
        ordered.verify(settlement, atLeast(1)).update()
    }

    @Test
    fun gets_or_creates_parent_settlement_for_invite__fetches_when_invite_already_attached_to_a_settlement() {
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(invite.transactionsInvitesSummary).thenReturn(settlement)

        assertThat(helper.getOrCreateParentSettlementFor(invite)).isEqualTo(settlement)
    }

    @Test
    fun gets_or_creates_parent_settlement_for_invite__returns_transactions_settlement_for_invite_that_shares_txid() {
        val txid = "--txid--"
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        val transaction: TransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(invite.fromUser).thenReturn(mock())
        whenever(invite.toUser).thenReturn(mock())
        whenever(invite.btcTransactionId).thenReturn(txid)
        whenever(invite.btcState).thenReturn(BTCState.FULFILLED)
        whenever(invite.sentDate).thenReturn(System.currentTimeMillis())
        whenever(invite.transactionsInvitesSummary).thenReturn(settlement)
        whenever(invite.transactionsInvitesSummary).thenReturn(null).thenReturn(settlement)
        whenever(transaction.transactionsInvitesSummary).thenReturn(settlement)
        whenever(helper.transactionQueryManager.transactionByTxid(txid)).thenReturn(transaction)

        assertThat(helper.getOrCreateParentSettlementFor(invite)).isEqualTo(settlement)

        val ordered = inOrder(invite, settlement)
        ordered.verify(invite).transactionsInvitesSummary = settlement
        ordered.verify(invite).update()
        ordered.verify(settlement).inviteTransactionSummary = invite
        ordered.verify(settlement).fromUser = invite.fromUser
        ordered.verify(settlement).toUser = invite.toUser
        ordered.verify(settlement).transactionTxID = invite.btcTransactionId
        ordered.verify(settlement).update()
        ordered.verify(settlement).btcTxTime = invite.sentDate
        ordered.verify(settlement).inviteTime = 0
        ordered.verify(settlement, atLeast(1)).update()
    }

    @Test
    fun gets_or_creates_parent_settlement_for_invite__returns_newly_saved_and_created_settlement_for_invite() {
        val txid = "--txid--"
        val helper = createHelper()
        val invite: InviteTransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(invite.fromUser).thenReturn(mock())
        whenever(invite.toUser).thenReturn(mock())
        whenever(invite.btcTransactionId).thenReturn(txid)
        whenever(invite.btcState).thenReturn(BTCState.FULFILLED)
        whenever(invite.sentDate).thenReturn(System.currentTimeMillis())
        whenever(invite.transactionsInvitesSummary).thenReturn(settlement)
        whenever(invite.transactionsInvitesSummary).thenReturn(null).thenReturn(settlement)
        whenever(helper.daoSessionManager.newTransactionInviteSummary()).thenReturn(settlement)

        assertThat(helper.getOrCreateParentSettlementFor(invite)).isEqualTo(settlement)

        val ordered = inOrder(invite, settlement, helper.daoSessionManager)
        ordered.verify(helper.daoSessionManager).insert(settlement)
        ordered.verify(invite).transactionsInvitesSummary = settlement
        ordered.verify(invite).update()
        ordered.verify(settlement).inviteTransactionSummary = invite
        ordered.verify(settlement).fromUser = invite.fromUser
        ordered.verify(settlement).toUser = invite.toUser
        ordered.verify(settlement).transactionTxID = invite.btcTransactionId
        ordered.verify(settlement).update()
        ordered.verify(settlement).btcTxTime = invite.sentDate
        ordered.verify(settlement).inviteTime = 0
        ordered.verify(settlement, atLeast(1)).update()
    }

    @Test
    fun populates_with_invite() {
        val helper = createHelper()
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
    fun updates_created_time_appropriately_from_transaction_using_transaction_time() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        whenever(transaction.txTime).thenReturn(100)
        whenever(transaction.transactionsInvitesSummary).thenReturn(settlement)

        helper.updateSentTimeFrom(transaction)

        val ordered = inOrder(settlement)
        ordered.verify(settlement).inviteTime = 0
        ordered.verify(settlement).btcTxTime = 100
        ordered.verify(settlement).update()
    }

    @Test
    fun updates_created_time_appropriately_from_transaction_using_invite_time() {
        val helper = createHelper()
        val transaction: TransactionSummary = mock()
        val settlement: TransactionsInvitesSummary = mock()
        val invite: InviteTransactionSummary = mock()
        whenever(transaction.txTime).thenReturn(100)
        whenever(transaction.transactionsInvitesSummary).thenReturn(settlement)
        whenever(settlement.inviteTransactionSummary).thenReturn(invite)

        helper.updateSentTimeFrom(transaction)

        val ordered = inOrder(settlement)
        ordered.verify(settlement, times(0)).inviteTime = any()
        ordered.verify(settlement, times(0)).btcTxTime = any()
        ordered.verify(settlement, times(0)).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__UNFULFILLED() {
        val helper = createHelper()
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.UNFULFILLED)
        helper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 100
        verify(transactionsInvitesSummary).btcTxTime = 0
        verify(transactionsInvitesSummary).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__EXPIRED() {
        val helper = createHelper()
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.EXPIRED)
        helper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 0
        verify(transactionsInvitesSummary).btcTxTime = 100

        verify(transactionsInvitesSummary).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__CANCELED() {
        val helper = createHelper()
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.CANCELED)
        helper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 0
        verify(transactionsInvitesSummary).btcTxTime = 100

        verify(transactionsInvitesSummary).update()
    }

    @Test
    fun updates_created_time_appropriately_from_invite__FULFILLED() {
        val helper = createHelper()
        val invite = mock<InviteTransactionSummary>()
        whenever(invite.sentDate).thenReturn(100)
        val transactionsInvitesSummary = mock<TransactionsInvitesSummary>()
        whenever(invite.transactionsInvitesSummary).thenReturn(transactionsInvitesSummary)

        whenever(invite.btcState).thenReturn(BTCState.FULFILLED)
        helper.updateSentTimeFrom(invite)
        verify(transactionsInvitesSummary).inviteTime = 0
        verify(transactionsInvitesSummary).btcTxTime = 100

        verify(transactionsInvitesSummary).update()
    }
}