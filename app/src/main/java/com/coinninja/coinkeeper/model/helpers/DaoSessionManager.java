package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.AccountDao;
import com.coinninja.coinkeeper.model.db.AddressDao;
import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.DaoMaster;
import com.coinninja.coinkeeper.model.db.DaoSession;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentity;
import com.coinninja.coinkeeper.model.db.DropbitMeIdentityDao;
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
import org.greenrobot.greendao.query.QueryBuilder;

import javax.inject.Singleton;

@Singleton
public class DaoSessionManager {
    private DaoSession session;
    private DaoMaster daoMaster;

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

    public void resetAll() {
        clear();
        dropAllTables();
        createAllTables();
        connect();
    }

    public void runRaw(String query) {
        session.getDatabase().execSQL(query);
    }

    // USER
    public UserDao getUserDao() {
        return session.getUserDao();
    }

    // WALLET
    public WalletDao getWalletDao() {
        return session.getWalletDao();
    }

    public QueryBuilder<Wallet> qeuryForWallet() {
        return getWalletDao().queryBuilder();
    }

    public Wallet createWallet() {
        resetAll();
        User user = new User();
        long id = getUserDao().insert(user);
        Wallet wallet = new Wallet();
        wallet.setUserId(id);
        getWalletDao().insert(wallet);
        return wallet;
    }

    // WORD
    public WordDao getWordDao() {
        return session.getWordDao();
    }

    // ADDRESS
    public AddressDao getAddressDao() {
        return session.getAddressDao();
    }

    // TARGET STAT
    public TargetStatDao getTargetStatDao() {
        return session.getTargetStatDao();
    }

    public long insert(TargetStat targetStat) {
        long id = getTargetStatDao().insert(targetStat);
        targetStat.setId(id);
        targetStat.__setDaoSession(session);
        return id;
    }

    public TargetStat newTargetStat() {
        return new TargetStat();
    }

    // FUNDING STAT
    public FundingStatDao getFundingStatDao() {
        return session.getFundingStatDao();
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

    // TRANSACTION INVITE SUMMARY
    public TransactionsInvitesSummaryDao getTransactionsInvitesSummaryDao() {
        return session.getTransactionsInvitesSummaryDao();
    }

    public TransactionsInvitesSummary newTransactionInviteSummary() {
        return new TransactionsInvitesSummary();
    }

    public long insert(TransactionsInvitesSummary summary) {
        long id = getTransactionsInvitesSummaryDao().insert(summary);
        summary.setId(id);
        summary.__setDaoSession(session);
        return id;
    }

    // TRANSACTION
    public TransactionSummaryDao getTransactionSummaryDao() {
        return session.getTransactionSummaryDao();
    }

    public long insert(TransactionSummary transaction) {
        long id = getTransactionSummaryDao().insert(transaction);
        transaction.setId(id);
        transaction.__setDaoSession(session);
        return id;
    }

    public void attach(TransactionSummary transactionSummary) {
        transactionSummary.__setDaoSession(session);
    }

    public TransactionSummary newTransactionSummary() {
        return new TransactionSummary();
    }

    // TRANSACTION NOTIFICATION
    public long insert(TransactionNotification notification) {
        return getTransactionNotificationDao().insert(notification);
    }

    // TRANSACTION

    public TransactionNotification newTransactionNotification() {
        return new TransactionNotification();
    }

    public TransactionNotificationDao getTransactionNotificationDao() {
        return session.getTransactionNotificationDao();
    }

    // DROPBIT
    public InviteTransactionSummaryDao getInviteTransactionSummaryDao() {
        return session.getInviteTransactionSummaryDao();
    }

    public long insert(InviteTransactionSummary inviteTransactionSummary) {
        return session.getInviteTransactionSummaryDao().insert(inviteTransactionSummary);
    }

    public InviteTransactionSummary newInviteTransactionSummary() {
        return new InviteTransactionSummary();
    }

    // ACCOUNT
    public AccountDao getAccountDao() {
        return session.getAccountDao();
    }

    // INTERNAL NOTIFICATION
    public InternalNotificationDao getInternalNotificationDao() {
        return session.getInternalNotificationDao();
    }

    public ExternalNotificationDao getExternalNotificationDao() {
        return session.getExternalNotificationDao();
    }

    public BroadcastBtcInviteDao getBroadcastBtcInviteDao() {
        return session.getBroadcastBtcInviteDao();
    }

    // Dropbit Me Identity
    public DropbitMeIdentityDao getDropbitMeIdentityDao() {
        return session.getDropbitMeIdentityDao();
    }

    public DropbitMeIdentity newDropbitMeIdentity() {
        return new DropbitMeIdentity();
    }

    public long insert(DropbitMeIdentity dropbitMeIdentity) {
        long id = getDropbitMeIdentityDao().insert(dropbitMeIdentity);
        dropbitMeIdentity.__setDaoSession(session);
        dropbitMeIdentity.setId(id);
        return id;
    }

    private void dropAllTables() {
        Database db = session.getDatabase();
        DaoMaster.dropAllTables(db, true);
    }

    private void createAllTables() {
        Database db = session.getDatabase();
        DaoMaster.createAllTables(db, true);
    }

}
