package com.coinninja.coinkeeper.model.helpers;

import android.net.Uri;

import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.db.InternalNotificationDao;
import com.coinninja.coinkeeper.model.db.enums.InternalNotificationPriority;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;

import org.greenrobot.greendao.query.QueryBuilder;

import javax.inject.Inject;

public class InternalNotificationHelper {

    private final DaoSessionManager daoSessionManager;
    private final WalletHelper walletHelper;

    @Inject
    public InternalNotificationHelper(DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
    }

    public void addNotifications(String message) {
        addNotifications(InternalNotificationPriority._10_LOWEST, message, MessageLevel.INFO);
    }

    public void addNotifications(InternalNotificationPriority priority, String message, MessageLevel messageLevel) {
        addNotifications(null, null, priority, message, messageLevel);
    }

    public void addNotifications(InternalNotificationPriority priority, Uri clickAction, String message, MessageLevel messageLevel) {
        addNotifications(null, clickAction, priority, message, messageLevel);
    }

    public void addNotifications(String UUID, Uri clickAction, InternalNotificationPriority priority, String message, MessageLevel level) {
        InternalNotificationDao notificationDao = daoSessionManager.getInternalNotificationDao();

        InternalNotification notification = null;
        if (UUID != null && !UUID.isEmpty()) {
            notification = notificationDao.queryBuilder().
                    where(InternalNotificationDao.Properties.ServerUUID.eq(UUID)).
                    limit(1).unique();
        }


        if (notification == null) {
            notification = new InternalNotification();
            notificationDao.insert(notification);
            notification.setServerUUID(UUID);
            notification.setHasBeenSeen(false);
        }

        notification.setWallet(walletHelper.getPrimaryWallet());
        notification.setMessage(message);
        notification.setPriority(priority);
        notification.setMessageLevel(level);
        notification.setClickAction(clickAction);

        notification.update();
        notification.refresh();

    }

    public InternalNotification getNextUnseenNotification() {
        QueryBuilder queryMaster = daoSessionManager.getInternalNotificationDao().queryBuilder()
                .where(InternalNotificationDao.Properties.HasBeenSeen.eq(false));

        //priority field (0-10; 0 is high priority and 10 is low)
        QueryBuilder queryAscPriority = queryMaster.orderAsc(InternalNotificationDao.Properties.Priority);

        return (InternalNotification) queryAscPriority.limit(1).unique();
    }

    public void setHasBeenSeen(InternalNotification notificationDbObject) {
        InternalNotificationDao notificationDao = daoSessionManager.getInternalNotificationDao();

        InternalNotification notification = notificationDao.queryBuilder().
                where(InternalNotificationDao.Properties.Id.eq(notificationDbObject.getId())).
                limit(1).unique();


        if (notification == null) {
            return;
        }

        notification.setHasBeenSeen(true);
        notification.update();
        notification.refresh();

    }
}
