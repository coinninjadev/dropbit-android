package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.converter.PhoneNumberConverter;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;

@Entity(active = true)
public class TransactionsInvitesSummary {

    @Id(autoincrement = true)
    private Long id;

    @Property
    Long transactionSummaryID;

    @Property
    String toName;

    @Convert(converter = PhoneNumberConverter.class, columnType = String.class)
    @Property
    PhoneNumber toPhoneNumber;

    @ToOne(joinProperty = "transactionSummaryID")
    @Property
    TransactionSummary transactionSummary;

    @Property
    Long inviteSummaryID;

    @ToOne(joinProperty = "inviteSummaryID")
    @Property
    InviteTransactionSummary inviteTransactionSummary;

    @Property
    long inviteTime;

    @Property
    long btcTxTime;

    @Property
    String transactionTxID;

    @Property
    String inviteTxID;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1017584317)
    private transient TransactionsInvitesSummaryDao myDao;

    @Generated(hash = 1891857737)
    private transient Long transactionSummary__resolvedKey;

    @Generated(hash = 803853178)
    private transient Long inviteTransactionSummary__resolvedKey;

    public TransactionsInvitesSummary(Long id, Long transactionSummaryID, Long transactionNotificationID,
            String toName, PhoneNumber toPhoneNumber, Long inviteSummaryID, long inviteTime, long btcTxTime,
            String transactionTxID, String inviteTxID) {
        this.id = id;
        this.transactionSummaryID = transactionSummaryID;
        this.toName = toName;
        this.toPhoneNumber = toPhoneNumber;
        this.inviteSummaryID = inviteSummaryID;
        this.inviteTime = inviteTime;
        this.btcTxTime = btcTxTime;
        this.transactionTxID = transactionTxID;
        this.inviteTxID = inviteTxID;
    }

    @Generated(hash = 839588361)
    public TransactionsInvitesSummary(Long id, Long transactionSummaryID, String toName,
            PhoneNumber toPhoneNumber, Long inviteSummaryID, long inviteTime, long btcTxTime,
            String transactionTxID, String inviteTxID) {
        this.id = id;
        this.transactionSummaryID = transactionSummaryID;
        this.toName = toName;
        this.toPhoneNumber = toPhoneNumber;
        this.inviteSummaryID = inviteSummaryID;
        this.inviteTime = inviteTime;
        this.btcTxTime = btcTxTime;
        this.transactionTxID = transactionTxID;
        this.inviteTxID = inviteTxID;
    }

    @Generated(hash = 646972297)
    public TransactionsInvitesSummary() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransactionSummaryID() {
        return this.transactionSummaryID;
    }

    public void setTransactionSummaryID(Long transactionSummaryID) {
        this.transactionSummaryID = transactionSummaryID;
    }

    public String getToName() {
        return this.toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Long getInviteSummaryID() {
        return this.inviteSummaryID;
    }

    public void setInviteSummaryID(Long inviteSummaryID) {
        this.inviteSummaryID = inviteSummaryID;
    }

    public long getInviteTime() {
        return this.inviteTime;
    }

    public void setInviteTime(long inviteTime) {
        this.inviteTime = inviteTime;
    }

    public long getBtcTxTime() {
        return this.btcTxTime;
    }

    public void setBtcTxTime(long btcTxTime) {
        this.btcTxTime = btcTxTime;
    }

    public String getTransactionTxID() {
        return this.transactionTxID;
    }

    public void setTransactionTxID(String transactionTxID) {
        this.transactionTxID = transactionTxID;
    }

    public String getInviteTxID() {
        return this.inviteTxID;
    }

    public void setInviteTxID(String inviteTxID) {
        this.inviteTxID = inviteTxID;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 950313541)
    public TransactionSummary getTransactionSummary() {
        Long __key = this.transactionSummaryID;
        if (transactionSummary__resolvedKey == null || !transactionSummary__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionSummaryDao targetDao = daoSession.getTransactionSummaryDao();
            TransactionSummary transactionSummaryNew = targetDao.load(__key);
            synchronized (this) {
                transactionSummary = transactionSummaryNew;
                transactionSummary__resolvedKey = __key;
            }
        }
        return transactionSummary;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 124943922)
    public void setTransactionSummary(TransactionSummary transactionSummary) {
        synchronized (this) {
            this.transactionSummary = transactionSummary;
            transactionSummaryID = transactionSummary == null ? null : transactionSummary.getId();
            transactionSummary__resolvedKey = transactionSummaryID;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 408633770)
    public InviteTransactionSummary getInviteTransactionSummary() {
        Long __key = this.inviteSummaryID;
        if (inviteTransactionSummary__resolvedKey == null
                || !inviteTransactionSummary__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            InviteTransactionSummaryDao targetDao = daoSession.getInviteTransactionSummaryDao();
            InviteTransactionSummary inviteTransactionSummaryNew = targetDao.load(__key);
            synchronized (this) {
                inviteTransactionSummary = inviteTransactionSummaryNew;
                inviteTransactionSummary__resolvedKey = __key;
            }
        }
        return inviteTransactionSummary;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 690463659)
    public void setInviteTransactionSummary(InviteTransactionSummary inviteTransactionSummary) {
        synchronized (this) {
            this.inviteTransactionSummary = inviteTransactionSummary;
            inviteSummaryID = inviteTransactionSummary == null ? null : inviteTransactionSummary.getId();
            inviteTransactionSummary__resolvedKey = inviteSummaryID;
        }
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

    public PhoneNumber getToPhoneNumber() {
        return this.toPhoneNumber;
    }


    public void setToPhoneNumber(PhoneNumber toPhoneNumber) {
        this.toPhoneNumber = toPhoneNumber;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 148830996)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransactionsInvitesSummaryDao() : null;
    }

}
