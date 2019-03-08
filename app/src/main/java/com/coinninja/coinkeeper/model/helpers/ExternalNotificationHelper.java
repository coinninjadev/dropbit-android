package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.ExternalNotification;
import com.coinninja.coinkeeper.model.db.ExternalNotificationDao;

import java.util.List;

import javax.inject.Inject;

public class ExternalNotificationHelper {


    private final DaoSessionManager daoSessionManager;

    @Inject
    public ExternalNotificationHelper(DaoSessionManager daoSessionManager) {
        this.daoSessionManager = daoSessionManager;
    }

    public void saveNotification(String message, String txID) {
        ExternalNotificationDao externalNotificationDao = daoSessionManager.getExternalNotificationDao();


        ExternalNotification externalNotification = externalNotificationDao.queryBuilder().
                where(ExternalNotificationDao.Properties.Message.eq(message)).
                limit(1).unique();

        if (externalNotification == null) {
            externalNotification = new ExternalNotification();
            externalNotificationDao.insert(externalNotification);
        }


        externalNotification.setMessage(message);
        externalNotification.setExtraData(txID);


        externalNotification.update();
        externalNotification.refresh();
        externalNotificationDao.refresh(externalNotification);
    }


    public List<ExternalNotification> getNotifications() {
        ExternalNotificationDao externalNotificationDao = daoSessionManager.getExternalNotificationDao();
        return externalNotificationDao.queryBuilder().list();
    }

    public void removeNotification(ExternalNotification remove) {
        ExternalNotificationDao externalNotificationDao = daoSessionManager.getExternalNotificationDao();

        ExternalNotification notification = externalNotificationDao.queryBuilder().
                where(ExternalNotificationDao.Properties.ExtraData.eq(remove.getExtraData())).
                limit(1).unique();

        if (notification == null) {
            return;
        }

        externalNotificationDao.delete(notification);
    }
}
