package com.coinninja.coinkeeper.model.db;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

@Entity(active = true)
public class User {

    @Id(autoincrement = true)
    private Long id;

    @Property()
    private String pin;

    @Deprecated
    @Property()
    private String uid;

    @Property()
    private boolean completedTraining;

    @Property()
    private long lockedUntilTime;

    @Property
    @ToMany(referencedJoinProperty = "userId")
    private List<Wallet> wallets;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1507654846)
    private transient UserDao myDao;

    @Generated(hash = 594271489)
    public User(Long id, String pin, String uid, boolean completedTraining,
                long lockedUntilTime) {
        this.id = id;
        this.pin = pin;
        this.uid = uid;
        this.completedTraining = completedTraining;
        this.lockedUntilTime = lockedUntilTime;
    }

    @Generated(hash = 586692638)
    public User() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean getCompletedTraining() {
        return completedTraining;
    }

    public void setCompletedTraining(boolean completedTraining) {
        this.completedTraining = completedTraining;
    }

    public long getLockedUntilTime() {
        return lockedUntilTime;
    }

    public void setLockedUntilTime(long lockedUntilTime) {
        this.lockedUntilTime = lockedUntilTime;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 816700210)
    public List<Wallet> getWallets() {
        if (wallets == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            WalletDao targetDao = daoSession.getWalletDao();
            List<Wallet> walletsNew = targetDao._queryUser_Wallets(id);
            synchronized (this) {
                if (wallets == null) {
                    wallets = walletsNew;
                }
            }
        }
        return wallets;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1899338572)
    public synchronized void resetWallets() {
        wallets = null;
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
    @Generated(hash = 2059241980)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserDao() : null;
    }
}
