package com.coinninja.coinkeeper.model.db;


import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.converter.IdentityTypeConverter;
import com.coinninja.coinkeeper.model.db.enums.IdentityType;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Unique;

@Entity(active = true)
public class UserIdentity {
    @Id
    private Long id;
    @Unique
    @Property
    private String identity;
    @Property
    @Convert(converter = IdentityTypeConverter.class, columnType = Integer.class)
    private IdentityType type;
    @Property
    private String displayName;
    @Property
    private String handle;
    @Property
    private String hash;
    // base64 image bytes
    @Property
    private String avatar;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 2041765723)
    private transient UserIdentityDao myDao;

    @Generated(hash = 1237797216)
    public UserIdentity(Long id, String identity, IdentityType type, String displayName,
                        String handle, String hash, String avatar) {
        this.id = id;
        this.identity = identity;
        this.type = type;
        this.displayName = displayName;
        this.handle = handle;
        this.hash = hash;
        this.avatar = avatar;
    }

    @Generated(hash = 1375924299)
    public UserIdentity() {
    }

    @Keep
    public String getLocaleFriendlyDisplayIdentityText() {
        if (getType() == IdentityType.TWITTER) {
            return String.format("@%s", handle);
        } else if (getDisplayName() != null && !getDisplayName().isEmpty())
            return getDisplayName();
        else
            return new PhoneNumber(getIdentity()).displayTextForLocale();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public IdentityType getType() {
        return type;
    }

    public void setType(IdentityType type) {
        this.type = type;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 60106747)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getUserIdentityDao() : null;
    }
}

