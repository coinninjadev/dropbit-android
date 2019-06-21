package com.coinninja.coinkeeper.model.db;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

@Entity(active = true)
public class TransactionNotification {

    @Id
    Long id;
    String memo;
    boolean isShared;
    long amount;
    String amountCurrency;
    String txid;

    Long toUserIdentityId;
    @ToOne(joinProperty = "toUserIdentityId")
    UserIdentity toUser;

    Long fromUserIdentityId;
    @ToOne(joinProperty = "fromUserIdentityId")
    UserIdentity fromUser;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 222728965)
    private transient TransactionNotificationDao myDao;
    @Generated(hash = 1661025460)
    private transient Long toUser__resolvedKey;
    @Generated(hash = 468263943)
    private transient Long fromUser__resolvedKey;

    @Generated(hash = 1482331413)
    public TransactionNotification(Long id, String memo, boolean isShared, long amount, String amountCurrency, String txid,
                                   Long toUserIdentityId, Long fromUserIdentityId) {
        this.id = id;
        this.memo = memo;
        this.isShared = isShared;
        this.amount = amount;
        this.amountCurrency = amountCurrency;
        this.txid = txid;
        this.toUserIdentityId = toUserIdentityId;
        this.fromUserIdentityId = fromUserIdentityId;
    }

    @Generated(hash = 1638360505)
    public TransactionNotification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean getIsShared() {
        return isShared;
    }

    public void setIsShared(boolean isShared) {
        this.isShared = isShared;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public void setAmountCurrency(String amountCurrency) {
        this.amountCurrency = amountCurrency;
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


    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Long getToUserIdentityId() {
        return toUserIdentityId;
    }

    public void setToUserIdentityId(Long toUserIdentityId) {
        this.toUserIdentityId = toUserIdentityId;
    }

    public Long getFromUserIdentityId() {
        return fromUserIdentityId;
    }

    public void setFromUserIdentityId(Long fromUserIdentityId) {
        this.fromUserIdentityId = fromUserIdentityId;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1597379521)
    public UserIdentity getToUser() {
        Long __key = this.toUserIdentityId;
        if (toUser__resolvedKey == null || !toUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserIdentityDao targetDao = daoSession.getUserIdentityDao();
            UserIdentity toUserNew = targetDao.load(__key);
            synchronized (this) {
                toUser = toUserNew;
                toUser__resolvedKey = __key;
            }
        }
        return toUser;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1187746829)
    public void setToUser(UserIdentity toUser) {
        synchronized (this) {
            this.toUser = toUser;
            toUserIdentityId = toUser == null ? null : toUser.getId();
            toUser__resolvedKey = toUserIdentityId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1726127259)
    public UserIdentity getFromUser() {
        Long __key = this.fromUserIdentityId;
        if (fromUser__resolvedKey == null || !fromUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserIdentityDao targetDao = daoSession.getUserIdentityDao();
            UserIdentity fromUserNew = targetDao.load(__key);
            synchronized (this) {
                fromUser = fromUserNew;
                fromUser__resolvedKey = __key;
            }
        }
        return fromUser;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 938191438)
    public void setFromUser(UserIdentity fromUser) {
        synchronized (this) {
            this.fromUser = fromUser;
            fromUserIdentityId = fromUser == null ? null : fromUser.getId();
            fromUser__resolvedKey = fromUserIdentityId;
        }
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 819572712)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransactionNotificationDao() : null;
    }
}
