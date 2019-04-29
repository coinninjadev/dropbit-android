package com.coinninja.coinkeeper.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.ExternalNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.helpers.ExternalNotificationHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.util.DropbitIntents;

import org.greenrobot.greendao.query.LazyList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowPendingIntent;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BtcBroadcastNotificationServiceTest {


    private BtcBroadcastNotificationService service;
    private WalletHelper walletHelper;
    private ExternalNotificationHelper externalNotificationHelper;

    @Before
    public void setUp() {

        service = Robolectric.setupService(BtcBroadcastNotificationService.class);

        externalNotificationHelper = mock(ExternalNotificationHelper.class);
        walletHelper = mock(WalletHelper.class);

        service.setExternalNotificationHelper(externalNotificationHelper);
        service.walletHelper = walletHelper;
    }


    @Test
    public void show_notification_details_test() {
        List<ExternalNotification> ExternalNotificationDAOList = buildSampleExternalNotification();
        when(externalNotificationHelper.getNotifications()).thenReturn(ExternalNotificationDAOList);

        LazyList<TransactionsInvitesSummary> transaction = mock(LazyList.class);
        when(transaction.size()).thenReturn(1);
        TransactionsInvitesSummary transactionItem = mock(TransactionsInvitesSummary.class);
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(transactionSummary.getTxid()).thenReturn("tx id 3");
        when(transactionItem.getTransactionSummary()).thenReturn(transactionSummary);

        ArrayList<TransactionsInvitesSummary> transactionItems = new ArrayList<>();
        transactionItems.add(transactionItem);
        when(transaction.iterator()).thenReturn(transactionItems.iterator());
        when(walletHelper.getTransactionsLazily()).thenReturn(transaction);

        service.onHandleIntent(null);

        NotificationManager notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = shadowOf(notificationManager).getNotification(null, 0);
        ShadowNotification no = shadowOf(notification);

        assertThat(no.getContentText(), equalTo("Some sampleNotification Message"));
    }

    @Test
    public void get_one_notification_from_dab_then_show_notification_test() {
        List<ExternalNotification> ExternalNotificationDAOList = buildSampleExternalNotification();
        when(externalNotificationHelper.getNotifications()).thenReturn(ExternalNotificationDAOList);

        LazyList<TransactionsInvitesSummary> transaction = mock(LazyList.class);
        when(transaction.size()).thenReturn(1);
        TransactionsInvitesSummary transactionItem = mock(TransactionsInvitesSummary.class);
        when(transactionItem.getTransactionTxID()).thenReturn("tx id 3");
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(transactionSummary.getTxid()).thenReturn("tx id 3");
        when(transactionItem.getTransactionSummary()).thenReturn(transactionSummary);

        ArrayList<TransactionsInvitesSummary> transactionItems = new ArrayList<>();
        transactionItems.add(transactionItem);
        when(transaction.iterator()).thenReturn(transactionItems.iterator());
        when(walletHelper.getTransactionsLazily()).thenReturn(transaction);

        service.onHandleIntent(null);

        NotificationManager notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);


        Notification notification = shadowOf(notificationManager).getNotification(null, 0);

        assertThat(notification, notNullValue());
        assertThat(notification.getChannelId(), equalTo(CoinKeeperApplication.INVITES_SERVICE_CHANNEL_ID));
    }

    @Test
    public void show_txid() {
        List<ExternalNotification> ExternalNotificationDAOList = buildSampleExternalNotification();
        when(externalNotificationHelper.getNotifications()).thenReturn(ExternalNotificationDAOList);

        service.onHandleIntent(null);

        NotificationManager notificationManager = (NotificationManager) RuntimeEnvironment.application.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = shadowOf(notificationManager).getNotification(null, 0);
        PendingIntent pendingIntent = notification.contentIntent;
        ShadowPendingIntent pi = shadowOf(pendingIntent);
        Intent intent = pi.getSavedIntent();
        assertThat(intent.getExtras().getString(DropbitIntents.EXTRA_TRANSACTION_ID), equalTo("tx id 3"));
    }

    @Test
    public void deletes_wallet() {
        service.onHandleIntent(null);

    }

    private List<ExternalNotification> buildSampleExternalNotification() {
        ExternalNotification sampleNotification = new ExternalNotification();
        sampleNotification.setMessage("Some sampleNotification Message");
        sampleNotification.setExtraData("tx id 3");

        List<ExternalNotification> notifications = new ArrayList<>();
        notifications.add(sampleNotification);
        return notifications;
    }

}