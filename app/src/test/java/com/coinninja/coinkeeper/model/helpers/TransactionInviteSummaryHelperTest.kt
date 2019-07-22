package com.coinninja.coinkeeper.model.helpers

import com.coinninja.coinkeeper.model.db.TransactionSummary
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary
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
        val transactionInviteSummaryHelper = TransactionInviteSummaryHelper(mock(), mock())
        whenever(transactionInviteSummaryHelper.transactionInviteSummaryQueryManager.getTransactionInviteSummaryByTransactionSummary(transactionSummary)).thenReturn(null)
        whenever(transactionInviteSummaryHelper.daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary)

        val orderedOperation = inOrder(transactionInviteSummaryHelper.daoSessionManager, transactionsInvitesSummary)

        val summary = transactionInviteSummaryHelper.getOrCreateTransactionInviteSummaryFor(transactionSummary)

        // check values
        verify(transactionsInvitesSummary).transactionSummary = transactionSummary
        verify(transactionInviteSummaryHelper.daoSessionManager).insert(transactionsInvitesSummary)

        // check order
        orderedOperation.verify(transactionInviteSummaryHelper.daoSessionManager).newTransactionInviteSummary()
        orderedOperation.verify(transactionsInvitesSummary).transactionSummary = transactionSummary
        orderedOperation.verify(transactionInviteSummaryHelper.daoSessionManager).insert(transactionsInvitesSummary)

        assertThat(summary).isNotNull()
    }


}