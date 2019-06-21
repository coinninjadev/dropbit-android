package com.coinninja.coinkeeper.model.helpers;

import android.net.Uri;

import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.InternalNotification;
import com.coinninja.coinkeeper.model.db.InternalNotificationDao;
import com.coinninja.coinkeeper.model.db.enums.InternalNotificationPriority;
import com.coinninja.coinkeeper.model.db.enums.MessageLevel;

import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InternalNotificationHelperTest {

    private InternalNotificationHelper internalNotificationHelper;
    @Mock
    InternalNotification notification;
    @Mock
    DaoSessionManager daoSessionManager;
    @Mock
    WalletHelper walletHelper;
    @Mock
    InternalNotificationDao internalNotificationDao;

    @Before
    public void setUp() throws Exception {
        internalNotificationHelper = new InternalNotificationHelper(daoSessionManager, walletHelper);

        QueryBuilder query = mock(QueryBuilder.class);
        when(daoSessionManager.getInternalNotificationDao()).thenReturn(internalNotificationDao);
        when(internalNotificationDao.queryBuilder()).thenReturn(query);


        when(query.limit(1)).thenReturn(query);
        when(query.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(notification);

    }

    @Test
    public void save_message_priority_internal_notification() {
        String UUID = "some uuid";
        InternalNotificationPriority priority = InternalNotificationPriority._0_HIGHEST;
        String message = "some message";
        MessageLevel level = MessageLevel.INFO;
        String actionURL = "https://coinninja.com/";

        internalNotificationHelper.addNotifications(UUID, Uri.parse(actionURL), priority, message, level);

        verify(notification).setPriority(priority);
    }

    @Test
    public void save_message_level_internal_notification() {
        String UUID = "some uuid";
        InternalNotificationPriority priority = InternalNotificationPriority._0_HIGHEST;
        String message = "some message";
        MessageLevel level = MessageLevel.INFO;
        String actionURL = "https://coinninja.com/";

        internalNotificationHelper.addNotifications(UUID, Uri.parse(actionURL), priority, message, level);

        verify(notification).setPriority(priority);
        verify(notification).setMessageLevel(level);
    }
}