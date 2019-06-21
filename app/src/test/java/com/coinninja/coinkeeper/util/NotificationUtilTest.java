package com.coinninja.coinkeeper.util;

import android.content.Context;

import com.coinninja.coinkeeper.model.db.enums.InternalNotificationPriority;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;
import com.coinninja.coinkeeper.model.helpers.InternalNotificationHelper;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class NotificationUtilTest {

    @Mock
    InternalNotificationHelper internalNotificationHelper;

    @Mock
    LocalBroadCastUtil localBroadCastUtil;

    @Mock
    Context context;

    private NotificationUtil localNotificationUtil;

    @Before
    public void setUp() {
        localNotificationUtil = new NotificationUtil(context, internalNotificationHelper, localBroadCastUtil);
    }

    @Test
    public void queues_error_message() {
        String message = "There is a snake in my boot";
        localNotificationUtil.dispatchInternalError(message);

        verify(internalNotificationHelper).addNotifications(InternalNotificationPriority._0_HIGHEST, null, message, MessageLevel.ERROR);
        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE);
    }

    @Test
    public void queues_local_message_from_string_res_id() {
        String message = "you got mail";
        when(context.getString(1234)).thenReturn(message);

        localNotificationUtil.dispatchInternal(1234);

        verify(internalNotificationHelper).addNotifications(message);
    }

    @Test
    public void broadcasts_local_notification_changes() {
        String message = "you got mail";

        localNotificationUtil.dispatchInternal(message);

        verify(localBroadCastUtil).sendBroadcast(DropbitIntents.ACTION_INTERNAL_NOTIFICATION_UPDATE);
    }

    @Test
    public void queues_local_message() {
        String message = "you got mail";

        localNotificationUtil.dispatchInternal(message);

        verify(internalNotificationHelper).addNotifications(message);
    }

}