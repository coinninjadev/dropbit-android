package com.coinninja.coinkeeper.cn.transaction;

import com.coinninja.bindings.DerivationPath;
import com.coinninja.bindings.UnspentTransactionOutput;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.model.TransactionNotificationMapper;
import com.coinninja.coinkeeper.model.UnspentTransactionHolder;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.dto.BroadcastTransactionDTO;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.dto.PendingInviteDTO;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.service.client.model.InvitedContact;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionNotificationManagerTest {

    private static final String PHONE_NUMBER_HASH = "710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d";
    private static final String TXID = "--txid--";
    private static final String PUBKEY = "--pubkey--";
    private final String ADDRESS = "--address--";
    @Mock
    CNLogger logger;
    @Mock
    private TransactionNotification transactionNotification;
    @Mock
    private DaoSessionManager daoSessionManager;
    @Mock
    private TransactionNotificationMapper transactionNotificationMapper;
    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private MessageEncryptor messageEncryptor;
    @Mock
    private Account account;
    @Mock
    private PhoneNumber receiverPhone;

    @InjectMocks
    private TransactionNotificationManager transactionNotificationManager;


    @Before
    public void setUp() {
        when(receiverPhone.toString()).thenReturn("+13305551111");
    }

    @After
    public void tearDown() {
        transactionNotificationManager = null;
        transactionNotification = null;
        daoSessionManager = null;
        apiClient = null;
        logger = null;
        account = null;
        messageEncryptor = null;
        receiverPhone = null;
    }

    @Test
    public void sends_encrypted_notification_when_invite_with_transaction_notification() {
        String json = "--v1-json--";
        String encryption = "--encryption--";
        String pubkey = PUBKEY;
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getBtcTransactionId()).thenReturn(TXID);
        when(invite.getAddress()).thenReturn(ADDRESS);
        when(invite.getPubkey()).thenReturn(pubkey);
        when(invite.getReceiverPhoneNumber()).thenReturn(receiverPhone);
        when(invite.getTransactionNotification()).thenReturn(transactionNotification);
        TransactionNotificationV1 transactionNotificationV1 = mock(TransactionNotificationV1.class);
        when(transactionNotificationV1.toString()).thenReturn(json);
        when(transactionNotificationMapper.toV1(invite, account)).thenReturn(transactionNotificationV1);
        when(messageEncryptor.encrypt(json, pubkey)).thenReturn(encryption);
        when(apiClient.postTransactionNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(Response.success(null));

        transactionNotificationManager.notifyCnOfFundedInvite(invite);

        verify(apiClient).postTransactionNotification(TXID, ADDRESS,
                PHONE_NUMBER_HASH, encryption);
    }

    @Test
    public void sends_empty_notification_when_invite_with_transaction_notification_but_no_pubkey() {
        String pubkey = null;
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getBtcTransactionId()).thenReturn(TXID);
        when(invite.getAddress()).thenReturn(ADDRESS);
        when(invite.getPubkey()).thenReturn(pubkey);
        when(invite.getReceiverPhoneNumber()).thenReturn(receiverPhone);
        when(invite.getTransactionNotification()).thenReturn(transactionNotification);
        when(apiClient.postTransactionNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(Response.success(null));

        transactionNotificationManager.notifyCnOfFundedInvite(invite);

        verify(apiClient).postTransactionNotification(TXID, ADDRESS,
                PHONE_NUMBER_HASH, "");
    }

    @Test
    public void sends_empty_notification_when_invite_has_no_transaction_notification() {
        InviteTransactionSummary invite = mock(InviteTransactionSummary.class);
        when(invite.getBtcTransactionId()).thenReturn(TXID);
        when(invite.getAddress()).thenReturn(ADDRESS);
        when(invite.getPubkey()).thenReturn(PUBKEY);
        when(invite.getReceiverPhoneNumber()).thenReturn(receiverPhone);
        when(invite.getTransactionNotification()).thenReturn(null);
        when(apiClient.postTransactionNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(Response.success(null));

        transactionNotificationManager.notifyCnOfFundedInvite(invite);

        verify(apiClient).postTransactionNotification(TXID, ADDRESS,
                PHONE_NUMBER_HASH, "");
    }

    @Test
    public void only_saves_notification_for_invite_when_memo_exists() {
        InviteTransactionSummary inviteSummary = mock(InviteTransactionSummary.class);
        CompletedInviteDTO completedInviteDTO = createCompletedInviteDTO();
        completedInviteDTO.setMemo("");

        transactionNotificationManager.saveTransactionNotificationLocally(inviteSummary, completedInviteDTO);

        verify(daoSessionManager, times(0)).insert(any(TransactionNotification.class));
        verify(inviteSummary, times(0)).setTransactionNotification(transactionNotification);
        verify(inviteSummary, times(0)).update();

    }

    @Test
    public void saves_transaction_locally_for_given_invite() {
        CompletedInviteDTO completedInviteDTO = createCompletedInviteDTO();
        InviteTransactionSummary inviteSummary = mock(InviteTransactionSummary.class);
        when(daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification);

        transactionNotificationManager.saveTransactionNotificationLocally(inviteSummary, completedInviteDTO);

        verify(transactionNotification).setIsShared(true);
        verify(transactionNotification).setMemo("--memo--");
        verify(daoSessionManager).insert(transactionNotification);
        verify(inviteSummary).setTransactionNotification(transactionNotification);
        verify(inviteSummary).update();
    }

    @Test
    public void saves_transaction_locally_for_given_transaction() {
        CompletedBroadcastDTO completedBroadcastDTO = createCompletedBroadCastDTO();
        completedBroadcastDTO.transactionId = TXID;
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        when(daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification);

        transactionNotificationManager.saveTransactionNotificationLocally(transactionSummary, completedBroadcastDTO);

        verify(transactionNotification).setIsShared(true);
        verify(transactionNotification).setMemo("--memo--");
        verify(daoSessionManager).insert(transactionNotification);
        verify(transactionSummary).setTransactionNotification(transactionNotification);
        verify(transactionSummary).update();
        verify(transactionNotification).setTxid(TXID);
        verify(transactionNotification).update();
    }

    @Test
    public void requests_to_notify_recipient_when_not_sharing_a_memo() {
        CompletedBroadcastDTO completedBroadcastDTO = createCompletedBroadCastDTO();
        TransactionNotificationV1 transactionNotificationV1 = mock(TransactionNotificationV1.class);
        String json = "--transaction-v1-as-json--";
        when(transactionNotificationV1.toString()).thenReturn(json);
        when(daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification);
        when(transactionNotificationMapper.toV1(completedBroadcastDTO, account)).thenReturn(transactionNotificationV1);
        when(apiClient.postTransactionNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(Response.success(200));

        transactionNotificationManager.notifyOfPayment(completedBroadcastDTO);

        verify(apiClient).postTransactionNotification(completedBroadcastDTO.transactionId,
                completedBroadcastDTO.getHolder().paymentAddress,
                completedBroadcastDTO.getPhoneNumberHash(),
                "");

    }

    @Test
    public void sends_transaction_notification_to_cn() {
        CompletedBroadcastDTO completedBroadcastDTO = createCompletedBroadCastDTO();
        TransactionNotificationV1 transactionNotificationV1 = mock(TransactionNotificationV1.class);
        String json = "--transaction-v1-as-json--";
        String encryption = "--encryption--";
        when(transactionNotificationV1.toString()).thenReturn(json);
        when(daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification);
        when(transactionNotificationMapper.toV1(completedBroadcastDTO, account)).thenReturn(transactionNotificationV1);
        when(messageEncryptor.encrypt(json, completedBroadcastDTO.getPublicKey())).thenReturn(encryption);
        when(apiClient.postTransactionNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(Response.success(200));

        transactionNotificationManager.sendTransactionNotificationToReceiver(completedBroadcastDTO);

        verify(apiClient).postTransactionNotification(completedBroadcastDTO.transactionId,
                completedBroadcastDTO.getHolder().paymentAddress,
                completedBroadcastDTO.getPhoneNumberHash(),
                encryption);

    }

    @Test
    public void failures_in_sending_memos_are_logged() {
        CompletedBroadcastDTO completedBroadcastDTO = createCompletedBroadCastDTO();
        TransactionNotificationV1 transactionNotificationV1 = mock(TransactionNotificationV1.class);
        String json = "--transaction-v1-as-json--";
        String encryption = "--encryption--";
        when(transactionNotificationV1.toString()).thenReturn(json);
        when(daoSessionManager.newTransactionNotification()).thenReturn(transactionNotification);
        when(transactionNotificationMapper.toV1(completedBroadcastDTO, account)).thenReturn(transactionNotificationV1);
        when(messageEncryptor.encrypt(json, completedBroadcastDTO.getPublicKey())).thenReturn(encryption);
        Response error = Response.error(400, ResponseBody.create(MediaType.get("plain/text"), ""));
        when(apiClient.postTransactionNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(error);

        transactionNotificationManager.sendTransactionNotificationToReceiver(completedBroadcastDTO);
        transactionNotificationManager.notifyOfPayment(completedBroadcastDTO);

        verify(logger, times(2)).logError(TransactionNotificationManager.class.getSimpleName(),
                "Transaction Notification Post Failed", error);

    }

    private CompletedBroadcastDTO createCompletedBroadCastDTO() {
        UnspentTransactionOutput[] outputs = new UnspentTransactionOutput[0];
        UnspentTransactionHolder holder = new UnspentTransactionHolder(
                10000100L,
                outputs,
                10000000L,
                100L,
                400000L,
                mock(DerivationPath.class),
                "--pay-address--");
        BroadcastTransactionDTO broadcastTransactionDTO = new BroadcastTransactionDTO(holder,
                new Contact(receiverPhone, "Joe", true),
                true,
                "--memo--",
                PUBKEY
        );
        return new CompletedBroadcastDTO(broadcastTransactionDTO, TXID);
    }

    private CompletedInviteDTO createCompletedInviteDTO() {
        Contact contact = new Contact(receiverPhone, "Joe Blow", false);
        PendingInviteDTO pendingInviteDTO = new PendingInviteDTO(contact,
                340000L,
                100000L,
                100L,
                "--memo--",
                true
        );
        InvitedContact invitedContact = new InvitedContact(
                "--cn-id--",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                "",
                contact.getHash(),
                "",
                ""
        );

        return new CompletedInviteDTO(pendingInviteDTO, invitedContact);
    }
}