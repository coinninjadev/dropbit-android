package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.db.BroadcastBtcInvite;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.db.enums.BTCState;
import com.coinninja.coinkeeper.model.helpers.BroadcastBtcInviteHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SentInvitesStatusSenderTest {
    private SignedCoinKeeperApiClient client;
    private BroadcastBtcInviteHelper broadcastBtcInviteHelper;

    private SentInvitesStatusSender sentInvitesStatusSender;
    private String sentInviteJSON = "[\n" +
            "  {\n" +
            "    \"id\": \"a1bb1d88-bfc8-4085-8966-e0062278237c\",\n" +
            "    \"created_at\": 1531921356,\n" +
            "    \"updated_at\": 1531921356,\n" +
            "    \"address\": \"1JbJbAkCXtxpko39nby44hpPenpC1xKGYw\",\n" +
            "    \"metadata\": {\n" +
            "      \"amount\": {\n" +
            "        \"btc\": 120000000,\n" +
            "        \"usd\": 8292280\n" +
            "      },\n" +
            "      \"sender\": {\n" +
            "        \"country_code\": 1,\n" +
            "        \"phone_number\": \"5554441234\"\n" +
            "      },\n" +
            "      \"receiver\": {\n" +
            "        \"country_code\": 1,\n" +
            "        \"phone_number\": \"5554441234\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"phone_number_hash\": \"498803d5964adce8037d2c53da0c7c7a96ce0e0f99ab99e9905f0dda59fb2e49\",\n" +
            "    \"request_ttl\": 1531921356,\n" +
            "    \"status\": \"new\",\n" +
            "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
            "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
            "  }\n" +
            "]";

    @Before
    public void setUp() throws Exception {
        client = mock(SignedCoinKeeperApiClient.class);
        broadcastBtcInviteHelper = mock(BroadcastBtcInviteHelper.class);

        sentInvitesStatusSender = new SentInvitesStatusSender(client, broadcastBtcInviteHelper);

    }

    @Test
    public void update_sent_invite_status_send_tx_id_test() {
        String inviteID = "sample invite ID";
        String txID = "sample tx id";
        BroadcastBtcInvite btcInvite = mock(BroadcastBtcInvite.class);
        when(btcInvite.getBroadcastTxID()).thenReturn(txID);
        when(btcInvite.getInviteServerID()).thenReturn(inviteID);
        InviteTransactionSummary inviteSummary = mock(InviteTransactionSummary.class);
        when(btcInvite.getInviteTransactionSummary()).thenReturn(inviteSummary);
        when(btcInvite.getBtcState()).thenReturn(BTCState.FULFILLED);
        when(inviteSummary.getBtcTransactionId()).thenReturn(txID);

        List<BroadcastBtcInvite> btcInvites = new ArrayList<>();
        btcInvites.add(btcInvite);


        when(broadcastBtcInviteHelper.getBroadcastInvites()).thenReturn(btcInvites);
        Response response = getResponse(sentInviteJSON);
        when(client.updateInviteStatusCompleted(inviteID, txID)).thenReturn(response);


        sentInvitesStatusSender.run();

        verify(broadcastBtcInviteHelper).removeBtcInvite(btcInvite);
        verify(btcInvite).getBroadcastTxID();
        verify(inviteSummary, times(0)).getBtcTransactionId();
    }


    @Test
    public void update_sent_invite_status_send_tx_id_but_get_bad_response() {

        String inviteID = "sample invite ID";
        String txID = "sample tx id";
        BroadcastBtcInvite btcInvite = mock(BroadcastBtcInvite.class);
        when(btcInvite.getBroadcastTxID()).thenReturn(txID);
        when(btcInvite.getInviteServerID()).thenReturn(inviteID);
        InviteTransactionSummary inviteSummary = mock(InviteTransactionSummary.class);
        when(btcInvite.getInviteTransactionSummary()).thenReturn(inviteSummary);
        when(btcInvite.getBtcState()).thenReturn(BTCState.FULFILLED);
        when(inviteSummary.getBtcTransactionId()).thenReturn(txID);
        List<BroadcastBtcInvite> btcInvites = new ArrayList<>();
        btcInvites.add(btcInvite);


        when(broadcastBtcInviteHelper.getBroadcastInvites()).thenReturn(btcInvites);
        Response response = getBadResponse();
        when(client.updateInviteStatusCompleted(inviteID, txID)).thenReturn(response);


        sentInvitesStatusSender.run();

        verify(broadcastBtcInviteHelper, times(0)).removeBtcInvite(btcInvite);
        verify(btcInvite).getBroadcastTxID();
        verify(inviteSummary, times(0)).getBtcTransactionId();
    }

    @Test
    public void if_btcInvite_is_canceled_notify_server_and_delete_entry() {

        String inviteID = "sample invite ID";
        String txID = "sample tx id";
        BroadcastBtcInvite btcInvite = mock(BroadcastBtcInvite.class);
        when(btcInvite.getBroadcastTxID()).thenReturn(txID);
        when(btcInvite.getInviteServerID()).thenReturn(inviteID);
        InviteTransactionSummary inviteSummary = mock(InviteTransactionSummary.class);
        when(btcInvite.getInviteTransactionSummary()).thenReturn(inviteSummary);
        when(btcInvite.getBtcState()).thenReturn(BTCState.CANCELED);
        when(inviteSummary.getBtcTransactionId()).thenReturn(txID);

        List<BroadcastBtcInvite> btcInvites = new ArrayList<>();
        btcInvites.add(btcInvite);


        when(broadcastBtcInviteHelper.getBroadcastInvites()).thenReturn(btcInvites);
        Response response = getResponse(sentInviteJSON);
        when(client.updateInviteStatusCanceled(inviteID)).thenReturn(response);


        sentInvitesStatusSender.run();

        verify(broadcastBtcInviteHelper).removeBtcInvite(btcInvite);
        verify(client, times(1)).updateInviteStatusCanceled(inviteID);
        verify(client, never()).updateInviteStatusCompleted(anyString(), anyString());
    }

    @Test
    public void update_sent_invite_status_send_tx_using_tad_from_failed_broadcast_test() {
        String inviteID = "sample invite ID";
        String txID = "sample tx id";
        String wrongTxID = "FAILED_TO_BROADCAST_TIME_sample tx id";

        BroadcastBtcInvite btcInvite = mock(BroadcastBtcInvite.class);
        when(btcInvite.getBroadcastTxID()).thenReturn(txID);
        when(btcInvite.getInviteServerID()).thenReturn(inviteID);
        InviteTransactionSummary inviteSummary = mock(InviteTransactionSummary.class);
        when(btcInvite.getInviteTransactionSummary()).thenReturn(inviteSummary);
        when(btcInvite.getBtcState()).thenReturn(BTCState.FULFILLED);
        when(inviteSummary.getBtcTransactionId()).thenReturn(wrongTxID);

        List<BroadcastBtcInvite> btcInvites = new ArrayList<>();
        btcInvites.add(btcInvite);


        when(broadcastBtcInviteHelper.getBroadcastInvites()).thenReturn(btcInvites);
        Response response = getResponse(sentInviteJSON);
        when(client.updateInviteStatusCompleted(inviteID, txID)).thenReturn(response);


        sentInvitesStatusSender.run();

        verify(broadcastBtcInviteHelper).removeBtcInvite(btcInvite);
        verify(btcInvite).getBroadcastTxID();
        verify(inviteSummary, times(0)).getBtcTransactionId();
    }

    @NonNull
    private Response getResponse(String responseData) {
        return Response.success(responseData, new okhttp3.Response.Builder()
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }


    private Response getBadResponse() {
        return Response.error(400, ResponseBody.create(MediaType.parse("application/json"),
                "[]"));
    }
}