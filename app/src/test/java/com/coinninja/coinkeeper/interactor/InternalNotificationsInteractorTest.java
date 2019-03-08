package com.coinninja.coinkeeper.interactor;

import android.content.Intent;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.base.MessagegerActivity;
import com.coinninja.coinkeeper.view.notifications.InternalNotificationView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class InternalNotificationsInteractorTest {

    private InternalNotificationsInteractor notificationsInteractor;
    private MessagegerActivity mockMessagegerActivity;
    private CoinKeeperApplication application;
    private LocalBroadCastUtil localBroadCast;
    private InternalNotificationHelper daoHelper;
    private InternalNotificationView notificationView;

    @Before
    public void setUp() throws Exception {
        notificationView = mock(InternalNotificationView.class);
        daoHelper = mock(InternalNotificationHelper.class);
        mockMessagegerActivity = mock(MessagegerActivity.class);
        application = (CoinKeeperApplication) RuntimeEnvironment.application;
        localBroadCast = new LocalBroadCastUtil(application);
        notificationsInteractor = new InternalNotificationsInteractor(mockMessagegerActivity, application, localBroadCast);
    }

    @Test
    public void start_listening_for_notifications() {
        InternalNotificationsInteractor.Receiver receiver = mock(InternalNotificationsInteractor.Receiver.class);
        LocalBroadCastUtil localBroadCastUtil = mock(LocalBroadCastUtil.class);
        InternalNotificationsInteractor interactor = new InternalNotificationsInteractor(mockMessagegerActivity, application, localBroadCastUtil);

        interactor.setReceiver(receiver);
        interactor.startListeningForNotifications(true);


        verify(localBroadCastUtil).registerReceiver(any(), any());
    }

    @Test
    public void localBroadCast_shows_notification_test() {
        InternalNotification notification = new InternalNotification();
        ViewGroup baseLayout = mock(ViewGroup.class);
        InternalNotificationView notificationView = mock(InternalNotificationView.class);
        when(daoHelper.getNextUnseenNotification()).thenReturn(notification);

        notificationsInteractor.startListeningForNotifications(true);
        notificationsInteractor.setNotificationView(notificationView);
        notificationsInteractor.setDaoHelper(daoHelper);
        notificationsInteractor.setBaseLayout(baseLayout);

        localBroadCast.sendBroadcast(new Intent(Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE));

        verify(notificationView).show(notification);
        verify(notificationView).setDismissListener(any());
    }

    @Test
    public void localBroadCast_set_notification_as_seen_test() {
        InternalNotification notification = new InternalNotification();
        initSampleNotifications(notification);

        localBroadCast.sendBroadcast(new Intent(Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE));
        notificationsInteractor.onUserDismissed(notification);

        verify(daoHelper).setHasBeenSeen(notification);
    }

    @Test
    public void localBroadCast_shows_five_notifications_test() {
        InternalNotification notification = new InternalNotification();
        initSampleNotifications(notification);
        when(daoHelper.getNextUnseenNotification()).thenReturn(notification);


        for (int i = 0; i < 10; i++) {
            if (i > 4) {
                when(daoHelper.getNextUnseenNotification()).thenReturn(null);
            }

            localBroadCast.sendBroadcast(new Intent(Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE));
        }

        //even thou we called localBroadCast 10 times we only had 5 items to show so only 5 were shown
        verify(notificationView, times(5)).show(notification);
    }

    @Test
    public void no_notifications_to_show() {
        InternalNotification notification = null;
        initSampleNotifications(notification);

        localBroadCast.sendBroadcast(new Intent(Intents.ACTION_INTERNAL_NOTIFICATION_UPDATE));

        verify(notificationView, times(0)).show(any());
        verify(mockMessagegerActivity).teardownMute();
    }

    @Test
    public void on_phone_in_background_do_teardown() {
        InternalNotificationsInteractor.Receiver receiver = mock(InternalNotificationsInteractor.Receiver.class);
        LocalBroadCastUtil mockLocal = mock(LocalBroadCastUtil.class);
        InternalNotificationsInteractor interactor = new InternalNotificationsInteractor(mockMessagegerActivity, application, mockLocal);
        interactor.setNotificationView(notificationView);
        interactor.setDaoHelper(daoHelper);
        interactor.setReceiver(receiver);


        interactor.stopListeningForNotifications();

        verify(mockLocal).unregisterReceiver(receiver);
        verify(notificationView).unNaturallyDismiss();

    }

    private void initSampleNotifications(InternalNotification notification) {
        ViewGroup baseLayout = mock(ViewGroup.class);
        when(daoHelper.getNextUnseenNotification()).thenReturn(notification);

        notificationsInteractor.startListeningForNotifications(true);
        notificationsInteractor.setNotificationView(notificationView);
        notificationsInteractor.setDaoHelper(daoHelper);
        notificationsInteractor.setBaseLayout(baseLayout);
    }
}