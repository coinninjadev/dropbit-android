package com.coinninja.coinkeeper.model.db;

import com.coinninja.bindings.DerivationPath;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(active = true)
public class Address {


    @Id(autoincrement = true)
    private Long id;

    @Unique
    @Property()
    private String address;

    @Property
    private Long walletId;

    @Property
    private int index;

    @Property
    private int changeIndex;

    @Keep
    public DerivationPath getDerivationPath() {
        return new DerivationPath(49, 0, 0, changeIndex, index);
    }

    @ToMany(referencedJoinProperty = "addressId")
    private List<FundingStat> funding;


    @ToMany(referencedJoinProperty = "addressId")
    private List<TargetStat> targets;

    @ToOne(joinProperty = "walletId")
    @Property
    private Wallet wallet;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1580986028)
    private transient AddressDao myDao;

    @Generated(hash = 393417580)
    public Address(Long id, String address, Long walletId, int index,
            int changeIndex) {
        this.id = id;
        this.address = address;
        this.walletId = walletId;
        this.index = index;
        this.changeIndex = changeIndex;
    }

    @Generated(hash = 388317431)
    public Address() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getChangeIndex() {
        return this.changeIndex;
    }

    public void setChangeIndex(int changeIndex) {
        this.changeIndex = changeIndex;
    }

    @Generated(hash = 1885063144)
    private transient Long wallet__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1903052073)
    public Wallet getWallet() {
        Long __key = this.walletId;
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
    @Generated(hash = 112459860)
    public void setWallet(Wallet wallet) {
        synchronized (this) {
            this.wallet = wallet;
            walletId = wallet == null ? null : wallet.getId();
            wallet__resolvedKey = walletId;
        }
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1472197772)
    public List<FundingStat> getFunding() {
        if (funding == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FundingStatDao targetDao = daoSession.getFundingStatDao();
            List<FundingStat> fundingNew = targetDao._queryAddress_Funding(id);
            synchronized (this) {
                if (funding == null) {
                    funding = fundingNew;
                }
            }
        }
        return funding;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1555041857)
    public synchronized void resetFunding() {
        funding = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1683165213)
    public List<TargetStat> getTargets() {
        if (targets == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TargetStatDao targetDao = daoSession.getTargetStatDao();
            List<TargetStat> targetsNew = targetDao._queryAddress_Targets(id);
            synchronized (this) {
                if (targets == null) {
                    targets = targetsNew;
                }
            }
        }
        return targets;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1141582004)
    public synchronized void resetTargets() {
        targets = null;
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
    @Generated(hash = 543375780)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAddressDao() : null;
    }
}
