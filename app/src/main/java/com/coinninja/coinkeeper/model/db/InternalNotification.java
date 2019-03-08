package com.coinninja.coinkeeper.model.db;

import android.net.Uri;

import com.coinninja.coinkeeper.model.db.converter.UriConverter;
import com.coinninja.coinkeeper.model.db.enums.InternalNotificationPriority;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(active = true)
public class InternalNotification {
    @Convert(converter = InternalNotificationPriority.PriorityConverter.class, columnType = Integer.class)
    private InternalNotificationPriority priority;

    @Unique
    @Id(autoincrement = true)
    private Long id;

    @Property()
    private String message;

    @Property
    boolean hasBeenSeen;

    @Convert(converter = MessageLevel.Converter.class, columnType = Integer.class)
    @Property()
    private MessageLevel messageLevel;

    @Property()
    private String serverUUID;

    @Convert(converter = UriConverter.class, columnType = String.class)
    @Property()
    private Uri clickAction;

    @ToOne(joinProperty = "walletId")
    @Property
    Wallet wallet;

    @Property
    Long walletId;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 693370399)
    private transient InternalNotificationDao myDao;

    @Generated(hash = 989134169)
    public InternalNotification(InternalNotificationPriority priority, Long id, String message,
            boolean hasBeenSeen, MessageLevel messageLevel, String serverUUID, Uri clickAction, Long walletId) {
        this.priority = priority;
        this.id = id;
        this.message = message;
        this.hasBeenSeen = hasBeenSeen;
        this.messageLevel = messageLevel;
        this.serverUUID = serverUUID;
        this.clickAction = clickAction;
        this.walletId = walletId;
    }

    @Generated(hash = 1531763480)
    public InternalNotification() {
    }

    public InternalNotificationPriority getPriority() {
        return this.priority;
    }

    public void setPriority(InternalNotificationPriority priority) {
        this.priority = priority;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getHasBeenSeen() {
        return this.hasBeenSeen;
    }

    public void setHasBeenSeen(boolean hasBeenSeen) {
        this.hasBeenSeen = hasBeenSeen;
    }

    public MessageLevel getMessageLevel() {
        return this.messageLevel;
    }

    public void setMessageLevel(MessageLevel messageLevel) {
        this.messageLevel = messageLevel;
    }

    public String getServerUUID() {
        return this.serverUUID;
    }

    public void setServerUUID(String serverUUID) {
        this.serverUUID = serverUUID;
    }

    public Uri getClickAction() {
        return this.clickAction;
    }

    public void setClickAction(Uri clickAction) {
        this.clickAction = clickAction;
    }

    public Long getWalletId() {
        return this.walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
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
    @Generated(hash = 306312836)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getInternalNotificationDao() : null;
    }


}
