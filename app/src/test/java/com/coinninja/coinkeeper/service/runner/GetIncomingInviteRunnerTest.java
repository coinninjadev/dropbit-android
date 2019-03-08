package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.helpers.TransactionHelper;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.model.ReceivedInvite;

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetIncomingInviteRunnerTest {

    private List<ReceivedInvite> testData;
    @Mock
    private SignedCoinKeeperApiClient apiClient;
    @Mock
    private WalletHelper walletHelper;
    @Mock
    private TransactionHelper transactionHelper;
    @InjectMocks
    private GetIncomingInviteRunner runner;

    @After
    public void tearDown() {
        runner = null;
        walletHelper = null;
        transactionHelper = null;
        apiClient = null;
        testData = null;
    }

    @Test
    public void writes_two_invites_to_database_test() {
        testData = new ArrayList<>();
        testData.add(new ReceivedInvite());
        testData.add(new ReceivedInvite());
        Response response = getResponse(testData);
        when(apiClient.getReceivedInvites()).thenReturn(response);

        runner.run();

        verify(transactionHelper, times(2)).saveReceivedInviteTransaction(any(), any());
    }

    @Test
    public void server_fail_test() {
        Response response = getBadResponse();
        when(apiClient.getReceivedInvites()).thenReturn(response);

        runner.run();

        verify(transactionHelper, times(0)).saveReceivedInviteTransaction(any(), any());
    }

    @Test
    public void server_good_but_has_no_data_test() {
        testData = new ArrayList<>();
        Response response = getResponse(testData);
        when(apiClient.getReceivedInvites()).thenReturn(response);

        runner.run();

        verify(transactionHelper, times(0)).saveReceivedInviteTransaction(any(), any());
    }

    @Test
    public void update_balance_test() {
        testData = new ArrayList<>();
        Response response = getResponse(testData);
        when(apiClient.getReceivedInvites()).thenReturn(response);

        runner.run();

        verify(walletHelper).updateBalances();
        verify(walletHelper).updateSpendableBalances();
    }

    @NonNull
    private Response getResponse(Object responseData) {
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