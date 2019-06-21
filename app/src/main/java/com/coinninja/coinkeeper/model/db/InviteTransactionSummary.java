package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.db.converter.BTCStateConverter;
import com.coinninja.coinkeeper.model.db.converter.TypeConverter;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

@Entity(active = true)
public class InviteTransactionSummary {
    Long toUserIdentityId;
    @ToOne(joinProperty = "toUserIdentityId")
    UserIdentity toUser;
    Long fromUserIdentityId;
    @ToOne(joinProperty = "fromUserIdentityId")
    UserIdentity fromUser;
    @Property
    private Long walletId;
    @ToOne(joinProperty = "walletId")
    @Property
    private Wallet wallet;
    @Property
    private Long transactionsInvitesSummaryID;
    @ToOne(joinProperty = "transactionsInvitesSummaryID")
    @Property
    private TransactionsInvitesSummary transactionsInvitesSummary;
    private Long transactionNotificationId;
    @ToOne(joinProperty = "transactionNotificationId")
    private TransactionNotification transactionNotification;
    @Id(autoincrement = true)
    private Long id;
    @Convert(converter = TypeConverter.class, columnType = Integer.class)
    @Property()
    private Type type;
    @Convert(converter = BTCStateConverter.class, columnType = Integer.class)
    @Property()
    private BTCState btcState;
    @Unique
    @Property()
    private String serverId;
    @Property()
    private String btcTransactionId;
    @Property()
    private Long sentDate;
    @Property()
    private String address;
    @Property()
    private String pubkey;
    @Property
    private Long valueSatoshis;
    @Property
    private Long valueFeesSatoshis;
    @Property
    private long historicValue;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 849284508)
    private transient InviteTransactionSummaryDao myDao;
    @Generated(hash = 1885063144)
    private transient Long wallet__resolvedKey;
    @Generated(hash = 1115183563)
    private transient Long transactionsInvitesSummary__resolvedKey;
    @Generated(hash = 562747265)
    private transient Long transactionNotification__resolvedKey;
    @Generated(hash = 1661025460)
    private transient Long toUser__resolvedKey;
    @Generated(hash = 468263943)
    private transient Long fromUser__resolvedKey;
    @Generated(hash = 1484232698)
    public InviteTransactionSummary(Long toUserIdentityId, Long fromUserIdentityId, Long walletId, Long transactionsInvitesSummaryID, Long transactionNotificationId, Long id, Type type, BTCState btcState,
            String serverId, String btcTransactionId, Long sentDate, String address, String pubkey, Long valueSatoshis, Long valueFeesSatoshis, long historicValue) {
        this.toUserIdentityId = toUserIdentityId;
        this.fromUserIdentityId = fromUserIdentityId;
        this.walletId = walletId;
        this.transactionsInvitesSummaryID = transactionsInvitesSummaryID;
        this.transactionNotificationId = transactionNotificationId;
        this.id = id;
        this.type = type;
        this.btcState = btcState;
        this.serverId = serverId;
        this.btcTransactionId = btcTransactionId;
        this.sentDate = sentDate;
        this.address = address;
        this.pubkey = pubkey;
        this.valueSatoshis = valueSatoshis;
        this.valueFeesSatoshis = valueFeesSatoshis;
        this.historicValue = historicValue;
    }

    @Generated(hash = 743506449)
    public InviteTransactionSummary() {
    }

    @Keep
    public String getLocaleFriendlyDisplayIdentityForReceiver() {
        if (getToUser() == null) return "";
        return toUser.getLocaleFriendlyDisplayIdentityText();
    }

    @Keep
    public String getLocaleFriendlyDisplayIdentityForSender() {
        if (getFromUser() == null) return "";
        return fromUser.getLocaleFriendlyDisplayIdentityText();
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Long getTransactionsInvitesSummaryID() {
        return transactionsInvitesSummaryID;
    }

    public void setTransactionsInvitesSummaryID(Long transactionsInvitesSummaryID) {
        this.transactionsInvitesSummaryID = transactionsInvitesSummaryID;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BTCState getBtcState() {
        return btcState;
    }

    public void setBtcState(BTCState btcState) {
        this.btcState = btcState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getBtcTransactionId() {
        return btcTransactionId;
    }

    @Keep
    public void setBtcTransactionId(String btcTransactionId) {
        setBbcTransactionIdProtector(btcTransactionId);
    }

    public Long getSentDate() {
        return sentDate;
    }

    public void setSentDate(Long sentDate) {
        this.sentDate = sentDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getValueSatoshis() {
        return valueSatoshis;
    }

    public void setValueSatoshis(Long valueSatoshis) {
        this.valueSatoshis = valueSatoshis;
    }

    public Long getValueFeesSatoshis() {
        return valueFeesSatoshis;
    }

    public void setValueFeesSatoshis(Long valueFeesSatoshis) {
        this.valueFeesSatoshis = valueFeesSatoshis;
    }

    public long getHistoricValue() {
        return historicValue;
    }

    public void setHistoricValue(long historicValue) {
        this.historicValue = historicValue;
    }

    public Long getTransactionNotificationId() {
        return transactionNotificationId;
    }

    public void setTransactionNotificationId(Long transactionNotificationId) {
        this.transactionNotificationId = transactionNotificationId;
    }

    /**
     * To-one relationship, resolved on first access.
     */
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
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1465356328)
    public TransactionsInvitesSummary getTransactionsInvitesSummary() {
        Long __key = this.transactionsInvitesSummaryID;
        if (transactionsInvitesSummary__resolvedKey == null || !transactionsInvitesSummary__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionsInvitesSummaryDao targetDao = daoSession.getTransactionsInvitesSummaryDao();
            TransactionsInvitesSummary transactionsInvitesSummaryNew = targetDao.load(__key);
            synchronized (this) {
                transactionsInvitesSummary = transactionsInvitesSummaryNew;
                transactionsInvitesSummary__resolvedKey = __key;
            }
        }
        return transactionsInvitesSummary;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 386011818)
    public void setTransactionsInvitesSummary(TransactionsInvitesSummary transactionsInvitesSummary) {
        synchronized (this) {
            this.transactionsInvitesSummary = transactionsInvitesSummary;
            transactionsInvitesSummaryID = transactionsInvitesSummary == null ? null : transactionsInvitesSummary.getId();
            transactionsInvitesSummary__resolvedKey = transactionsInvitesSummaryID;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 761085204)
    public TransactionNotification getTransactionNotification() {
        Long __key = this.transactionNotificationId;
        if (transactionNotification__resolvedKey == null || !transactionNotification__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TransactionNotificationDao targetDao = daoSession.getTransactionNotificationDao();
            TransactionNotification transactionNotificationNew = targetDao.load(__key);
            synchronized (this) {
                transactionNotification = transactionNotificationNew;
                transactionNotification__resolvedKey = __key;
            }
        }
        return transactionNotification;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 144706267)
    public void setTransactionNotification(TransactionNotification transactionNotification) {
        synchronized (this) {
            this.transactionNotification = transactionNotification;
            transactionNotificationId = transactionNotification == null ? null : transactionNotification.getId();
            transactionNotification__resolvedKey = transactionNotificationId;
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

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public Long getToUserIdentityId() {
        return toUserIdentityId;
    }

    public void setToUserIdentityId(Long toUserIdentityId) {
        this.toUserIdentityId = toUserIdentityId;
    }

    public Long getFromUserIdentityId() {
        return fromUserIdentityId;
    }

    public void setFromUserIdentityId(Long fromUserIdentityId) {
        this.fromUserIdentityId = fromUserIdentityId;
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1597379521)
    public UserIdentity getToUser() {
        Long __key = this.toUserIdentityId;
        if (toUser__resolvedKey == null || !toUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserIdentityDao targetDao = daoSession.getUserIdentityDao();
            UserIdentity toUserNew = targetDao.load(__key);
            synchronized (this) {
                toUser = toUserNew;
                toUser__resolvedKey = __key;
            }
        }
        return toUser;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1187746829)
    public void setToUser(UserIdentity toUser) {
        synchronized (this) {
            this.toUser = toUser;
            toUserIdentityId = toUser == null ? null : toUser.getId();
            toUser__resolvedKey = toUserIdentityId;
        }
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1726127259)
    public UserIdentity getFromUser() {
        Long __key = this.fromUserIdentityId;
        if (fromUser__resolvedKey == null || !fromUser__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            UserIdentityDao targetDao = daoSession.getUserIdentityDao();
            UserIdentity fromUserNew = targetDao.load(__key);
            synchronized (this) {
                fromUser = fromUserNew;
                fromUser__resolvedKey = __key;
            }
        }
        return fromUser;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 938191438)
    public void setFromUser(UserIdentity fromUser) {
        synchronized (this) {
            this.fromUser = fromUser;
            fromUserIdentityId = fromUser == null ? null : fromUser.getId();
            fromUser__resolvedKey = fromUserIdentityId;
        }
    }

    //do not delete this method
    @Keep
    protected void setTxId(String btcTransactionId) {
        this.btcTransactionId = btcTransactionId;
    }

    @Keep
    private void setBbcTransactionIdProtector(String btcTransactionId) {
        if (btcTransactionId == null) return;
        if (btcTransactionId.isEmpty()) return;

        setTxId(btcTransactionId);//calling in different makes it easier to mock out and touch with unittest
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1922106514)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getInviteTransactionSummaryDao() : null;
    }
}
