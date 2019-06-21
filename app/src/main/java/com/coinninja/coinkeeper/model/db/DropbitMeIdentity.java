package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.converter.AccountStatusConverter;
import com.coinninja.coinkeeper.model.db.converter.IdentityTypeConverter;
import com.coinninja.coinkeeper.model.db.enums.AccountStatus;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

@Entity(active = true)
public class DropbitMeIdentity {
    @Id
    private Long id;

    @Property
    @Convert(converter = IdentityTypeConverter.class, columnType = Integer.class)
    private IdentityType type;

    @Property
    private String identity;

    @Unique
    @Property
    private String serverId;

    @Property
    private String handle;

    @Property
    private String hash;

    @Convert(converter = AccountStatusConverter.class, columnType = Integer.class)
    private AccountStatus status;

    @Property
    private Long accountId;

    @ToOne(joinProperty = "accountId")
    private Account account;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1708066102)
    private transient DropbitMeIdentityDao myDao;


    @Generated(hash = 285794710)
    public DropbitMeIdentity() {
    }

    @Generated(hash = 288467611)
    public DropbitMeIdentity(Long id, IdentityType type, String identity, String serverId,
            String handle, String hash, AccountStatus status, Long accountId) {
        this.id = id;
        this.type = type;
        this.identity = identity;
        this.serverId = serverId;
        this.handle = handle;
        this.hash = hash;
        this.status = status;
        this.accountId = accountId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Generated(hash = 1501133588)
    private transient Long account__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 531730087)
    public Account getAccount() {
        Long __key = this.accountId;
        if (account__resolvedKey == null || !account__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AccountDao targetDao = daoSession.getAccountDao();
            Account accountNew = targetDao.load(__key);
            synchronized (this) {
                account = accountNew;
                account__resolvedKey = __key;
            }
        }
        return account;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1910176546)
    public void setAccount(Account account) {
        synchronized (this) {
            this.account = account;
            accountId = account == null ? null : account.getId();
            account__resolvedKey = accountId;
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

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 124065835)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getDropbitMeIdentityDao() : null;
    }

}
