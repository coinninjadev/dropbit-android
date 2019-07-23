package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;

public class GetIncomingInviteRunner implements Runnable {
    private static final String TAG = InviteContactRunner.class.getSimpleName();

    private final SignedCoinKeeperApiClient client;
    private final InviteTransactionSummaryHelper inviteTransactionSummaryHelper;

    @Inject
    public GetIncomingInviteRunner(SignedCoinKeeperApiClient client,
                                   InviteTransactionSummaryHelper inviteTransactionSummaryHelper) {
        this.client = client;
        this.inviteTransactionSummaryHelper = inviteTransactionSummaryHelper;
    }

    @Override
    public void run() {
        Response response = client.getReceivedInvites();
        if (response.isSuccessful()) {
            writeInvitesToDatabase((List<ReceivedInvite>) response.body());
        } else {
            logError(response);
        }
    }

    private void writeInvitesToDatabase(List<ReceivedInvite> receivedInvites) {
        for (ReceivedInvite invite : receivedInvites) {
            inviteTransactionSummaryHelper.saveReceivedInviteTransaction(invite);
        }
    }

    private void logError(Response response) {
        Log.d(TAG, "|---- Received Invite failed");
        Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
        try {
            Log.d(TAG, "|--------- message: " + response.errorBody().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
