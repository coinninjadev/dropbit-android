package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.helpers.InviteTransactionSummaryHelper;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReceivedInvitesStatusRunnerTest {
    private String fulfilledReceivedRequest = "[\n" +
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
            "    \"status\": \"completed\",\n" +
            "    \"txid\": \"7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03\",\n" +
            "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
            "  }\n" +
            "]";
    private String un_fulfilledReceivedRequest = "[\n" +
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
            "    \"txid\": \"\",\n" +
            "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
            "  }\n" +
            "]";
    @Mock
    CNLogger logger;
    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private InviteTransactionSummaryHelper inviteTransactionSummaryHelper;
    @Mock
    private TransactionHelper transactionHelper;
    @Mock
    private Analytics analytics;
    @InjectMocks
    private ReceivedInvitesStatusRunner receivedInvitesStatusRunner;

    @After
    public void tearDown() {
        apiClient = null;
        transactionHelper = null;
        receivedInvitesStatusRunner = null;
    }

    @Test
    public void updates_user_profile_to_account_for_dropbit_receive() {
        when(apiClient.getReceivedInvites()).thenReturn(getResponse(fulfilledReceivedRequest));

        receivedInvitesStatusRunner.run();

        verify(analytics).setUserProperty(Analytics.PROPERTY_HAS_RECEIVED_DROPBIT, true);
    }

    @Test
    public void save_completed_invite_test() {
        when(apiClient.getReceivedInvites()).thenReturn(getResponse(fulfilledReceivedRequest));

        receivedInvitesStatusRunner.run();

        verify(inviteTransactionSummaryHelper).updateFulfilledInviteByCnId("a1bb1d88-bfc8-4085-8966-e0062278237c", "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03");
    }

    @Test
    public void not_save_uncompleted_invite_test() {
        when(apiClient.getReceivedInvites()).thenReturn(getResponse(un_fulfilledReceivedRequest));


        receivedInvitesStatusRunner.run();

        verify(inviteTransactionSummaryHelper, times(0)).updateFulfilledInviteByCnId(anyString(), anyString());
    }

    @Test
    public void not_save_bad_response_from_server_invite_test() {
        Response response = getBadResponse();
        when(apiClient.getReceivedInvites()).thenReturn(response);

        receivedInvitesStatusRunner.run();

        verify(inviteTransactionSummaryHelper, times(0)).updateFulfilledInviteByCnId(anyString(), anyString());
        verify(logger).logError(ReceivedInvitesStatusRunner.TAG, ReceivedInvitesStatusRunner.RECEIVED_INVITE_FAILED, response);
    }

    @Test
    public void received_invite_is_completed_return_tx_id_test() {
        String sampleTxID = "sample tx id";
        String sampleStatus = "completed";

        ReceivedInvite invite = mock(ReceivedInvite.class);
        when(invite.getStatus()).thenReturn(sampleStatus);
        when(invite.getTxid()).thenReturn(sampleTxID);

        String txID = receivedInvitesStatusRunner.getCompletedTxID(invite);


        assertThat(txID, equalTo(sampleTxID));
    }

    @Test
    public void received_invite_is_waiting_for_a_tx_id_return_null_test() {
        String sampleStatus = "waiting";

        ReceivedInvite invite = mock(ReceivedInvite.class);
        when(invite.getStatus()).thenReturn(sampleStatus);

        String txID = receivedInvitesStatusRunner.getCompletedTxID(invite);


        assertThat(txID, nullValue());
    }

    @Test
    public void received_invite_is_null_return_null_without_crashing_test() {
        ReceivedInvite invite = mock(ReceivedInvite.class);
        when(invite.getStatus()).thenReturn(null);

        String txID = receivedInvitesStatusRunner.getCompletedTxID(invite);


        assertThat(txID, nullValue());
    }

    private Response getResponse(String jsonArray) {
        return Response.success(new Gson().fromJson(jsonArray, new TypeToken<List<ReceivedInvite>>() {
        }.getType()), new okhttp3.Response.Builder()
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