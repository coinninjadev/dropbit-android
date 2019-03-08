package com.coinninja.coinkeeper.service.runner;

import android.content.Context;
import android.content.res.Resources;

import com.coinninja.bindings.TransactionBuilder;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FulfillSentInvitesTest {

    @Mock
    private Context context;
    @Mock
    private SentInvitesStatusGetter sentInvitesStatusGetter;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private SentInvitesStatusSender sentInvitesStatusSender;

    @Mock
    private BroadcastBtcInviteRunner broadcastBtcInviteRunner;

    @InjectMocks
    private FulfillSentInvitesRunner fulfillRunner;

    private final PhoneNumberUtil phoneNumberUtil = new PhoneNumberUtil();
    private final PhoneNumber receiverPhoneNumber = new PhoneNumber("+12223334444");
    private final PhoneNumber senderPhoneNumber = new PhoneNumber("+12062020925");

    @After
    public void tearDown() {
        context = null;
        sentInvitesStatusGetter = null;
        transactionHelper = null;
        sentInvitesStatusSender = null;
        broadcastBtcInviteRunner = null;
        fulfillRunner = null;
    }

    @Test
    public void successful_full_flow() {
        InOrder inOrder = inOrder(sentInvitesStatusGetter, transactionHelper, sentInvitesStatusSender);

        fulfillRunner.run();

        //Step 1. grab all sent invites from server, save/update the ones that now have an address
        inOrder.verify(sentInvitesStatusGetter).run();

        //Step 2. get any sent invites that do not have a tx id but have an address
        inOrder.verify(transactionHelper).gatherUnfulfilledInviteTrans();

        //Step 3. report to coinninja server of any invites that have been newly fulfilled (with TX ID)
        inOrder.verify(sentInvitesStatusSender).run();
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
        when(context.getResources()).thenReturn(re);

        when(transactionHelper.gatherUnfulfilledInviteTrans()).thenReturn(unfulfilledTransactions);

        fulfillRunner.run();


        verify(broadcastBtcInviteRunner, times(1)).run();
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

        TransactionBuilder txBuilder = mock(TransactionBuilder.class);
        when(context.getString(anyInt())).thenReturn("Some String");

        when(transactionHelper.gatherUnfulfilledInviteTrans()).thenReturn(unfulfilledTransactions);

        fulfillRunner.run();


        verify(broadcastBtcInviteRunner, times(3)).run();
    }

}