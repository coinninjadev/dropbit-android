package com.coinninja.coinkeeper.cn.dropbit;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class DropBitCancellationManager {
    private final SignedCoinKeeperApiClient client;
    private final TransactionHelper transactionHelper;
    private final CNWalletManager cnWalletManager;

    @Inject
    DropBitCancellationManager(SignedCoinKeeperApiClient client, TransactionHelper transactionHelper, CNWalletManager cnWalletManager) {
        this.client = client;
        this.transactionHelper = transactionHelper;
        this.cnWalletManager = cnWalletManager;
    }

    public void markAsCanceled(List<InviteTransactionSummary> summaries) {
        if (summaries == null || summaries.isEmpty()) return;
        for (InviteTransactionSummary invite : summaries) {
            markAsCanceled(invite);
        }
    }

    public void markAsCanceled(InviteTransactionSummary invite) {
        String inviteID = invite.getServerId();

        Response response = client.updateInviteStatusCanceled(inviteID);
        if (response.isSuccessful()) {
            transactionHelper.updateInviteAsCanceled(inviteID);
            cnWalletManager.updateBalances();
        }
    }

    public void markAsCanceled(String id) {
        InviteTransactionSummary inviteTransactionSummary = transactionHelper.getInviteTransactionSummary(id);
        if (inviteTransactionSummary != null)
            markAsCanceled(inviteTransactionSummary);
    }

    public void markUnfulfilledAsCanceled() {
        markAsCanceled(transactionHelper.gatherUnfulfilledInviteTrans());
        cnWalletManager.updateBalances();
    }
}
