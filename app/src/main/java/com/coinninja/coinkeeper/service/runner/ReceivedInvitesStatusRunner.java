package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.analytics.Analytics;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class ReceivedInvitesStatusRunner implements Runnable {
    static final String TAG = ReceivedInvitesStatusRunner.class.getSimpleName();
    static final String RECEIVED_INVITE_FAILED = "|---- Received Invite failed";


    private final SignedCoinKeeperApiClient client;
    private final TransactionHelper transactionHelper;
    private final WalletHelper walletHelper;
    private final Analytics analytics;
    private final CNLogger logger;

    @Inject
    public ReceivedInvitesStatusRunner(SignedCoinKeeperApiClient client, TransactionHelper transactionHelper,
                                       WalletHelper walletHelper, Analytics analytics, CNLogger logger) {
        this.client = client;
        this.transactionHelper = transactionHelper;
        this.walletHelper = walletHelper;
        this.analytics = analytics;
        this.logger = logger;
    }

    @Override
    public void run() {
        Response response = client.getReceivedInvites();
        if (response.isSuccessful()) {
            saveCompletedInvites((List<ReceivedInvite>) response.body());
        } else {
            logger.logError(TAG, RECEIVED_INVITE_FAILED, response);
        }

        cleanInviteJoinTable();
    }

    private void saveCompletedInvites(List<ReceivedInvite> invites) {
        for (ReceivedInvite invite : invites) {
            String completedTxID = getCompletedTxID(invite);
            if (completedTxID == null || completedTxID.isEmpty()) {
                continue;
            }

            saveFulfilledInvite(invite);
        }
    }

    private void saveFulfilledInvite(ReceivedInvite invite) {
        transactionHelper.updateInviteTxIDTransaction(walletHelper.getWallet(), invite.getId(), invite.getTxid());
        analytics.setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, true);
    }

    String getCompletedTxID(ReceivedInvite invite) {
        String currentStatus = invite.getStatus();
        if (currentStatus == null) return null;

        if ("completed".contentEquals(currentStatus)) {
            return invite.getTxid();
        }

        return null;
    }

    private void cleanInviteJoinTable() {
        List<InviteTransactionSummary> invites = transactionHelper.getInvitesWithTxID();
        for (InviteTransactionSummary invite : invites) {
            String txID = invite.getBtcTransactionId();
            TransactionSummary transaction = transactionHelper.getTransactionWithTxID(txID);
            if (transaction == null) continue;

            transactionHelper.joinInviteToTx(invite, transaction);
        }
    }
}