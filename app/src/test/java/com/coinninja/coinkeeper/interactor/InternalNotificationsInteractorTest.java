package com.coinninja.coinkeeper.interactor;

import android.content.Intent;
import android.view.ViewGroup;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.notifications.InternalNotificationView;
import com.coinninja.matchers.IntentFilterMatchers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class InternalNotificationsInteractorTest {

    @Mock
    private BaseActivity activity;
    @Mock
    private LocalBroadCastUtil localBroadCastUtil;
    @Mock
    private InternalNotificationHelper internalNotificationHelper;
    @Mock
    private InternalNotificationView notificationView;
    @Mock
    private ViewGroup baseLayout;

    private InternalNotificationsInteractor notificationsInteractor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        notificationsInteractor = new InternalNotificationsInteractor(internalNotificationHelper, localBroadCastUtil);
        notificationsInteractor.setNotificationView(notificationView);
        when(activity.findViewById(R.id.message_queue)).thenReturn(baseLayout);
    }

    @Test
    public void start_listening_for_notifications() {
        notificationsInteractor.startListeningForNotifications(activity, true);

        verify(localBroadCastUtil).registerReceiver(notificationsInteractor.receiver, notificationsInteractor.filter);
        assertThat(notificationsInteractor.filter, IntentFilterMatchers.containsAction(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE));
    }

    @Test
    public void localBroadCast_shows_notification_test() {
        InternalNotification notification = new InternalNotification();
        initSampleNotifications(notification);

        notificationsInteractor.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE));

        verify(notificationView).show(notification);
        verify(notificationView).setDismissListener(any());
    }

    @Test
    public void localBroadCast_set_notification_as_seen_test() {
        InternalNotification notification = new InternalNotification();
        initSampleNotifications(notification);
        notificationsInteractor.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE));

        notificationsInteractor.onUserDismissed(notification);

        verify(internalNotificationHelper).setHasBeenSeen(notification);
    }

    @Test
    public void localBroadCast_shows_five_notifications_test() {
        InternalNotification notification = new InternalNotification();
        initSampleNotifications(notification);
        when(internalNotificationHelper.getNextUnseenNotification()).thenReturn(notification);


        for (int i = 0; i < 10; i++) {
            if (i > 4) {
                when(internalNotificationHelper.getNextUnseenNotification()).thenReturn(null);
            }

            notificationsInteractor.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE));
        }

        //even thou we called localBroadCastUtil 10 times we only had 5 items to show so only 5 were shown
        verify(notificationView, times(5)).show(notification);
    }

    @Test
    public void no_notifications_to_show() {
        InternalNotification notification = null;
        initSampleNotifications(notification);

        notificationsInteractor.receiver.onReceive(activity, new Intent(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE));

        verify(notificationView, times(0)).show(any());
        verify(activity).teardownMute();
    }

    @Test
    public void on_phone_in_background_do_teardown() {
        initSampleNotifications(null);

        notificationsInteractor.stopListeningForNotifications();

        verify(localBroadCastUtil).unregisterReceiver(notificationsInteractor.receiver);
        verify(notificationView).unNaturallyDismiss();

    }

    private void initSampleNotifications(InternalNotification notification) {
        when(internalNotificationHelper.getNextUnseenNotification()).thenReturn(notification);
        notificationsInteractor.startListeningForNotifications(activity, true);
        notificationsInteractor.setNotificationView(notificationView);
    }
}