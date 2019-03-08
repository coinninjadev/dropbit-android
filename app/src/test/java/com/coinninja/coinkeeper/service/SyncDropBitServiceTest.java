package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.cn.account.AccountManager;
import com.coinninja.coinkeeper.service.runner.FulfillSentInvitesRunner;
import com.coinninja.coinkeeper.service.runner.ReceivedInvitesStatusRunner;
import com.coinninja.coinkeeper.service.runner.SyncIncomingInvitesRunner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.inOrder;

@RunWith(MockitoJUnitRunner.class)
public class SyncDropBitServiceTest {

    private SyncDropBitService service;
    @Mock
    private SyncIncomingInvitesRunner syncIncomingInvitesRunner;
    @Mock
    private FulfillSentInvitesRunner fulfillSentInvitesRunner;
    @Mock
    private ReceivedInvitesStatusRunner receivedInvitesStatusRunner;
    @Mock
    private AccountManager accountManager;

    @Before
    public void setUp() throws Exception {
        service = new SyncDropBitService();
        service.fulfillSentInvitesRunner = fulfillSentInvitesRunner;
        service.syncIncomingInvitesRunner = syncIncomingInvitesRunner;
        service.receivedInvitesStatusRunner = receivedInvitesStatusRunner;
        service.accountManager = accountManager;
    }

    @After
    public void tearDown() {
        accountManager = null;
        service = null;
        syncIncomingInvitesRunner = null;
        fulfillSentInvitesRunner = null;
        receivedInvitesStatusRunner = null;
    }

    @Test
    public void run_all_dropbit_sync_runners_in_order_test() {
        InOrder inOrder = inOrder(accountManager, syncIncomingInvitesRunner, fulfillSentInvitesRunner, receivedInvitesStatusRunner);

        service.onHandleIntent(null);

        inOrder.verify(accountManager).cacheAddresses();
        inOrder.verify(syncIncomingInvitesRunner).run();
        inOrder.verify(fulfillSentInvitesRunner).run();
        inOrder.verify(receivedInvitesStatusRunner).run();
    }
}