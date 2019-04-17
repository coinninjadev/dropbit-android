package com.coinninja.coinkeeper.cn.transaction;

import com.coinninja.coinkeeper.model.TransactionNotificationMapper;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionNotification;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.dto.CompletedBroadcastDTO;
import com.coinninja.coinkeeper.model.dto.CompletedInviteDTO;
import com.coinninja.coinkeeper.model.encryptedpayload.v1.TransactionNotificationV1;
import com.coinninja.coinkeeper.model.helpers.DaoSessionManager;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.Contact;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.encryption.MessageEncryptor;

import javax.inject.Inject;

import retrofit2.Response;

public class TransactionNotificationManager {
    private final TransactionNotificationMapper transactionNotificationMapper;
    private final MessageEncryptor messageEncryptor;
    private final DaoSessionManager daoSessionManager;
    private final Account account;
    private final SignedCoinKeeperApiClient apiClient;
    private final CNLogger logger;

    @Inject
    TransactionNotificationManager(TransactionNotificationMapper transactionNotificationMapper,
                                   MessageEncryptor messageEncryptor, DaoSessionManager daoSessionManager,
                                   Account account, SignedCoinKeeperApiClient apiClient, CNLogger logger) {

        this.transactionNotificationMapper = transactionNotificationMapper;
        this.messageEncryptor = messageEncryptor;
        this.daoSessionManager = daoSessionManager;
        this.account = account;
        this.apiClient = apiClient;
        this.logger = logger;
    }

    public void saveTransactionNotificationLocally(InviteTransactionSummary inviteTransactionSummary, CompletedInviteDTO completedInviteDTO) {
        if (!completedInviteDTO.hasMemo()) return;

        TransactionNotification transactionNotification = createTransactionNotification(completedInviteDTO.getMemo(),
                completedInviteDTO.isMemoIsShared());

        inviteTransactionSummary.setTransactionNotification(transactionNotification);
        inviteTransactionSummary.update();
    }

    public void notifyCnOfFundedInvite(InviteTransactionSummary invite) {
        TransactionNotification transactionNotification = invite.getTransactionNotification();
        Contact contact = new Contact(invite.getReceiverPhoneNumber(), invite.getInviteName(), true);

        if (null == transactionNotification || null == invite.getPubkey()) {
            sendNotificationToCN(invite.getBtcTransactionId(), invite.getAddress(), contact.getHash(), "");
        } else {
            TransactionNotificationV1 message = transactionNotificationMapper.toV1(invite, account);
            String encryption = messageEncryptor.encrypt(message.toString(), invite.getPubkey());
            sendNotificationToCN(invite.getBtcTransactionId(), invite.getAddress(), contact.getHash(), encryption);
        }
    }

    public void saveTransactionNotificationLocally(TransactionSummary transactionSummary,
                                                   CompletedBroadcastDTO completedBroadcastActivityDTO) {
        if (!completedBroadcastActivityDTO.hasMemo()) return;

        TransactionNotification transactionNotification = createTransactionNotification(completedBroadcastActivityDTO.getMemo(),
                completedBroadcastActivityDTO.isMemoShared());
        transactionSummary.setTransactionNotification(transactionNotification);
        transactionNotification.setTxid(completedBroadcastActivityDTO.transactionId);
        transactionNotification.update();
        transactionSummary.update();
    }

    private TransactionNotification createTransactionNotification(String memo, boolean isShared) {
        TransactionNotification transactionNotification = daoSessionManager.newTransactionNotification();
        transactionNotification.setMemo(memo);
        transactionNotification.setIsShared(isShared);
        daoSessionManager.insert(transactionNotification);
        return transactionNotification;
    }

    public void sendTransactionNotificationToReceiver(CompletedBroadcastDTO completedBroadcastDTO) {
        TransactionNotificationV1 message = transactionNotificationMapper.toV1(completedBroadcastDTO, account);
        String encryption = messageEncryptor.encrypt(message.toString(), completedBroadcastDTO.getPublicKey());

        sendNotificationToCN(completedBroadcastDTO.transactionId,
                completedBroadcastDTO.getTransactionData().getPaymentAddress(),
                completedBroadcastDTO.getPhoneNumberHash(),
                encryption);
    }

    public void notifyOfPayment(CompletedBroadcastDTO completedBroadcastDTO) {
        sendNotificationToCN(completedBroadcastDTO.transactionId,
                completedBroadcastDTO.getTransactionData().getPaymentAddress(),
                completedBroadcastDTO.getPhoneNumberHash(),
                "");

    }

    private void sendNotificationToCN(String transactionId, String paymentAddress,
                                      String phoneNumberHash, String encryption) {

        Response response = apiClient.postTransactionNotification(transactionId,
                paymentAddress, phoneNumberHash, encryption);

        if (!response.isSuccessful()) {
            logger.logError(getClass().getSimpleName(), "Transaction Notification Post Failed", response);
        }
    }

}
