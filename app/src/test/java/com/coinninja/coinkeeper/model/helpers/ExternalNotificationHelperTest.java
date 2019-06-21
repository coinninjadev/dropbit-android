package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.ExternalNotification;
import com.coinninja.coinkeeper.model.db.ExternalNotificationDao;

import org.greenrobot.greendao.query.QueryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExternalNotificationHelperTest {


    @Mock
    private ExternalNotificationDao externalNotificationDao;
    @Mock
    private ExternalNotification externalNotification;
    @Mock
    private DaoSessionManager daoSessionManager;
    @Mock
    private QueryBuilder query;
    @InjectMocks
    private ExternalNotificationHelper helper;

    @Before
    public void setUp() throws Exception {
        when(daoSessionManager.getExternalNotificationDao()).thenReturn(externalNotificationDao);
        when(externalNotificationDao.queryBuilder()).thenReturn(query);
    }

    @Test
    public void save_external_notification_to_table_invite_test() {
        String sampleMessage = "sample message";
        String sampleTXID = "BTC TX ID";

        when(query.limit(1)).thenReturn(query);
        when(query.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(externalNotification);

        helper.saveNotification(sampleMessage, sampleTXID);

        verify(externalNotification).refresh();
        verify(externalNotification).update();
        verify(externalNotificationDao).refresh(externalNotification);
    }

    @Test
    public void get_external_notification_from_table_test() {
        List mockInvitesList = mock(List.class);
        when(query.list()).thenReturn(mockInvitesList);

        List<ExternalNotification> notifications = helper.getNotifications();

        assertThat(notifications, equalTo(mockInvitesList));
    }

    @Test
    public void get_external_notification_from_table__no_data_found_test() {
        when(query.list()).thenReturn(null);

        List<ExternalNotification> notifications = helper.getNotifications();

        assertThat(notifications, nullValue());
    }


    @Test
    public void remove_external_notification_test() {
        ExternalNotification mockNotification = mock(ExternalNotification.class);

        when(query.limit(1)).thenReturn(query);
        when(query.where(ExternalNotificationDao.Properties.ExtraData.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(mockNotification);


        helper.removeNotification(mockNotification);


        verify(externalNotificationDao).delete(mockNotification);
    }

    @Test
    public void remove_external_notification_was_already_deleted_test() {
        ExternalNotification mockNotification = mock(ExternalNotification.class);

        when(query.limit(1)).thenReturn(query);
        when(query.where(ExternalNotificationDao.Properties.ExtraData.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(null);


        helper.removeNotification(mockNotification);

        verify(externalNotificationDao, times(0)).delete(mockNotification);
    }

}