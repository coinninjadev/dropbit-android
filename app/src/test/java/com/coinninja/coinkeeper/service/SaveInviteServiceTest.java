package com.coinninja.coinkeeper.service;

import android.content.Intent;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.transaction.TransactionNotificationManager;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.util.Intents;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class SaveInviteServiceTest {

    @Mock
    InviteTransactionSummaryHelper inviteTransactionSummaryHelper;

    @Mock
    CNWalletManager cnWalletManager;

    @Mock
    TransactionNotificationManager transactionNotificationManager;

    private SaveInviteService service;
    private Intent intent;
    private CompletedInviteDTO completedInviteDTO;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Contact contact = new Contact(new PhoneNumber("+12223334444"), "Joe Blow", false);
        PendingInviteDTO pendingInviteDTO = new PendingInviteDTO(contact,
                340000L,
                100000L,
                100L,
                "--memo--",
                true
        );
        DropBitInvitation invitedContact = new DropBitInvitation(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                contact.getHash(),
                "",
                ""
        );

        completedInviteDTO = new CompletedInviteDTO(pendingInviteDTO, invitedContact);
        service = Robolectric.setupService(SaveInviteService.class);
        service.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
        service.cnWalletManager = cnWalletManager;
        service.transactionNotificationManager = transactionNotificationManager;

        intent = new Intent(service, SaveInviteService.class);
        intent.putExtra(Intents.EXTRA_COMPLETED_INVITE_DTO, completedInviteDTO);
    }

    @After
    public void tearDown() {
        service = null;
        intent = null;
        completedInviteDTO = null;
        cnWalletManager = null;
        inviteTransactionSummaryHelper = null;
    }

    @Test
    public void does_nothing_when_intent_does_not_have_completed_dto() {
        intent.removeExtra(Intents.EXTRA_COMPLETED_INVITE_DTO);

        verify(inviteTransactionSummaryHelper, times(0)).saveCompletedSentInvite(completedInviteDTO);
        verify(cnWalletManager, times(0)).updateBalances();
        verify(transactionNotificationManager, times(0)).saveTransactionNotificationLocally(any(InviteTransactionSummary.class), any());
    }

    @Test
    public void saves_invite_transaction_notification_and_updates_wallet() {
        InOrder orderedOperations = inOrder(inviteTransactionSummaryHelper, cnWalletManager, transactionNotificationManager);
        InviteTransactionSummary inviteTransactionSummary = mock(InviteTransactionSummary.class);
        when(inviteTransactionSummaryHelper.saveCompletedSentInvite(completedInviteDTO)).thenReturn(inviteTransactionSummary);

        service.onHandleIntent(intent);

        orderedOperations.verify(inviteTransactionSummaryHelper).saveCompletedSentInvite(completedInviteDTO);
        orderedOperations.verify(transactionNotificationManager).saveTransactionNotificationLocally(inviteTransactionSummary, completedInviteDTO);
        orderedOperations.verify(cnWalletManager).updateBalances();
    }
}