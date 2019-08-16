package com.coinninja.coinkeeper.model.db;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

@Entity(active = true)
public class Wallet {

    @Id(autoincrement = true)
    private Long id;

    @ToMany(referencedJoinProperty = "walletId")
    @Property
    private List<Word> words;

    @Property
    private int hdIndex;

    @Property
    private Long userId;

    @Property
    private long lastSync;

    @Property
    private int internalIndex;

    @Property
    private int externalIndex;

    @ToMany(referencedJoinProperty = "walletId")
    @Property
    private List<FundingStat> fundingStats;

    @ToMany(referencedJoinProperty = "walletId")
    @Property
    private List<InviteTransactionSummary> inviteTransactionSummaries;

    @ToMany(referencedJoinProperty = "walletId")
    @Property
    private List<TargetStat> targetStats;

    @ToMany(referencedJoinProperty = "walletId")
    @Property
    private List<Address> addressses;

    @ToMany(referencedJoinProperty = "walletId")
    @Property
    private List<TransactionSummary> transactions;

    @Property
    private long balance;

    @Property
    private long spendableBalance;

    @Property
    private int blockTip;

    @ToMany(referencedJoinProperty = "id")
    private List<Account> accounts;

    @Property
    private long lastUSDPrice;

    @Property
    private int purpose;

    @Property
    private int coinType;

    @Property
    private int accountIndex;


    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;


    /**
     * Used for active entity operations.
     */
    @Generated(hash = 741381941)
    private transient WalletDao myDao;

    public Wallet(Long id, int hdIndex, Long userId, long lastSync,
                  int internalIndex, int externalIndex, long balance,
                  long spendableBalance, int blockTip, long lastUSDPrice,
                  String lastFee) {
        this.id = id;
        this.hdIndex = hdIndex;
        this.userId = userId;
        this.lastSync = lastSync;
        this.internalIndex = internalIndex;
        this.externalIndex = externalIndex;
        this.balance = balance;
        this.spendableBalance = spendableBalance;
        this.blockTip = blockTip;
        this.lastUSDPrice = lastUSDPrice;
    }

    @Generated(hash = 1197745249)
    public Wallet() {
    }

    @Generated(hash = 1456873090)
    public Wallet(Long id, int hdIndex, Long userId, long lastSync, int internalIndex,
            int externalIndex, long balance, long spendableBalance, int blockTip, long lastUSDPrice,
            int purpose, int coinType, int accountIndex) {
        this.id = id;
        this.hdIndex = hdIndex;
        this.userId = userId;
        this.lastSync = lastSync;
        this.internalIndex = internalIndex;
        this.externalIndex = externalIndex;
        this.balance = balance;
        this.spendableBalance = spendableBalance;
        this.blockTip = blockTip;
        this.lastUSDPrice = lastUSDPrice;
        this.purpose = purpose;
        this.coinType = coinType;
        this.accountIndex = accountIndex;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getHdIndex() {
        return hdIndex;
    }

    public void setHdIndex(int hdIndex) {
        this.hdIndex = hdIndex;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public long getLastSync() {
        return lastSync;
    }

    public void setLastSync(long lastSync) {
        this.lastSync = lastSync;
    }

    public int getInternalIndex() {
        return internalIndex;
    }

    public void setInternalIndex(int internalIndex) {
        this.internalIndex = internalIndex;
    }

    public int getExternalIndex() {
        return externalIndex;
    }

    public void setExternalIndex(int externalIndex) {
        this.externalIndex = externalIndex;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public long getSpendableBalance() {
        return spendableBalance;
    }

    public void setSpendableBalance(long spendableBalance) {
        this.spendableBalance = spendableBalance;
    }

    public int getBlockTip() {
        return blockTip;
    }

    public void setBlockTip(int blockTip) {
        this.blockTip = blockTip;
    }

    public long getLastUSDPrice() {
        return lastUSDPrice;
    }

    public void setLastUSDPrice(long lastUSDPrice) {
        this.lastUSDPrice = lastUSDPrice;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 747989095)
    public List<Word> getWords() {
        if (words == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            WordDao targetDao = daoSession.getWordDao();
            List<Word> wordsNew = targetDao._queryWallet_Words(id);
            synchronized (this) {
                if (words == null) {
                    words = wordsNew;
                }
            }
        }
        return words;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1954400333)
    public synchronized void resetWords() {
        words = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 339208949)
    public List<FundingStat> getFundingStats() {
        if (fundingStats == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FundingStatDao targetDao = daoSession.getFundingStatDao();
            List<FundingStat> fundingStatsNew = targetDao._queryWallet_FundingStats(id);
            synchronized (this) {
                if (fundingStats == null) {
                    fundingStats = fundingStatsNew;
                }
            }
        }
        return fundingStats;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 242399275)
    public synchronized void resetFundingStats() {
        fundingStats = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 159094658)
    public List<InviteTransactionSummary> getInviteTransactionSummaries() {
        if (inviteTransactionSummaries == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            InviteTransactionSummaryDao targetDao = daoSession.getInviteTransactionSummaryDao();
            List<InviteTransactionSummary> inviteTransactionSummariesNew = targetDao
                    ._queryWallet_InviteTransactionSummaries(id);
            synchronized (this) {
                if (inviteTransactionSummaries == null) {
                    inviteTransactionSummaries = inviteTransactionSummariesNew;
                }
            }
        }
        return inviteTransactionSummaries;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 159423227)
    public synchronized void resetInviteTransactionSummaries() {
        inviteTransactionSummaries = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1703366100)
    public List<TargetStat> getTargetStats() {
        if (targetStats == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TargetStatDao targetDao = daoSession.getTargetStatDao();
            List<TargetStat> targetStatsNew = targetDao._queryWallet_TargetStats(id);
            synchronized (this) {
                if (targetStats == null) {
                    targetStats = targetStatsNew;
                }
            }
        }
        return targetStats;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1351823693)
    public synchronized void resetTargetStats() {
        targetStats = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1079747734)
    public List<Address> getAddressses() {
        if (addressses == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AddressDao targetDao = daoSession.getAddressDao();
            List<Address> addresssesNew = targetDao._queryWallet_Addressses(id);
            synchronized (this) {
                if (addressses == null) {
                    addressses = addresssesNew;
                }
            }
        }
        return addressses;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 621404480)
    public synchronized void resetAddressses() {
        addressses = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 564437246)
    public List<TransactionSummary> getTransactions() {
        if (transactions == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionSummaryDao targetDao = daoSession.getTransactionSummaryDao();
            List<TransactionSummary> transactionsNew = targetDao._queryWallet_Transactions(id);
            synchronized (this) {
                if (transactions == null) {
                    transactions = transactionsNew;
                }
            }
        }
        return transactions;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1751056821)
    public synchronized void resetTransactions() {
        transactions = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1985763940)
    public List<Account> getAccounts() {
        if (accounts == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AccountDao targetDao = daoSession.getAccountDao();
            List<Account> accountsNew = targetDao._queryWallet_Accounts(id);
            synchronized (this) {
                if (accounts == null) {
                    accounts = accountsNew;
                }
            }
        }
        return accounts;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 121514453)
    public synchronized void resetAccounts() {
        accounts = null;
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

    public int getPurpose() {
        return this.purpose;
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
    }

    public int getCoinType() {
        return this.coinType;
    }

    public void setCoinType(int coinType) {
        this.coinType = coinType;
    }

    public int getAccountIndex() {
        return this.accountIndex;
    }

    public void setAccountIndex(int accountIndex) {
        this.accountIndex = accountIndex;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 657468544)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getWalletDao() : null;
    }

}
