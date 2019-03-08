package com.coinninja.coinkeeper.cn.dropbit;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.model.db.InviteTransactionSummary;
import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class DropBitCancellationManagerTest {

    @Mock
    CNWalletManager cnWalletManager;

    @Mock
    private TransactionHelper transactionHelper;

    @Mock
    private SignedCoinKeeperApiClient client;

    @InjectMocks
    private DropBitCancellationManager service;
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
            "    \"user_id\": \"ad983e63-526d-4679-a682-c4ab052b20e1\",\n" +
            "    \"wallet_id\": \"f8e8c20e-ba44-4bac-9a96-44f3b7ae955d\"\n" +
            "  }\n" +
            "]";

    @After
    public void teardown() {
        service = null;
        cnWalletManager = null;
        transactionHelper = null;
        client = null;
    }

    @Test
    public void markUnfulfilledAsCanceled() {
        List<InviteTransactionSummary> sampleInvitesList = new ArrayList<>();
        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary tempInvite2 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn("some invite id 1");
        when(tempInvite2.getServerId()).thenReturn("some invite id 2");

        sampleInvitesList.add(tempInvite1);
        sampleInvitesList.add(tempInvite2);

        when(transactionHelper.gatherUnfulfilledInviteTrans()).thenReturn(sampleInvitesList);

        Response response = getResponse(sentInviteJSON);

        when(client.updateInviteStatusCanceled(anyString())).thenReturn(response);

        service.markUnfulfilledAsCanceled();

        verify(transactionHelper).updateInviteAsCanceled("some invite id 1");
        verify(transactionHelper).updateInviteAsCanceled("some invite id 2");
        verify(cnWalletManager, atLeast(1)).updateBalances();
    }

    @Test
    public void markAsCanceled() {
        List<InviteTransactionSummary> sampleInvitesList = new ArrayList<>();
        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary tempInvite2 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn("some invite id 1");
        when(tempInvite2.getServerId()).thenReturn("some invite id 2");

        sampleInvitesList.add(tempInvite1);
        sampleInvitesList.add(tempInvite2);
        Response response = getResponse(sentInviteJSON);

        when(client.updateInviteStatusCanceled(anyString())).thenReturn(response);

        service.markAsCanceled(sampleInvitesList);

        verify(transactionHelper).updateInviteAsCanceled("some invite id 1");
        verify(transactionHelper).updateInviteAsCanceled("some invite id 2");

    }

    @Test
    public void markAsCanceled_server_error() {
        List<InviteTransactionSummary> sampleInvitesList = new ArrayList<>();
        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        InviteTransactionSummary tempInvite2 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn("some invite id 1");
        when(tempInvite2.getServerId()).thenReturn("some invite id 2");

        sampleInvitesList.add(tempInvite1);
        sampleInvitesList.add(tempInvite2);
        Response badResponse = getBadResponse();

        when(client.updateInviteStatusCanceled(anyString())).thenReturn(badResponse);

        service.markAsCanceled(sampleInvitesList);

        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 1");
        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 2");

    }

    @Test
    public void markAsCanceled_single() {
        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn("some invite id 1");
        when(transactionHelper.getInviteTransactionSummary("some invite id 1")).thenReturn(tempInvite1);

        Response response = getResponse(sentInviteJSON);

        when(client.updateInviteStatusCanceled(anyString())).thenReturn(response);

        service.markAsCanceled(tempInvite1);

        verify(transactionHelper).updateInviteAsCanceled("some invite id 1");
        verify(cnWalletManager, atLeast(1)).updateBalances();
    }

    @Test
    public void markAsCanceled_single_server_error() {
        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn("some invite id 1");

        Response badResponse = getBadResponse();

        when(client.updateInviteStatusCanceled(anyString())).thenReturn(badResponse);

        service.markAsCanceled(tempInvite1);

        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 1");
    }

    @Test
    public void markAsCanceled_string() {
        String serverInviteId = "some invite id 1";

        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn(serverInviteId);

        Response response = getResponse(sentInviteJSON);

        when(transactionHelper.getInviteTransactionSummary(serverInviteId)).thenReturn(tempInvite1);
        when(client.updateInviteStatusCanceled(anyString())).thenReturn(response);

        service.markAsCanceled(serverInviteId);

        verify(transactionHelper).updateInviteAsCanceled(serverInviteId);
    }

    @Test
    public void markAsCanceled_string_server_error() {
        String serverInviteId = "some invite id 1";

        InviteTransactionSummary tempInvite1 = mock(InviteTransactionSummary.class);
        when(tempInvite1.getServerId()).thenReturn(serverInviteId);

        Response badResponse = getBadResponse();
        when(transactionHelper.getInviteTransactionSummary(serverInviteId)).thenReturn(tempInvite1);
        when(client.updateInviteStatusCanceled(anyString())).thenReturn(badResponse);

        service.markAsCanceled(tempInvite1);

        verify(transactionHelper, times(0)).updateInviteAsCanceled(serverInviteId);
    }

    @Test
    public void markAsCanceled_string_null() {
        String serverInviteId = "some invite id 1";

        Response response = getResponse(sentInviteJSON);

        when(transactionHelper.getInviteTransactionSummary(serverInviteId)).thenReturn(null);
        when(client.updateInviteStatusCanceled(anyString())).thenReturn(response);

        service.markAsCanceled(serverInviteId);

        verify(transactionHelper, times(0)).updateInviteAsCanceled(serverInviteId);
    }

    @Test
    public void markAsCanceled_emptyList() {
        List<InviteTransactionSummary> sampleInvitesList = new ArrayList<>();

        service.markAsCanceled(sampleInvitesList);

        verify(client, times(0)).updateInviteStatusCanceled(anyString());
        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 1");
        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 2");

    }

    @Test
    public void markAsCanceled_null() {

        service.markAsCanceled((List) null);

        verify(client, times(0)).updateInviteStatusCanceled(anyString());
        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 1");
        verify(transactionHelper, times(0)).updateInviteAsCanceled("some invite id 2");

    }

    private Response getBadResponse() {
        return Response.error(400, ResponseBody.create(MediaType.parse("application/json"),
                "[]"));
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


}