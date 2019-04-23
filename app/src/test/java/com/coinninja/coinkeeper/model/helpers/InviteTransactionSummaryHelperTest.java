package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.bindings.TransactionBroadcastResult;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionsInvitesSummary;
import com.coinninja.coinkeeper.model.db.Wallet;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.db.enums.Type;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.query.InviteSummaryQueryManager;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InviteTransactionSummaryHelperTest {

    private final String RECEIVER_PHONE_STRING = "+13305552222";
    private final String SENDER_PHONE_STRING = "+13305551111";
    @Mock
    DaoSessionManager daoSessionManager;

    @Mock
    InviteSummaryQueryManager inviteSummaryQueryManager;

    @Mock
    TransactionHelper transactionHelper;

    @Mock
    WalletHelper walletHelper;

    @Mock
    Account account;

    @Mock
    DateUtil dateUtil;

    @Mock
    TransactionsInvitesSummary transactionsInvitesSummary;

    @Mock
    InviteTransactionSummary inviteTransactionSummary;

    @Mock
    PhoneNumberUtil phoneNumberUtil;

    @InjectMocks
    InviteTransactionSummaryHelper helper;

    @Mock
    private PhoneNumber senderPhoneNumber;
    @Mock
    private PhoneNumber receiverPhoneNumber;

    @After
    public void tearDown() {
        walletHelper = null;
        daoSessionManager = null;
        helper = null;
        account = null;
        transactionsInvitesSummary = null;
        inviteTransactionSummary = null;
        phoneNumberUtil = null;
    }

    @Before
    public void setUp() throws Exception {
        when(senderPhoneNumber.toString()).thenReturn(SENDER_PHONE_STRING);
        when(receiverPhoneNumber.toString()).thenReturn(RECEIVER_PHONE_STRING);
    }

    @Test
    public void creates_new_temp_invite_when_server_id_absent_from_records() {
        InOrder orderedOperations = inOrder(daoSessionManager, inviteTransactionSummary, transactionsInvitesSummary);

        PendingInviteDTO pendingInviteDTO = createPendingInviteDTO();
        when(inviteSummaryQueryManager.getInviteSummaryByCnId(pendingInviteDTO.getRequestId())).thenReturn(null);
        when(daoSessionManager.newInviteTransactionSummary()).thenReturn(inviteTransactionSummary);
        when(daoSessionManager.insert(inviteTransactionSummary)).thenReturn(1L);

        when(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary);

        assertThat(helper.getOrCreateInviteSummaryWithServerId(pendingInviteDTO.getRequestId()),
                equalTo(transactionsInvitesSummary));

        orderedOperations.verify(inviteTransactionSummary).setServerId(pendingInviteDTO.getRequestId());
        orderedOperations.verify(daoSessionManager).insert(inviteTransactionSummary);
        orderedOperations.verify(daoSessionManager).insert(transactionsInvitesSummary);
        orderedOperations.verify(inviteTransactionSummary).update();
    }

    @Test
    public void returns_existing_invite_join_record_when_server_id_exists_in_records() {
        when(inviteTransactionSummary.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        PendingInviteDTO completedInviteDTO = createPendingInviteDTO();
        when(inviteSummaryQueryManager.getInviteSummaryByCnId(completedInviteDTO.getRequestId())).thenReturn(inviteTransactionSummary);

        assertThat(helper.getOrCreateInviteSummaryWithServerId(completedInviteDTO.getRequestId()),
                equalTo(transactionsInvitesSummary));
    }

    @Test
    public void saves_temporary_sent_invite() {
        Wallet wallet = mock(Wallet.class);
        PendingInviteDTO pendingInviteDTO = createPendingInviteDTO();
        when(daoSessionManager.newInviteTransactionSummary()).thenReturn(inviteTransactionSummary);
        when(daoSessionManager.newTransactionInviteSummary()).thenReturn(transactionsInvitesSummary);
        when(inviteSummaryQueryManager.getInviteSummaryByCnId(pendingInviteDTO.getRequestId())).thenReturn(inviteTransactionSummary);
        when(inviteTransactionSummary.getTransactionsInvitesSummary()).thenReturn(transactionsInvitesSummary);
        when(transactionsInvitesSummary.getInviteTransactionSummary()).thenReturn(inviteTransactionSummary);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getPhoneNumber()).thenReturn(senderPhoneNumber);
        when(walletHelper.getWallet()).thenReturn(wallet);

        InviteTransactionSummary invite = helper.saveTemporaryInvite(pendingInviteDTO);

        verify(inviteTransactionSummary).setInviteName(pendingInviteDTO.getContact().getDisplayName());
        verify(inviteTransactionSummary).setHistoricValue(34000L);
        verify(inviteTransactionSummary).setReceiverPhoneNumber(receiverPhoneNumber);
        verify(inviteTransactionSummary).setSenderPhoneNumber(senderPhoneNumber);
        verify(inviteTransactionSummary).setBtcState(BTCState.UNACKNOWLEDGED);
        verify(inviteTransactionSummary).setValueSatoshis(pendingInviteDTO.getInviteAmount());
        verify(inviteTransactionSummary).setValueFeesSatoshis(pendingInviteDTO.getInviteFee());
        verify(inviteTransactionSummary).setWallet(wallet);
        verify(inviteTransactionSummary).setType(Type.SENT);
        verify(inviteTransactionSummary).update();

        assertThat(invite, equalTo(inviteTransactionSummary));
    }

    @Test
    public void updates_sent_invite_when_transaction_is_fulfilled() {
        long currentTimeMillis = System.currentTimeMillis();
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        String txid = "--txid--";
        TransactionBroadcastResult transactionBroadcastResult = mock(TransactionBroadcastResult.class);
        when(transactionBroadcastResult.getTxId()).thenReturn(txid);
        when(transactionsInvitesSummary.getInviteTransactionSummary()).thenReturn(inviteTransactionSummary);
        when(transactionHelper.createInitialTransaction(txid)).thenReturn(transactionSummary);
        when(dateUtil.getCurrentTimeInMillis()).thenReturn(currentTimeMillis);
        InOrder orderedOperations = inOrder(transactionsInvitesSummary, inviteTransactionSummary, transactionSummary);

        helper.updateFulfilledInvite(transactionsInvitesSummary, transactionBroadcastResult);

        orderedOperations.verify(inviteTransactionSummary).setBtcTransactionId(txid);
        orderedOperations.verify(inviteTransactionSummary).setBtcState(BTCState.FULFILLED);
        orderedOperations.verify(transactionsInvitesSummary).setInviteTxID(txid);
        orderedOperations.verify(transactionsInvitesSummary).setTransactionTxID(txid);
        orderedOperations.verify(transactionsInvitesSummary).setTransactionSummary(transactionSummary);
        orderedOperations.verify(transactionsInvitesSummary).setInviteTime(0L);
        orderedOperations.verify(transactionsInvitesSummary).setBtcTxTime(currentTimeMillis);

        orderedOperations.verify(inviteTransactionSummary).update();
        orderedOperations.verify(transactionsInvitesSummary).update();
    }

    private PendingInviteDTO createPendingInviteDTO() {
        Contact contact = new Contact(receiverPhoneNumber, "Joe Blow", false);
        PendingInviteDTO pendingInviteDTO = new PendingInviteDTO(contact,
                340000L,
                10000000L,
                100L,
                "--memo--",
                true
        );

        return pendingInviteDTO;
    }
}