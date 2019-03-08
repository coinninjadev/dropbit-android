package com.coinninja.coinkeeper.model.query;

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class InviteSummaryQueryManager {
    private final DaoSessionManager daoSessionManager;

    @Inject
    InviteSummaryQueryManager(DaoSessionManager daoSessionManager) {

        this.daoSessionManager = daoSessionManager;
    }

    @Nullable
    public InviteTransactionSummary getInviteSummaryByCnId(String cnId) {
        return daoSessionManager.getInviteTransactionSummaryDao().queryBuilder()
                .where(InviteTransactionSummaryDao.Properties.ServerId.eq(cnId)).unique();
    }
}
