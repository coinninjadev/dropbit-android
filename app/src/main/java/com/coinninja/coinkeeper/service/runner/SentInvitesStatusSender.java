package com.coinninja.coinkeeper.service.runner;

import android.util.Log;

import com.coinninja.coinkeeper.model.db.BroadcastBtcInvite;
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;

import java.util.List;

import javax.inject.Inject;

import retrofit2.Response;


public class SentInvitesStatusSender implements Runnable {
    private static final String TAG = SentInvitesStatusSender.class.getSimpleName();

    private final SignedCoinKeeperApiClient client;
    private final BroadcastBtcInviteHelper broadcastBtcInviteHelper;

    @Inject
    public SentInvitesStatusSender(SignedCoinKeeperApiClient client, BroadcastBtcInviteHelper broadcastBtcInviteHelper) {
        this.client = client;
        this.broadcastBtcInviteHelper = broadcastBtcInviteHelper;
    }

    @Override
    public void run() {
        List<BroadcastBtcInvite> btcInvites = broadcastBtcInviteHelper.getBroadcastInvites();
        for (BroadcastBtcInvite btcInvite : btcInvites) {

            Response response;

            switch (btcInvite.getBtcState()) {
                case FULFILLED:
                    response = postStatusCompletedToServer(btcInvite);
                    break;
                case CANCELED:
                    response = postStatusCanceledToServer(btcInvite);
                    break;
                default:
                    response = unknownBTCState(btcInvite);
            }

            if (response.isSuccessful()) {
                broadcastBtcInviteHelper.removeBtcInvite(btcInvite);
            } else {
                Log.d(TAG, "|---- Invite Status update failed");
                Log.d(TAG, "|------ statusCode: " + String.valueOf(response.code()));
                Log.d(TAG, "|--------- message: " + response.body());
            }
        }

    }

    private Response unknownBTCState(BroadcastBtcInvite btcInvite) {
        String errorMessage = "Unknown BTC State = " + btcInvite.getBtcState().name();
        return client.createUpdateInviteStatusError(btcInvite.getInviteServerID(), errorMessage);
    }

    private Response postStatusCanceledToServer(BroadcastBtcInvite btcInvite) {
        return client.updateInviteStatusCanceled(btcInvite.getInviteServerID());
    }


    private Response postStatusCompletedToServer(BroadcastBtcInvite btcInvite) {
        return client.updateInviteStatusCompleted(btcInvite.getInviteServerID(), btcInvite.getBroadcastTxID());
    }
}
