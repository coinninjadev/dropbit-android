package com.coinninja.coinkeeper.model.db;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.converter.BTCStateConverter;
import com.coinninja.coinkeeper.model.db.converter.PhoneNumberConverter;
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

    @Property
    Long walletId;
    @ToOne(joinProperty = "walletId")
    @Property
    Wallet wallet;
    @Property
    Long transactionsInvitesSummaryID;
    @ToOne(joinProperty = "transactionsInvitesSummaryID")
    @Property
    TransactionsInvitesSummary transactionsInvitesSummary;
    @Convert(converter = TypeConverter.class, columnType = Integer.class)
    @Property()
    private Type type;
    @Convert(converter = BTCStateConverter.class, columnType = Integer.class)
    @Property()
    private BTCState btcState;
    @Id(autoincrement = true)
    private Long id;
    @Unique
    @Property()
    private String serverId;
    @Property()
    private String inviteName;
    @Property()
    private String btcTransactionId;
    @Property()
    private Long sentDate;

    @Convert(converter = PhoneNumberConverter.class, columnType = String.class)
    @Property
    private PhoneNumber SenderPhoneNumber;

    @Convert(converter = PhoneNumberConverter.class, columnType = String.class)
    @Property
    private PhoneNumber ReceiverPhoneNumber;

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
    //TODO: make long
    @Property
    private String historicUSDValue;

    Long transactionNotificationId;
    @ToOne(joinProperty = "transactionNotificationId")
    TransactionNotification transactionNotification;
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

    @Generated(hash = 960547814)
    public InviteTransactionSummary(Long walletId, Long transactionsInvitesSummaryID, Type type, BTCState btcState, Long id, String serverId, String inviteName,
                                    String btcTransactionId, Long sentDate, PhoneNumber SenderPhoneNumber, PhoneNumber ReceiverPhoneNumber, String address, String pubkey,
                                    Long valueSatoshis, Long valueFeesSatoshis, long historicValue, String historicUSDValue, Long transactionNotificationId) {
        this.walletId = walletId;
        this.transactionsInvitesSummaryID = transactionsInvitesSummaryID;
        this.type = type;
        this.btcState = btcState;
        this.id = id;
        this.serverId = serverId;
        this.inviteName = inviteName;
        this.btcTransactionId = btcTransactionId;
        this.sentDate = sentDate;
        this.SenderPhoneNumber = SenderPhoneNumber;
        this.ReceiverPhoneNumber = ReceiverPhoneNumber;
        this.address = address;
        this.pubkey = pubkey;
        this.valueSatoshis = valueSatoshis;
        this.valueFeesSatoshis = valueFeesSatoshis;
        this.historicValue = historicValue;
        this.historicUSDValue = historicUSDValue;
        this.transactionNotificationId = transactionNotificationId;
    }

    @Generated(hash = 743506449)
    public InviteTransactionSummary() {
    }

    @Generated(hash = 1885063144)
    private transient Long wallet__resolvedKey;
    @Generated(hash = 1115183563)
    private transient Long transactionsInvitesSummary__resolvedKey;
    @Generated(hash = 562747265)
    private transient Long transactionNotification__resolvedKey;

    @Keep
    public void setBtcTransactionId(String btcTransactionId) {
        setBbcTransactionIdProtector(btcTransactionId);
    }

    @Keep
    private void setBbcTransactionIdProtector(String btcTransactionId) {
        if (btcTransactionId == null) return;
        if (btcTransactionId.isEmpty()) return;

        setTxId(btcTransactionId);//calling in different makes it easier to mock out and touch with unittest
    }

    //do not delete this method!!
    @Keep
    protected void setTxId(String btcTransactionId) {
        this.btcTransactionId = btcTransactionId;
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

    public String getInviteName() {
        return inviteName;
    }

    public void setInviteName(String inviteName) {
        this.inviteName = inviteName;
    }

    public String getBtcTransactionId() {
        return btcTransactionId;
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

    public String getHistoricUSDValue() {
        return historicUSDValue;
    }

    public void setHistoricUSDValue(String historicUSDValue) {
        this.historicUSDValue = historicUSDValue;
    }

    public Long getTransactionNotificationId() {
        return transactionNotificationId;
    }

    public void setTransactionNotificationId(Long transactionNotificationId) {
        this.transactionNotificationId = transactionNotificationId;
    }

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

    /** To-one relationship, resolved on first access. */
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

    /** To-one relationship, resolved on first access. */
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

    public PhoneNumber getSenderPhoneNumber() {
        return SenderPhoneNumber;
    }


    public void setReceiverPhoneNumber(PhoneNumber ReceiverPhoneNumber) {
        this.ReceiverPhoneNumber = ReceiverPhoneNumber;
    }

    public void setSenderPhoneNumber(PhoneNumber SenderPhoneNumber) {
        this.SenderPhoneNumber = SenderPhoneNumber;
    }

    public PhoneNumber getReceiverPhoneNumber() {
        return ReceiverPhoneNumber;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1922106514)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getInviteTransactionSummaryDao() : null;
    }
}
