package com.coinninja.coinkeeper.model.db;

import com.coinninja.bindings.UnspentTransactionOutput;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.converter.PropertyConverter;

@Entity(active = true)
public class TargetStat {

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

    public static class StateConverter implements PropertyConverter<TargetStat.State, Integer> {
        @Override
        public TargetStat.State convertToEntityProperty(Integer databaseValue) {
            if (databaseValue == null) {
                return null;
            }

            for (TargetStat.State state : TargetStat.State.values()) {
                if (state.id == databaseValue) {
                    return state;
                }
            }

            return TargetStat.State.PENDING;
        }

        @Override
        public Integer convertToDatabaseValue(TargetStat.State state) {
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
    long txTime;

    @Property
    Long fundingId;

    @ToOne(joinProperty = "fundingId")
    @Property
    FundingStat fundingStat;

    @Convert(converter = TargetStat.StateConverter.class, columnType = Integer.class)
    @Property()
    private TargetStat.State state;

    @Keep
    public UnspentTransactionOutput toUnspentTranasactionOutput() {
        TransactionSummary transaction = getTransaction();
        Address address = getAddress();
        return new UnspentTransactionOutput(
                transaction.getTxid(),
                getPosition(),
                getValue(),
                address.getDerivationPath(),
                transaction.isReplaceable()
        );
    }

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 994360634)
    private transient TargetStatDao myDao;

    @Generated(hash = 788946611)
    public TargetStat(Long id, Long addressId, Long tsid, Long walletId, String addr, int position,
            long value, long txTime, Long fundingId, TargetStat.State state) {
        this.id = id;
        this.addressId = addressId;
        this.tsid = tsid;
        this.walletId = walletId;
        this.addr = addr;
        this.position = position;
        this.value = value;
        this.txTime = txTime;
        this.fundingId = fundingId;
        this.state = state;
    }

    @Generated(hash = 427742802)
    public TargetStat() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getTsid() {
        return tsid;
    }

    public void setTsid(Long tsid) {
        this.tsid = tsid;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long getTxTime() {
        return txTime;
    }

    public void setTxTime(long txTime) {
        this.txTime = txTime;
    }

    public Long getFundingId() {
        return fundingId;
    }

    public void setFundingId(Long fundingId) {
        this.fundingId = fundingId;
    }

    public TargetStat.State getState() {
        return state;
    }

    public void setState(TargetStat.State state) {
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

    @Generated(hash = 172504962)
    private transient Long fundingStat__resolvedKey;

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 322566844)
    public FundingStat getFundingStat() {
        Long __key = this.fundingId;
        if (fundingStat__resolvedKey == null || !fundingStat__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FundingStatDao targetDao = daoSession.getFundingStatDao();
            FundingStat fundingStatNew = targetDao.load(__key);
            synchronized (this) {
                fundingStat = fundingStatNew;
                fundingStat__resolvedKey = __key;
            }
        }
        return fundingStat;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1388994558)
    public void setFundingStat(FundingStat fundingStat) {
        synchronized (this) {
            this.fundingStat = fundingStat;
            fundingId = fundingStat == null ? null : fundingStat.getId();
            fundingStat__resolvedKey = fundingId;
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
    @Generated(hash = 2042836476)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTargetStatDao() : null;
    }
}
