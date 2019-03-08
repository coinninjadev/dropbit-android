package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.converter.MemPoolStateConverter;
import com.coinninja.coinkeeper.model.db.enums.MemPoolState;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;

@Entity(active = true)
public class TransactionSummary {
    @Id
    Long id;
    @Unique
    @Property
    String txid;
    @ToMany(referencedJoinProperty = "tsid")
    @Property
    List<FundingStat> funder;
    @ToMany(referencedJoinProperty = "tsid")
    @Property
    List<TargetStat> receiver;
    @ToOne(joinProperty = "walletId")
    Wallet wallet;
    @Property
    Long transactionsInvitesSummaryID;
    @ToOne(joinProperty = "transactionsInvitesSummaryID")
    @Property
    TransactionsInvitesSummary transactionsInvitesSummary;
    @Property
    boolean soughtNotification;
    @Property
    long walletId;
    @Property
    long fee;
    @Property
    long txTime;
    @Property
    int numConfirmations;
    @Property
    String blockhash;
    @Property
    int numInputs;
    @Property
    int numOutputs;
    @Property
    int blockheight;
    @Property
    long historicPrice;
    Long transactionNotificationId;
    @ToOne(joinProperty = "transactionNotificationId")
    TransactionNotification transactionNotification;
    @Convert(converter = MemPoolStateConverter.class, columnType = Integer.class)
    @Property()
    private MemPoolState memPoolState;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 989876454)
    private transient TransactionSummaryDao myDao;


    @Generated(hash = 1265541453)
    public TransactionSummary(Long id, String txid,
            Long transactionsInvitesSummaryID, boolean soughtNotification,
            long walletId, long fee, long txTime, int numConfirmations,
            String blockhash, int numInputs, int numOutputs, int blockheight,
            long historicPrice, Long transactionNotificationId,
            MemPoolState memPoolState) {
        this.id = id;
        this.txid = txid;
        this.transactionsInvitesSummaryID = transactionsInvitesSummaryID;
        this.soughtNotification = soughtNotification;
        this.walletId = walletId;
        this.fee = fee;
        this.txTime = txTime;
        this.numConfirmations = numConfirmations;
        this.blockhash = blockhash;
        this.numInputs = numInputs;
        this.numOutputs = numOutputs;
        this.blockheight = blockheight;
        this.historicPrice = historicPrice;
        this.transactionNotificationId = transactionNotificationId;
        this.memPoolState = memPoolState;
    }

    @Generated(hash = 91817479)
    public TransactionSummary() {
    }

    @Generated(hash = 1885063144)
    private transient Long wallet__resolvedKey;
    @Generated(hash = 1115183563)
    private transient Long transactionsInvitesSummary__resolvedKey;
    @Generated(hash = 562747265)
    private transient Long transactionNotification__resolvedKey;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Long getTransactionsInvitesSummaryID() {
        return transactionsInvitesSummaryID;
    }

    public void setTransactionsInvitesSummaryID(Long transactionsInvitesSummaryID) {
        this.transactionsInvitesSummaryID = transactionsInvitesSummaryID;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getTxTime() {
        return txTime;
    }

    public void setTxTime(long txTime) {
        this.txTime = txTime;
    }

    public int getNumConfirmations() {
        return numConfirmations;
    }

    public void setNumConfirmations(int numConfirmations) {
        this.numConfirmations = numConfirmations;
    }

    public String getBlockhash() {
        return blockhash;
    }

    public void setBlockhash(String blockhash) {
        this.blockhash = blockhash;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public void setNumInputs(int numInputs) {
        this.numInputs = numInputs;
    }

    public int getNumOutputs() {
        return numOutputs;
    }

    public void setNumOutputs(int numOutputs) {
        this.numOutputs = numOutputs;
    }

    public int getBlockheight() {
        return blockheight;
    }

    public void setBlockheight(int blockheight) {
        this.blockheight = blockheight;
    }

    public long getHistoricPrice() {
        return historicPrice;
    }

    public void setHistoricPrice(long historicPrice) {
        this.historicPrice = historicPrice;
    }

    public MemPoolState getMemPoolState() {
        return memPoolState;
    }

    public void setMemPoolState(MemPoolState memPoolState) {
        this.memPoolState = memPoolState;
    }

    public boolean getSoughtNotification() {
        return this.soughtNotification;
    }

    public void setSoughtNotification(boolean soughtNotification) {
        this.soughtNotification = soughtNotification;
    }

    public Long getTransactionNotificationId() {
        return this.transactionNotificationId;
    }

    public void setTransactionNotificationId(Long transactionNotificationId) {
        this.transactionNotificationId = transactionNotificationId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1461174222)
    public Wallet getWallet() {
        long __key = this.walletId;
        if (wallet__resolvedKey == null || !wallet__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            WalletDao targetDao = daoSession.getWalletDao();
            Wallet walletNew = targetDao.load(__key);
            synchronized (this) {
                wallet = walletNew;
                wallet__resolvedKey = __key;
            }
        }
        return wallet;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 276091648)
    public void setWallet(@NotNull Wallet wallet) {
        if (wallet == null) {
            throw new DaoException(
                    "To-one property 'walletId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.wallet = wallet;
            walletId = wallet.getId();
            wallet__resolvedKey = walletId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1465356328)
    public TransactionsInvitesSummary getTransactionsInvitesSummary() {
        Long __key = this.transactionsInvitesSummaryID;
        if (transactionsInvitesSummary__resolvedKey == null
                || !transactionsInvitesSummary__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionsInvitesSummaryDao targetDao = daoSession
                    .getTransactionsInvitesSummaryDao();
            TransactionsInvitesSummary transactionsInvitesSummaryNew = targetDao
                    .load(__key);
            synchronized (this) {
                transactionsInvitesSummary = transactionsInvitesSummaryNew;
                transactionsInvitesSummary__resolvedKey = __key;
            }
        }
        return transactionsInvitesSummary;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 386011818)
    public void setTransactionsInvitesSummary(
            TransactionsInvitesSummary transactionsInvitesSummary) {
        synchronized (this) {
            this.transactionsInvitesSummary = transactionsInvitesSummary;
            transactionsInvitesSummaryID = transactionsInvitesSummary == null ? null
                    : transactionsInvitesSummary.getId();
            transactionsInvitesSummary__resolvedKey = transactionsInvitesSummaryID;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 761085204)
    public TransactionNotification getTransactionNotification() {
        Long __key = this.transactionNotificationId;
        if (transactionNotification__resolvedKey == null
                || !transactionNotification__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionNotificationDao targetDao = daoSession
                    .getTransactionNotificationDao();
            TransactionNotification transactionNotificationNew = targetDao
                    .load(__key);
            synchronized (this) {
                transactionNotification = transactionNotificationNew;
                transactionNotification__resolvedKey = __key;
            }
        }
        return transactionNotification;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 144706267)
    public void setTransactionNotification(
            TransactionNotification transactionNotification) {
        synchronized (this) {
            this.transactionNotification = transactionNotification;
            transactionNotificationId = transactionNotification == null ? null
                    : transactionNotification.getId();
            transactionNotification__resolvedKey = transactionNotificationId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 83884632)
    public List<FundingStat> getFunder() {
        if (funder == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FundingStatDao targetDao = daoSession.getFundingStatDao();
            List<FundingStat> funderNew = targetDao
                    ._queryTransactionSummary_Funder(id);
            synchronized (this) {
                if (funder == null) {
                    funder = funderNew;
                }
            }
        }
        return funder;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 461552668)
    public synchronized void resetFunder() {
        funder = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 400822490)
    public List<TargetStat> getReceiver() {
        if (receiver == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TargetStatDao targetDao = daoSession.getTargetStatDao();
            List<TargetStat> receiverNew = targetDao
                    ._queryTransactionSummary_Receiver(id);
            synchronized (this) {
                if (receiver == null) {
                    receiver = receiverNew;
                }
            }
        }
        return receiver;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 804108067)
    public synchronized void resetReceiver() {
        receiver = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1202648939)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransactionSummaryDao() : null;
    }


}