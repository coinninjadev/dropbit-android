package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.cn.wallet.HDWallet;
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class TargetStatHelper {

    private final DaoSessionManager daoSessionManager;
    private final WalletHelper walletHelper;
    private final DustProtectionPreference dustProtectionPreference;

    @Inject
    public TargetStatHelper(DaoSessionManager daoSessionManager, WalletHelper walletHelper, DustProtectionPreference dustProtectionPreference) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
        this.dustProtectionPreference = dustProtectionPreference;
    }

    public List<TargetStat> getSpendableTargets() {
        Wallet wallet = walletHelper.getWallet();
        TargetStatDao dao = daoSessionManager.getTargetStatDao();
        QueryBuilder<TargetStat> queryBuilder = dao.queryBuilder();
        Join<TargetStat, TransactionSummary> transactionSummaryJoin = queryBuilder.join(TargetStatDao.Properties.Tsid, TransactionSummary.class);
        transactionSummaryJoin.where(
                TransactionSummaryDao.Properties.MemPoolState.notIn(
                        MemPoolState.FAILED_TO_BROADCAST.getId(),
                        MemPoolState.DOUBLE_SPEND.getId(),
                        MemPoolState.ORPHANED
                )
        );

        queryBuilder.where(
                TargetStatDao.Properties.State.notEq(TargetStat.State.CANCELED.getId()),
                TargetStatDao.Properties.WalletId.eq(wallet.getId()),
                TargetStatDao.Properties.FundingId.isNull(),
                TargetStatDao.Properties.Value.gt(getSpendableMinimum()),
                TargetStatDao.Properties.AddressId.isNotNull()
        );


        queryBuilder.orderAsc(TargetStatDao.Properties.TxTime);
        List<TargetStat> targetStats = queryBuilder.list();

        List<TargetStat> toRemove = new ArrayList<>();

        for (TargetStat targetStat : targetStats) {
            if (targetStat.getAddress().getChangeIndex() == HDWallet.EXTERNAL &&
                    targetStat.getTransaction().getNumConfirmations() < 1) {
                toRemove.add(targetStat);
            }
        }

        for (TargetStat targetStat : toRemove) {
            targetStats.remove(targetStat);
        }

        toRemove.clear();

        return targetStats;
    }

    private long getSpendableMinimum() {
        long minimum = 0L;

        if (dustProtectionPreference.isDustProtectionEnabled()) {
            minimum = 999L;
        }

        return minimum;
    }
}
