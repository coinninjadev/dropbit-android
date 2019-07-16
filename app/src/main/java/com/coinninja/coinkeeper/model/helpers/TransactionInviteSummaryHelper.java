package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.query.TransactionInviteSummaryQueryManager;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class TransactionInviteSummaryHelper {
    private final DaoSessionManager daoSessionManager;
    private final TransactionInviteSummaryQueryManager transactionInviteSummaryQueryManager;

    @Inject
    TransactionInviteSummaryHelper(DaoSessionManager daoSessionManager,
                                   TransactionInviteSummaryQueryManager transactionInviteSummaryQueryManager) {
        this.daoSessionManager = daoSessionManager;
        this.transactionInviteSummaryQueryManager = transactionInviteSummaryQueryManager;
    }


    public TransactionsInvitesSummary getOrCreateTransactionInviteSummaryFor(TransactionSummary transaction) {
        TransactionsInvitesSummary transactionsInvitesSummary =
                transactionInviteSummaryQueryManager.getTransactionInviteSummaryByTransactionSummary(transaction);

        if (transactionsInvitesSummary == null) {
            transactionsInvitesSummary = daoSessionManager.newTransactionInviteSummary();
            transactionsInvitesSummary.setTransactionSummary(transaction);
            daoSessionManager.insert(transactionsInvitesSummary);
        }

        return transactionsInvitesSummary;
    }
}
