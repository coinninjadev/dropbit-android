package com.coinninja.coinkeeper.db.migrations;

import com.coinninja.coinkeeper.db.AbstractMigration;
import com.coinninja.coinkeeper.db.Migration;
import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.FundingStatDao;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.LazyList;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class Migrate_V10_to_V11 extends AbstractMigration {
    @Override
    protected void applyMigration(@NonNull Database db, int currentVersion) {
        alterDatabase(db);
        initWithValues(db);
    }

    @Nullable
    @Override
    public Migration getPreviousMigration() {
        return null;
    }

    @Override
    public int getTargetVersion() {
        return 10;
    }

    @Override
    public int getMigratedVersion() {
        return 11;
    }

    private void alterDatabase(Database db) {
        db.execSQL("ALTER TABLE " + TransactionSummaryDao.TABLENAME +
                " ADD COLUMN MEM_POOL_STATE INTEGER");
        db.execSQL("ALTER TABLE " + TargetStatDao.TABLENAME +
                " ADD COLUMN " + TargetStatDao.Properties.State.columnName +
                " INTEGER");
        db.execSQL("ALTER TABLE " + FundingStatDao.TABLENAME +
                " ADD COLUMN " + FundingStatDao.Properties.State.columnName +
                " INTEGER");
    }


    private void initWithValues(Database db) {
        DaoSession daoSession = new DaoMaster(db).newSession();
        LazyList<TransactionSummary> transactionSummaries = daoSession.getTransactionSummaryDao().
                queryBuilder().listLazyUncached();

        for (TransactionSummary transaction : transactionSummaries) {
            String blockhash = transaction.getBlockhash();
            if (blockhash != null && !blockhash.isEmpty()) {
                transaction.setMemPoolState(MemPoolState.MINED);
                for (TargetStat target : transaction.getReceiver()) {
                    target.setState(TargetStat.State.ACKNOWLEDGE);
                    target.update();
                }
                for (FundingStat funder : transaction.getFunder()) {
                    funder.setState(FundingStat.State.ACKNOWLEDGE);
                    funder.update();
                }
            } else {
                transaction.setMemPoolState(MemPoolState.PENDING);
                for (TargetStat target : transaction.getReceiver()) {
                    target.setState(TargetStat.State.PENDING);
                    target.update();
                }
                for (FundingStat funder : transaction.getFunder()) {
                    funder.setState(FundingStat.State.PENDING);
                    funder.update();
                }
            }

            transaction.update();
        }

        linkTransactionTableToJoinTable(daoSession);
        linkInviteTableToJoinTable(daoSession);

        transactionSummaries.close();
        daoSession.clear();
    }

    private void linkTransactionTableToJoinTable(DaoSession daoSession) {
        TransactionsInvitesSummaryDao joinTableDAO = daoSession.getTransactionsInvitesSummaryDao();

        List<TransactionsInvitesSummary> joinTableEntrys = joinTableDAO.queryBuilder().where(TransactionsInvitesSummaryDao.Properties.InviteSummaryID.isNotNull()).list();

        for (TransactionsInvitesSummary joinTableEntry : joinTableEntrys) {
            TransactionSummary transaction = joinTableEntry.getTransactionSummary();
            if (transaction == null) continue;

            transaction.setTransactionsInvitesSummary(joinTableEntry);
            transaction.setTransactionsInvitesSummaryID(joinTableEntry.getId());

            transaction.update();
            transaction.refresh();
        }
    }

    private void linkInviteTableToJoinTable(DaoSession daoSession) {
        TransactionsInvitesSummaryDao joinTableDAO = daoSession.getTransactionsInvitesSummaryDao();

        List<TransactionsInvitesSummary> joinTableEntrys = joinTableDAO.queryBuilder().where(TransactionsInvitesSummaryDao.Properties.InviteSummaryID.isNotNull()).list();

        for (TransactionsInvitesSummary joinTableEntry : joinTableEntrys) {
            InviteTransactionSummary invite = joinTableEntry.getInviteTransactionSummary();
            if (invite == null) continue;

            invite.setTransactionsInvitesSummary(joinTableEntry);
            invite.setTransactionsInvitesSummaryID(joinTableEntry.getId());

            invite.update();
            invite.refresh();
        }
    }
}
