package com.coinninja.coinkeeper.model.db;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.converter.PropertyConverter;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(active = true)
public class FundingStat {
    public enum State {
        PENDING(0),
        ACKNOWLEDGE(1),
        CANCELED(2);

        private final int id;

        State(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public static class StateConverter implements PropertyConverter<FundingStat.State, Integer> {
        @Override
        public FundingStat.State convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            for (FundingStat.State state : FundingStat.State.values()) {
                if (state.id == databaseValue) {
                    return state;
                }
            }

            return FundingStat.State.PENDING;
        }

        @Override
        public Integer convertToDatabaseValue(FundingStat.State state) {
            return state == null ? null : state.id;
        }
    }

    @Id
    Long id;

    @Property
    Long addressId;

    @Property
    Long tsid;

    @Property
    Long walletId;

    @ToOne(joinProperty = "walletId")
    @Property
    Wallet wallet;

    @ToOne(joinProperty = "tsid")
    @Property
    TransactionSummary transaction;

    @ToOne(joinProperty = "addressId")
    @Property
    Address address;

    @Property()
    String addr;

    @Property
    int position;

    @Property
    long value;

    @Property
    Long targetId;

    @ToOne(joinProperty = "targetId")
    @Property
    TargetStat targetStat;

    @Property
    String fundedTransaction;

    @Convert(converter = FundingStat.StateConverter.class, columnType = Integer.class)
    @Property()
    private FundingStat.State state;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1529096706)
    private transient FundingStatDao myDao;

    @Generated(hash = 1983051231)
    public FundingStat(Long id, Long addressId, Long tsid, Long walletId, String addr, int position,
            long value, Long targetId, String fundedTransaction, FundingStat.State state) {
        this.id = id;
        this.addressId = addressId;
        this.tsid = tsid;
        this.walletId = walletId;
        this.addr = addr;
        this.position = position;
        this.value = value;
        this.targetId = targetId;
        this.fundedTransaction = fundedTransaction;
        this.state = state;
    }

    @Generated(hash = 1567276388)
    public FundingStat() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAddressId() {
        return this.addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getTsid() {
        return this.tsid;
    }

    public void setTsid(Long tsid) {
        this.tsid = tsid;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getAddr() {
        return this.addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Long getTargetId() {
        return this.targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getFundedTransaction() {
        return this.fundedTransaction;
    }

    public void setFundedTransaction(String fundedTransaction) {
        this.fundedTransaction = fundedTransaction;
    }

    public FundingStat.State getState() {
        return this.state;
    }

    public void setState(FundingStat.State state) {
        this.state = state;
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

    @Generated(hash = 1000999824)
    private transient Long transaction__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1332323283)
    public TransactionSummary getTransaction() {
        Long __key = this.tsid;
        if (transaction__resolvedKey == null || !transaction__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionSummaryDao targetDao = daoSession.getTransactionSummaryDao();
            TransactionSummary transactionNew = targetDao.load(__key);
            synchronized (this) {
                transaction = transactionNew;
                transaction__resolvedKey = __key;
            }
        }
        return transaction;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 796903244)
    public void setTransaction(TransactionSummary transaction) {
        synchronized (this) {
            this.transaction = transaction;
            tsid = transaction == null ? null : transaction.getId();
            transaction__resolvedKey = tsid;
        }
    }

    @Generated(hash = 1156467801)
    private transient Long address__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 489389972)
    public Address getAddress() {
        Long __key = this.addressId;
        if (address__resolvedKey == null || !address__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AddressDao targetDao = daoSession.getAddressDao();
            Address addressNew = targetDao.load(__key);
            synchronized (this) {
                address = addressNew;
                address__resolvedKey = __key;
            }
        }
        return address;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 607080948)
    public void setAddress(Address address) {
        synchronized (this) {
            this.address = address;
            addressId = address == null ? null : address.getId();
            address__resolvedKey = addressId;
        }
    }

    @Generated(hash = 163151411)
    private transient Long targetStat__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1113047321)
    public TargetStat getTargetStat() {
        Long __key = this.targetId;
        if (targetStat__resolvedKey == null || !targetStat__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TargetStatDao targetDao = daoSession.getTargetStatDao();
            TargetStat targetStatNew = targetDao.load(__key);
            synchronized (this) {
                targetStat = targetStatNew;
                targetStat__resolvedKey = __key;
            }
        }
        return targetStat;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 477228390)
    public void setTargetStat(TargetStat targetStat) {
        synchronized (this) {
            this.targetStat = targetStat;
            targetId = targetStat == null ? null : targetStat.getId();
            targetStat__resolvedKey = targetId;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 960344631)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFundingStatDao() : null;
    }
}
