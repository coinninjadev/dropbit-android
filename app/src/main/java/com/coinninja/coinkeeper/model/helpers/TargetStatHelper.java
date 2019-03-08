package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;

import java.util.List;

import javax.inject.Inject;

public class TargetStatHelper {

    private final DaoSessionManager daoSessionManager;
    private final WalletHelper walletHelper;

    @Inject
    public TargetStatHelper(DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
    }

    public List<TargetStat> getSpendableTargets() {
        TargetStatDao dao = daoSessionManager.getTargetStatDao();
        List<TargetStat> stats = dao.queryBuilder().where(
                TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.getId()),
                TargetStatDao.Properties.WalletId.eq(walletHelper.getWallet().getId()),
                TargetStatDao.Properties.AddressId.isNotNull(),
                TargetStatDao.Properties.FundingId.isNull()).
                orderAsc(TargetStatDao.Properties.TxTime).list();
        return stats;

    }
}
