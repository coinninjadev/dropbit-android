package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.converter.PhoneNumberConverter;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

@Entity(active = true)
public class Account {
    public enum Status {
        UNVERIFIED(0),
        PENDING_VERIFICATION(10),
        VERIFIED(100);

        private final int id;

        Status(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public void populateStatus(String value) {
        if (value.equals("verified") || value.equals("pending-verification"))
            status = Status.PENDING_VERIFICATION;
        else
            status = Status.UNVERIFIED;
    }

    @Id
    private Long id;

    @Property
    private Long walletId;

    @ToOne(joinProperty = "walletId")
    private Wallet wallet;

    @Property
    private String cnWalletId;

    @Property
    private String cnUserId;

    @Convert(converter = StatusConverter.class, columnType = Integer.class)
    private Status status;

    @Property
    private String phoneNumberHash;

    @Convert(converter = PhoneNumberConverter.class, columnType = String.class)
    @Property
    private PhoneNumber phoneNumber;

    @Property
    private long verification_ttl;

    @ToMany(referencedJoinProperty = "accountId")
    @Property
    private List<DropbitMeIdentity> identities;

    @Property
    private boolean isPrivate;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 335469827)
    private transient AccountDao myDao;


    @Generated(hash = 749345295)
    public Account(Long id, Long walletId, String cnWalletId, String cnUserId, Status status,
                   String phoneNumberHash, PhoneNumber phoneNumber, long verification_ttl,
                   boolean isPrivate) {
        this.id = id;
        this.walletId = walletId;
        this.cnWalletId = cnWalletId;
        this.cnUserId = cnUserId;
        this.status = status;
        this.phoneNumberHash = phoneNumberHash;
        this.phoneNumber = phoneNumber;
        this.verification_ttl = verification_ttl;
        this.isPrivate = isPrivate;
    }


    @Generated(hash = 882125521)
    public Account() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getWalletId() {
        return walletId;
    }


    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }


    public String getCnWalletId() {
        return cnWalletId;
    }


    public void setCnWalletId(String cnWalletId) {
        this.cnWalletId = cnWalletId;
    }


    public String getCnUserId() {
        return cnUserId;
    }


    public void setCnUserId(String cnUserId) {
        this.cnUserId = cnUserId;
    }


    public Status getStatus() {
        return status;
    }


    public void setStatus(Status status) {
        this.status = status;
    }


    public String getPhoneNumberHash() {
        return phoneNumberHash;
    }


    public void setPhoneNumberHash(String phoneNumberHash) {
        this.phoneNumberHash = phoneNumberHash;
    }

    public long getVerification_ttl() {
        return verification_ttl;
    }


    public void setVerification_ttl(long verification_ttl) {
        this.verification_ttl = verification_ttl;
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


    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 112459860)
    public void setWallet(Wallet wallet) {
        synchronized (this) {
            this.wallet = wallet;
            walletId = wallet == null ? null : wallet.getId();
            wallet__resolvedKey = walletId;
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


    public void setPhoneNumber(PhoneNumber phoneNumber) {
        this.phoneNumber = phoneNumber;
    }


    public PhoneNumber getPhoneNumber() {
        return phoneNumber;
    }


    public boolean getIsPrivate() {
        return isPrivate;
    }


    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }


    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 188162726)
    public List<DropbitMeIdentity> getIdentities() {
        if (identities == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            DropbitMeIdentityDao targetDao = daoSession.getDropbitMeIdentityDao();
            List<DropbitMeIdentity> identitiesNew = targetDao._queryAccount_Identities(id);
            synchronized (this) {
                if (identities == null) {
                    identities = identitiesNew;
                }
            }
        }
        return identities;
    }


    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 991003748)
    public synchronized void resetIdentities() {
        identities = null;
    }


    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1812283172)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAccountDao() : null;
    }


    public static class StatusConverter implements PropertyConverter<Status, Integer> {
        @Override
        public Status convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            for (Status status : Status.values()) {
                if (status.id == databaseValue) {
                    return status;
                }
            }

            return Status.UNVERIFIED;
        }

        @Override
        public Integer convertToDatabaseValue(Status status) {
            return status == null ? null : status.id;
        }
    }

}
