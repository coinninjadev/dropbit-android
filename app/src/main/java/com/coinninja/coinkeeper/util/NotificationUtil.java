package com.coinninja.coinkeeper.util;

import android.content.Context;
import android.net.Uri;

import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.db.enums.InternalNotificationPriority;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import javax.inject.Inject;

public class NotificationUtil {

    private Context context;
    private final InternalNotificationHelper internalNotificationHelper;
    private final LocalBroadCastUtil localBroadCastUtil;

    @Inject
    public NotificationUtil(@ApplicationContext Context context, InternalNotificationHelper internalNotificationHelper, LocalBroadCastUtil localBroadCastUtil) {
        this.context = context;
        this.internalNotificationHelper = internalNotificationHelper;
        this.localBroadCastUtil = localBroadCastUtil;
    }

    public void dispatchInternal(int stringResId) {
        dispatchInternal(context.getString(stringResId));
    }

    public void dispatchInternal(String message) {
        internalNotificationHelper.addNotifications(message);
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE);
    }

    public void dispatchInternalError(String message) {
        dispatchInternalError(message, null);
    }

    public void dispatchInternalError(String message, Uri clickAction) {
        internalNotificationHelper.addNotifications(InternalNotificationPriority._0_HIGHEST, clickAction, message, MessageLevel.ERROR);
        localBroadCastUtil.sendBroadcast(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE);
    }
}
