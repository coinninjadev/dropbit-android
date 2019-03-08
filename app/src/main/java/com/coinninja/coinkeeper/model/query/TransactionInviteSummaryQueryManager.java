package com.coinninja.coinkeeper.model.query;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;

import javax.inject.Inject;

public class TransactionInviteSummaryQueryManager {

    private final DaoSessionManager daoSessionManager;

    @Inject
    TransactionInviteSummaryQueryManager(DaoSessionManager daoSessionManager) {
        this.daoSessionManager = daoSessionManager;
    }

    public TransactionsInvitesSummary getTransactionInviteSummaryByTransactionSummary(TransactionSummary transaction) {
        return daoSessionManager.getTransactionsInvitesSummaryDao()
                .queryBuilder().whereOr(TransactionsInvitesSummaryDao.Properties.TransactionSummaryID.eq(transaction.getId()),
                        TransactionsInvitesSummaryDao.Properties.InviteTxID.eq(transaction.getTxid()))
                .limit(1).unique();
    }

}
