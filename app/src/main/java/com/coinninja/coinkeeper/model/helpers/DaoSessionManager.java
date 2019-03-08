package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.AccountDao;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.db.ExternalNotificationDao;
import com.coinninja.coinkeeper.model.db.FundingStat;
import com.coinninja.coinkeeper.model.db.FundingStatDao;
import com.coinninja.coinkeeper.model.db.InternalNotificationDao;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TargetStat;
import com.coinninja.coinkeeper.model.db.TargetStatDao;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionNotificationDao;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummaryDao;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummaryDao;
import com.coinninja.coinkeeper.model.db.User;
import com.coinninja.coinkeeper.model.db.UserDao;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.WalletDao;
import com.coinninja.coinkeeper.model.db.WordDao;

import org.greenrobot.greendao.database.Database;

import javax.inject.Singleton;

@Singleton
public class DaoSessionManager {
    private DaoSession session;
    private DaoMaster daoMaster;

    // todo manage access to other daos
    public DaoSessionManager(DaoMaster daoMaster) {
        this.daoMaster = daoMaster;
    }

    public void clear() {
        session.clear();
    }

    public DaoSessionManager connect() {
        session = daoMaster.newSession();
        return this;
    }

    @Deprecated
    public DaoSession getDaoSession() {
        return session;
    }

    private void dropAllTables() {
        Database db = session.getDatabase();
        DaoMaster.dropAllTables(db, true);
    }

    private void createAllTables() {
        Database db = session.getDatabase();
        DaoMaster.createAllTables(db, true);
    }

    public void resetAll() {
        clear();
        dropAllTables();
        createAllTables();
        connect();
    }

    public void runRaw(String query) {
        session.getDatabase().execSQL(query);
    }

    public WalletDao getWalletDao() {
        return session.getWalletDao();
    }

    public WordDao getWordDao() {
        return session.getWordDao();
    }

    public AddressDao getAddressDao() {
        return session.getAddressDao();
    }

    public TargetStatDao getTargetStatDao() {
        return session.getTargetStatDao();
    }

    public FundingStatDao getFundingStatDao() {
        return session.getFundingStatDao();
    }

    public TransactionSummaryDao getTransactionSummaryDao() {
        return session.getTransactionSummaryDao();
    }

    public TransactionsInvitesSummaryDao getTransactionsInvitesSummaryDao() {
        return session.getTransactionsInvitesSummaryDao();
    }

    public UserDao getUserDao() {
        return session.getUserDao();
    }

    public InviteTransactionSummaryDao getInviteTransactionSummaryDao() {
        return session.getInviteTransactionSummaryDao();
    }

    public AccountDao getAccountDao() {
        return session.getAccountDao();
    }

    public void attach(User user) {
        user.__setDaoSession(session);
    }

    public void createWallet(User user) {
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        getWalletDao().insert(wallet);
    }

    public TransactionsInvitesSummary newTransactionInviteSummary() {
        return new TransactionsInvitesSummary();
    }

    public long insert(TransactionSummary transaction) {
        long id = getTransactionSummaryDao().insert(transaction);
        transaction.setId(id);
        transaction.__setDaoSession(session);
        return id;
    }

    public long insert(TransactionsInvitesSummary summary) {
        long id = getTransactionsInvitesSummaryDao().insert(summary);
        summary.setId(id);
        summary.__setDaoSession(session);
        return id;
    }

    public long insert(TransactionNotification notification) {
        return getTransactionNotificationDao().insert(notification);
    }

    public long insert(InviteTransactionSummary inviteTransactionSummary) {
        return session.getInviteTransactionSummaryDao().insert(inviteTransactionSummary);
    }

    public long insert(TargetStat targetStat) {
        long id = getTargetStatDao().insert(targetStat);
        targetStat.setId(id);
        targetStat.__setDaoSession(session);
        return id;
    }

    public TransactionSummary newTransactionSummary() {
        return new TransactionSummary();
    }

    public TransactionNotification newTransactionNotification() {
        return new TransactionNotification();
    }

    public InviteTransactionSummary newInviteTransactionSummary() {
        return new InviteTransactionSummary();
    }

    public InternalNotificationDao getInternalNotificationDao() {
        return session.getInternalNotificationDao();
    }

    public BroadcastBtcInviteDao getBroadcastBtcInviteDao() {
        return session.getBroadcastBtcInviteDao();
    }

    public ExternalNotificationDao getExternalNotificationDao() {
        return session.getExternalNotificationDao();
    }

    public TransactionNotificationDao getTransactionNotificationDao() {
        return session.getTransactionNotificationDao();
    }

    public TargetStat newTargetStat() {
        return new TargetStat();
    }

    public void attach(TransactionSummary transactionSummary) {
        transactionSummary.__setDaoSession(session);
    }

    public FundingStat newFundingStat() {
        return new FundingStat();
    }

    public long insert(FundingStat fundingStat) {
        long id = session.insert(fundingStat);
        fundingStat.setId(id);
        fundingStat.__setDaoSession(session);
        return id;
    }
}
