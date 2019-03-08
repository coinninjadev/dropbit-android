package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.query.TransactionInviteSummaryQueryManager;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionInviteSummaryHelperTest {

    @Mock
    TransactionInviteSummaryQueryManager queryManager;
    @Mock
    DaoSessionManager daoSessionManager;

    @InjectMocks
    TransactionInviteSummaryHelper helper;

    @After
    public void teardown() {
        helper = null;
        queryManager = null;
        daoSessionManager = null;
    }

    @Test
    public void inserts_new_join_record_for_provided_transaction_summary_when_one_does_not_exist() {
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        TransactionsInvitesSummary transactionsInvitesSummary = mock(TransactionsInvitesSummary.class);
        when(queryManager.getTransactionInviteSummaryByTransactionSummary(transactionSummary)).thenReturn(null);
        when(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary);
        InOrder orderedOperation = inOrder(daoSessionManager, transactionsInvitesSummary);

        TransactionsInvitesSummary joinSummary = helper.getOrCreateTransactionInviteSummaryFor(transactionSummary);

        // check values
        verify(transactionsInvitesSummary).setTransactionSummary(transactionSummary);
        verify(daoSessionManager).insert(transactionsInvitesSummary);

        // check order
        orderedOperation.verify(daoSessionManager).newTransactionInviteSummary();
        orderedOperation.verify(transactionsInvitesSummary).setTransactionSummary(transactionSummary);
        orderedOperation.verify(daoSessionManager).insert(transactionsInvitesSummary);
    }


}