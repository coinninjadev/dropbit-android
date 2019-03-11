package com.coinninja.coinkeeper.service.runner;

import android.content.res.Resources;

import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FulfillSentInvitesRunnerTest {

    @Mock
    BroadcastBtcInviteRunner broadcastBtcInviteRunner;
    @Mock
    private PhoneNumber receiverPhoneNumber;
    @Mock
    private PhoneNumber senderPhoneNumber;
    @Mock
    private CoinKeeperApplication application;
    @Mock
    private SentInvitesStatusGetter statusRunner;
    @Mock
    private TransactionHelper txHelper;
    @Mock
    private SentInvitesStatusSender sender;
    @InjectMocks
    private FulfillSentInvitesRunner fulfillRunner;

    @After
    public void tearDown() throws Exception {
        application = null;
        statusRunner = null;
        txHelper = null;
        sender = null;
        broadcastBtcInviteRunner = null;
        fulfillRunner = null;
        receiverPhoneNumber = null;
        senderPhoneNumber = null;
    }

    @Test
    public void successful_full_flow() {

        fulfillRunner.run();

        //Step 1. grab all sent invites from server, save/update the ones that now have an address
        verify(statusRunner).run();

        //Step 2. get any sent invites that do not have a tx id but have an address
        verify(txHelper).gatherUnfulfilledInviteTrans();

        //Step 3. report to coinninja server of any invites that have been newly fulfilled (with TX ID)
        verify(sender).run();
    }

    @Test
    public void one_unfulfilled_test() {
        InviteTransactionSummary unfulfilled = mock(InviteTransactionSummary.class);

        List<InviteTransactionSummary> unfulfilledTransactions = new ArrayList<>();
        unfulfilledTransactions.add(unfulfilled);
        TransactionBuilder txBuilder = mock(TransactionBuilder.class);
        when(unfulfilled.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(unfulfilled.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);

        Resources re = mock(Resources.class);
        when(re.getString(anyInt())).thenReturn("Some String");
        when(application.getResources()).thenReturn(re);

        when(txHelper.gatherUnfulfilledInviteTrans()).thenReturn(unfulfilledTransactions);

        fulfillRunner.run();


        verify(broadcastBtcInviteRunner).run();
    }

    @Test
    public void three_or_more_unfulfilled_test() {
        InviteTransactionSummary unfulfilled1 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary unfulfilled2 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary unfulfilled3 = mock(InviteTransactionSummary.class);

        List<InviteTransactionSummary> unfulfilledTransactions = new ArrayList<>();
        unfulfilledTransactions.add(unfulfilled1);
        unfulfilledTransactions.add(unfulfilled2);
        unfulfilledTransactions.add(unfulfilled3);
        when(unfulfilled1.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(unfulfilled1.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);
        when(unfulfilled2.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(unfulfilled2.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);
        when(unfulfilled3.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(unfulfilled3.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);

        Resources re = mock(Resources.class);
        when(re.getString(anyInt())).thenReturn("Some String");
        when(application.getResources()).thenReturn(re);

        when(txHelper.gatherUnfulfilledInviteTrans()).thenReturn(unfulfilledTransactions);

        fulfillRunner.run();


        verify(broadcastBtcInviteRunner, times(3)).run();
    }

    @Test
    public void skip_invites_with_tx_id_test() {
        String sampleTxID = "some tx id";
        InviteTransactionSummary unfulfilled1 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary fulfilled2 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary unfulfilled3 = mock(InviteTransactionSummary.class);

        List<InviteTransactionSummary> unfulfilledTransactions = new ArrayList<>();
        unfulfilledTransactions.add(unfulfilled1);
        unfulfilledTransactions.add(fulfilled2);
        unfulfilledTransactions.add(unfulfilled3);
        when(unfulfilled1.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(unfulfilled1.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);
        when(fulfilled2.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(fulfilled2.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);
        when(fulfilled2.getBtcTransactionId()).thenReturn(sampleTxID);
        when(unfulfilled3.getReceiverPhoneNumber()).thenReturn(receiverPhoneNumber);
        when(unfulfilled3.getSenderPhoneNumber()).thenReturn(senderPhoneNumber);

        Resources re = mock(Resources.class);
        when(re.getString(anyInt())).thenReturn("Some String");
        when(application.getResources()).thenReturn(re);

        when(txHelper.gatherUnfulfilledInviteTrans()).thenReturn(unfulfilledTransactions);

        fulfillRunner.run();

        //only call two times, even tho we have 3 invitesop
        verify(broadcastBtcInviteRunner, times(2)).run();
    }

    @Test
    public void check_any_invite_to_see_if_it_has_a_tx_id_test() {
        String sampleTxID = "some tx id";
        InviteTransactionSummary fulfilled = mock(InviteTransactionSummary.class);
        when(fulfilled.getBtcTransactionId()).thenReturn(sampleTxID);

        boolean alreadyHasTxId = fulfillRunner.alreadyHasTxId(fulfilled);

        assertTrue(alreadyHasTxId);
    }

    @Test
    public void check_any_invite_to_see_if_it_has_a_null_tx_id_test() {
        String sampleTxID = null;
        InviteTransactionSummary fulfilled = mock(InviteTransactionSummary.class);
        when(fulfilled.getBtcTransactionId()).thenReturn(sampleTxID);

        boolean alreadyHasTxId = fulfillRunner.alreadyHasTxId(fulfilled);

        assertFalse(alreadyHasTxId);
    }

    @Test
    public void check_any_invite_to_see_if_it_has_a_empty_tx_id_test() {
        String sampleTxID = "";
        InviteTransactionSummary fulfilled = mock(InviteTransactionSummary.class);
        when(fulfilled.getBtcTransactionId()).thenReturn(sampleTxID);

        boolean alreadyHasTxId = fulfillRunner.alreadyHasTxId(fulfilled);

        assertFalse(alreadyHasTxId);
    }
}