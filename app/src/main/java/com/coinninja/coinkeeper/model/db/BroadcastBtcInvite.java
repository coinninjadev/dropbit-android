package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.converter.BTCStateConverter;
import com.coinninja.coinkeeper.model.db.enums.BTCState;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

@Entity(active = true)
public class BroadcastBtcInvite {

    @Unique
    @Id(autoincrement = true)
    private Long id;

    @Unique
    @Property
    String broadcastTxID;

    @Convert(converter = BTCStateConverter.class, columnType = Integer.class)
    @Property()
    private BTCState btcState;

    @Property
    String broadcastToAddress;

    @Unique
    @Property
    String inviteServerID;

    @Property
    Long inviteTransactionSummaryID;

    @ToOne(joinProperty = "inviteTransactionSummaryID")
    @Property
    InviteTransactionSummary inviteTransactionSummary;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1645473254)
    private transient BroadcastBtcInviteDao myDao;

    @Generated(hash = 2088230261)
    public BroadcastBtcInvite(Long id, String broadcastTxID, BTCState btcState, String broadcastToAddress,
            String inviteServerID, Long inviteTransactionSummaryID) {
        this.id = id;
        this.broadcastTxID = broadcastTxID;
        this.btcState = btcState;
        this.broadcastToAddress = broadcastToAddress;
        this.inviteServerID = inviteServerID;
        this.inviteTransactionSummaryID = inviteTransactionSummaryID;
    }

    @Generated(hash = 812957227)
    public BroadcastBtcInvite() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBroadcastTxID() {
        return broadcastTxID;
    }

    public void setBroadcastTxID(String broadcastTxID) {
        this.broadcastTxID = broadcastTxID;
    }

    public String getBroadcastToAddress() {
        return broadcastToAddress;
    }

    public void setBroadcastToAddress(String broadcastToAddress) {
        this.broadcastToAddress = broadcastToAddress;
    }

    public String getInviteServerID() {
        return inviteServerID;
    }

    public void setInviteServerID(String inviteServerID) {
        this.inviteServerID = inviteServerID;
    }

    public Long getInviteTransactionSummaryID() {
        return inviteTransactionSummaryID;
    }

    public void setInviteTransactionSummaryID(Long inviteTransactionSummaryID) {
        this.inviteTransactionSummaryID = inviteTransactionSummaryID;
    }

    @Generated(hash = 803853178)
    private transient Long inviteTransactionSummary__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 568155316)
    public InviteTransactionSummary getInviteTransactionSummary() {
        Long __key = this.inviteTransactionSummaryID;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 69880907)
    public void setInviteTransactionSummary(
            InviteTransactionSummary inviteTransactionSummary) {
        synchronized (this) {
            this.inviteTransactionSummary = inviteTransactionSummary;
            inviteTransactionSummaryID = inviteTransactionSummary == null ? null
                    : inviteTransactionSummary.getId();
            inviteTransactionSummary__resolvedKey = inviteTransactionSummaryID;
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

    public BTCState getBtcState() {
        return btcState;
    }

    public void setBtcState(BTCState btcState) {
        this.btcState = btcState;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 2121905163)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBroadcastBtcInviteDao() : null;
    }

}
