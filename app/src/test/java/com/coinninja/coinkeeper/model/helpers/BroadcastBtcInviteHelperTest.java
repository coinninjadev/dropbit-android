package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.BroadcastBtcInvite;
import com.coinninja.coinkeeper.model.db.BroadcastBtcInviteDao;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;

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
public class BroadcastBtcInviteHelperTest {

    @Mock
    private QueryBuilder query;
    @Mock
    private BroadcastBtcInviteDao broadcastInviteDao;
    @Mock
    private BroadcastBtcInvite invite;

    @Mock
    private DaoSessionManager daoSessionManager;

    @InjectMocks
    private BroadcastBtcInviteHelper helper;

    @Before
    public void setUp() throws Exception {
        when(daoSessionManager.getBroadcastBtcInviteDao()).thenReturn(broadcastInviteDao);
        when(broadcastInviteDao.queryBuilder()).thenReturn(query);
    }

    @Test
    public void save_broadcast_btc_to_table_invite_test() {
        InviteTransactionSummary inviteTransaction = mock(InviteTransactionSummary.class);
        String sampleServerID = "Server ID";
        String sampleTXID = "BTC TX ID";
        String sampleAddress = "BTC ADDRESS";

        when(query.limit(1)).thenReturn(query);
        when(query.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(invite);

        helper.saveBroadcastBtcInvite(inviteTransaction, sampleServerID, sampleTXID, sampleAddress, BTCState.FULFILLED);

        verify(invite).refresh();
        verify(invite).update();
        verify(broadcastInviteDao).refresh(invite);
    }

    @Test
    public void save_canceled_broadcast_bbc_to_table_invite_test() {
        InviteTransactionSummary inviteTransaction = mock(InviteTransactionSummary.class);

        when(query.limit(1)).thenReturn(query);
        when(query.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(invite);

        helper.saveBroadcastInviteAsCanceled(inviteTransaction);

        verify(invite).setBtcState(BTCState.CANCELED);
        verify(invite).refresh();
        verify(invite).update();
        verify(broadcastInviteDao).refresh(invite);
    }


    @Test
    public void get_broadcast_invites_from_table_test() {
        List mockInvitesList = mock(List.class);
        when(query.list()).thenReturn(mockInvitesList);

        List<BroadcastBtcInvite> invites = helper.getBroadcastInvites();

        assertThat(invites, equalTo(mockInvitesList));
    }

    @Test
    public void get_broadcast_invites_from_table__no_data_found_test() {
        when(query.list()).thenReturn(null);

        List<BroadcastBtcInvite> invites = helper.getBroadcastInvites();

        assertThat(invites, nullValue());
    }


    @Test
    public void remove_btc_invite_test() {
        BroadcastBtcInvite mockInvite = mock(BroadcastBtcInvite.class);

        when(query.limit(1)).thenReturn(query);
        when(query.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(mockInvite);


        helper.removeBtcInvite(mockInvite);


        verify(broadcastInviteDao).delete(mockInvite);
    }

    @Test
    public void remove_btc_invite_but_invite_was_already_deleted_test() {
        BroadcastBtcInvite mockInvite = mock(BroadcastBtcInvite.class);

        when(query.limit(1)).thenReturn(query);
        when(query.where(BroadcastBtcInviteDao.Properties.InviteServerID.eq(any()))).thenReturn(query);
        when(query.unique()).thenReturn(null);


        helper.removeBtcInvite(mockInvite);

        verify(broadcastInviteDao, times(0)).delete(mockInvite);
    }
}