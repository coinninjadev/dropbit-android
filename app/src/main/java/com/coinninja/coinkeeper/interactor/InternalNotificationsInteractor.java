package com.coinninja.coinkeeper.interactor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.base.MessagegerActivity;
import com.coinninja.coinkeeper.view.notifications.InternalNotificationView;

import static com.coinninja.coinkeeper.util.Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE;


public class InternalNotificationsInteractor {
    private final MessagegerActivity activity;
    private final CoinKeeperApplication application;
    private final LocalBroadCastUtil localBroadCastUtil;
    private Receiver receiver;

    private ViewGroup baseLayout;

    private InternalNotificationView notificationView;
    private boolean notificationsMuteBackground;
    private InternalNotificationHelper notificationHelper;

    public InternalNotificationsInteractor(MessagegerActivity activity, CoinKeeperApplication application, LocalBroadCastUtil localBroadCastUtil) {
        this.activity = activity;
        this.application = application;
        this.localBroadCastUtil = localBroadCastUtil;
    }

    /**
     * call before any super.()
     */
    public void startListeningForNotifications(boolean notificationsMuteBackground) {
        setMute(notificationsMuteBackground);
        setReceiver(new Receiver());

        buildDependencies();
        registerNotificationReceiver();
    }

    private void buildDependencies() {
        ViewGroup queue = activity.findViewById(R.id.message_queue);
        setBaseLayout(queue);
        setDaoHelper(application.getInternalNotificationHelper());
        setNotificationView(new InternalNotificationView(baseLayout));
    }

    /**
     * call before any super.()
     */
    public void stopListeningForNotifications() {
        notificationsMuteBackground = false;
        unregisterNotificationReceiver();
        receiver = null;
        clean();
    }

    public void registerNotificationReceiver() {
        if (receiver == null) return;

        IntentFilter filter = new IntentFilter(ACTION_INTERNAL_NOTIFICATION_UPDATE);
        localBroadCastUtil.registerReceiver(receiver, filter);
    }

    private void unregisterNotificationReceiver() {
        if (receiver == null) return;

        localBroadCastUtil.unregisterReceiver(receiver);
    }

    public void clean() {
        if (notificationView != null) {
            notificationView.unNaturallyDismiss();
            notificationView = null;
        }

        if (notificationHelper != null) {
            notificationHelper = null;
        }
    }

    public void showPendingInternalNotifications() {
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


    private void show(InternalNotification nextNotification) {
        if (notificationsMuteBackground) {
            activity.muteViews();
        }

        notificationView.setDismissListener(this::onUserDismissed);
        notificationView.show(nextNotification);
    }

    private void removeAnyShowingNotifications() {
        View currentShowing = notificationView.getCurrentNotificationView();
        if (currentShowing != null) {
            notificationView.removeView(currentShowing);
        }
    }

    private void notificationsDone() {
        if (notificationsMuteBackground) {
            activity.teardownMute();
        }
    }

    public void onUserDismissed(InternalNotification notificationDbObject) {
        removeNotificationFromDatabase(notificationDbObject);
        InternalNotification nextNotification = getNextNotification();
        if (nextNotification == null || notificationView == null) {
            notificationsDone();
            notificationView = null;
            return;
        }

        notificationView.show(nextNotification);
    }

    private InternalNotification getNextNotification() {
        if (notificationHelper == null) {
            return null;
        }

        return getNotificationsByPriority();
    }

    private void removeNotificationFromDatabase(InternalNotification notificationDbObject) {
        notificationHelper.setHasBeenSeen(notificationDbObject);
    }

    public void setBaseLayout(ViewGroup baseLayout) {
        this.baseLayout = baseLayout;
    }

    public void setDaoHelper(InternalNotificationHelper notificationHelper) {
        this.notificationHelper = notificationHelper;
    }

    private InternalNotification getNotificationsByPriority() {
        return notificationHelper.getNextUnseenNotification();
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public void setNotificationView(InternalNotificationView notificationView) {
        this.notificationView = notificationView;
    }

    public void setMute(boolean notificationsMuteBackground) {
        this.notificationsMuteBackground = notificationsMuteBackground;
    }

    public boolean isRegistered() {
        if (baseLayout == null) {
            return false;
        }

        if (notificationHelper == null) {
            return false;
        }

        if (notificationView == null) {
            return false;
        }

        return true;
    }

    protected class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_INTERNAL_NOTIFICATION_UPDATE.equals(intent.getAction())) {
                showPendingInternalNotifications();
            }
        }
    }
}
