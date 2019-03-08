package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.converter.PhoneNumberConverter;
import com.google.i18n.phonenumbers.Phonenumber;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

@Entity(active = true)
public class TransactionNotification {

    @Id
    Long id;
    String dropbitMeHandle;
    String avatar;
    String displayName;
    String memo;
    boolean isShared;
    long amount;
    String amountCurrency;
    String txid;
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

    @Convert(converter = PhoneNumberConverter.class, columnType = String.class)
    @Property
    private PhoneNumber phoneNumber;

    @Generated(hash = 131708261)
    public TransactionNotification(Long id, String dropbitMeHandle, String avatar, String displayName,
            String memo, boolean isShared, long amount, String amountCurrency, String txid,
            PhoneNumber phoneNumber) {
        this.id = id;
        this.dropbitMeHandle = dropbitMeHandle;
        this.avatar = avatar;
        this.displayName = displayName;
        this.memo = memo;
        this.isShared = isShared;
        this.amount = amount;
        this.amountCurrency = amountCurrency;
        this.txid = txid;
        this.phoneNumber = phoneNumber;
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

    public String getDropbitMeHandle() {
        return dropbitMeHandle;
    }

    public void setDropbitMeHandle(String dropbitMeHandle) {
        this.dropbitMeHandle = dropbitMeHandle;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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
        return this.txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 819572712)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTransactionNotificationDao() : null;
    }
}
