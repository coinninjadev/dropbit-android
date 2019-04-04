package com.coinninja.coinkeeper.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.ExternalNotification;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.Intents;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import dagger.android.AndroidInjection;

public class BtcBroadcastNotificationService extends JobIntentService {
    public static final String TAG = BtcBroadcastNotificationService.class.getName();
    @Inject
    ExternalNotificationHelper externalNotificationHelper;
    @Inject
    WalletHelper walletHelper;

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        onHandleIntent(intent);
    }

    public void show(Intent intent, ExternalNotification notificationDAO) {

        String notificationMessage = notificationDAO.getMessage();
        PendingIntent
                pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Resources r = getResources();
        Notification notification = new NotificationCompat.Builder(this, CoinKeeperApplication.INVITES_SERVICE_CHANNEL_ID)
                .setTicker(r.getString(R.string.notification_invite_fulfilled_ticker))
                .setSmallIcon(R.drawable.ic_dropbit_logo_small)
                .setContentTitle(r.getString(R.string.notification_invite_fulfilled_title))
                .setContentText(notificationMessage)
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
    }

    public void setExternalNotificationHelper(ExternalNotificationHelper externalNotificationHelper) {
        this.externalNotificationHelper = externalNotificationHelper;
    }

    protected void onHandleIntent(@Nullable Intent intent) {
        List<ExternalNotification> notifications = externalNotificationHelper.getNotifications();
        for (ExternalNotification notification : notifications) {
            showNotification(notification);
        }
    }

    private void showNotification(ExternalNotification notification) {
        if (notification == null) {
            return;
        }

        String txID = notification.getExtraData();
        if (txID == null || txID.isEmpty()) return;

        Intent clickedIntent = new Intent(this, TransactionHistoryActivity.class);
        clickedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        clickedIntent.putExtra(Intents.EXTRA_TRANSACTION_ID, txID);

        show(clickedIntent, notification);
        externalNotificationHelper.removeNotification(notification);
    }

}
