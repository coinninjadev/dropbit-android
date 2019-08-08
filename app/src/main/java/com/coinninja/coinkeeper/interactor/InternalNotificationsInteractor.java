package com.coinninja.coinkeeper.interactor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.notifications.InternalNotificationView;

import javax.inject.Inject;

import static com.coinninja.coinkeeper.util.DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE;


public class InternalNotificationsInteractor {
    private final LocalBroadCastUtil localBroadCastUtil;
    private final InternalNotificationHelper internalNotificationHelper;
    IntentFilter filter = new IntentFilter(ACTION_INTERNAL_NOTIFICATION_UPDATE);
    private BaseActivity activity;
    private ViewGroup baseLayout;
    private InternalNotificationView notificationView;
    private boolean notificationsMuteBackground;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_INTERNAL_NOTIFICATION_UPDATE.equals(intent.getAction())) {
                showPendingInternalNotifications();
            }
        }
    };

    @Inject
    public InternalNotificationsInteractor(@NonNull InternalNotificationHelper internalNotificationHelper, @NonNull LocalBroadCastUtil localBroadCastUtil) {
        this.internalNotificationHelper = internalNotificationHelper;
        this.localBroadCastUtil = localBroadCastUtil;
    }

    /**
     * call before any super.()
     */
    public void startListeningForNotifications(BaseActivity activity, boolean notificationsMuteBackground) {
        this.activity = activity;
        setMute(notificationsMuteBackground);
        baseLayout = activity.findViewById(R.id.message_queue);
        localBroadCastUtil.registerReceiver(receiver, filter);
        setNotificationView(new InternalNotificationView(baseLayout));
    }

    /**
     * call before any super.()
     */
    public void stopListeningForNotifications() {
        notificationsMuteBackground = false;
        unregisterNotificationReceiver();
        activity = null;
        clean();
    }

    void onUserDismissed(InternalNotification notificationDbObject) {
        removeNotificationFromDatabase(notificationDbObject);
        InternalNotification nextNotification = getNextNotification();
        if (nextNotification == null || notificationView == null) {
            notificationsDone();
            notificationView = null;
            return;
        }

        notificationView.show(nextNotification);
    }

    void setNotificationView(InternalNotificationView notificationView) {
        this.notificationView = notificationView;
    }

    private void clean() {
        if (notificationView != null) {
            notificationView.unNaturallyDismiss();
            notificationView = null;
        }
    }

    private void showPendingInternalNotifications() {
        if (!isRegistered()) {
            notificationsDone();
            return;
        }


        InternalNotification nextNotification = getNextNotification();
        if (nextNotification != null) {
            removeAnyShowingNotifications();
            show(nextNotification);
        } else {
            notificationsDone();
        }
    }

    private InternalNotification getNextNotification() {
        return getNotificationsByPriority();
    }

    private InternalNotification getNotificationsByPriority() {
        return internalNotificationHelper.getNextUnseenNotification();
    }

    private boolean isRegistered() {
        return baseLayout != null && notificationView != null;
    }

    private void notificationsDone() {
        if (notificationsMuteBackground) {
            activity.teardownMute();
        }
    }

    private void removeAnyShowingNotifications() {
        View currentShowing = notificationView.getCurrentNotificationView();
        if (currentShowing != null) {
            notificationView.removeView(currentShowing);
        }
    }

    private void removeNotificationFromDatabase(InternalNotification notificationDbObject) {
        internalNotificationHelper.setHasBeenSeen(notificationDbObject);
    }

    private void setMute(boolean notificationsMuteBackground) {
        this.notificationsMuteBackground = notificationsMuteBackground;
    }

    private void show(InternalNotification nextNotification) {
        if (notificationsMuteBackground) {
            activity.muteViews();
        }

        notificationView.setDismissListener(this::onUserDismissed);
        notificationView.show(nextNotification);
    }

    private void unregisterNotificationReceiver() {
        localBroadCastUtil.unregisterReceiver(receiver);
    }

}
