package com.coinninja.coinkeeper.service.runner;

import com.coinninja.coinkeeper.model.db.TransactionSummary;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperClient;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.TransactionConfirmation;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.coinninja.coinkeeper.util.CNLogger;
import com.coinninja.coinkeeper.wallet.data.TestData;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import edu.emory.mathcs.backport.java.util.Arrays;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionAPIUtilTest {

    @Mock
    CoinKeeperApiClient apiClient;
    @Mock
    CNLogger cnLogger;


    TransactionAPIUtil apiUtil;
    private List<TransactionSummary> transactions;
    private Response txResponse1;
    private Response txResponse2;
    private String transactionOneId = "1d1ef96bc636952cc01d7d613df41caf7815e6766670bafa5d096bacf843fd24";
    private String transactionTwoId = "9cbe0f300ffa3581f729c5f2a35686a14643ac4d20cb2f7018a173736ac40c57";
    private Response error;
    private Response txStatsResponse2;
    private Response txStatsResponse1;
    private Response txConfirmationResponse2;
    private Response txConfirmationResponse1;
    private String[] transactionIds;

    private String[] hunk1;
    private String[] hunk2;
    private String[] hunk3;

    @Before
    public void setUp() {
        transactionIds = new String[2];
        apiUtil = new TransactionAPIUtil(apiClient, cnLogger);
        transactions = new ArrayList<>();

        TransactionSummary t1 = new TransactionSummary();
        t1.setTxid(transactionOneId);
        transactions.add(t1);
        transactionIds[0] = t1.getTxid();

        TransactionSummary t2 = new TransactionSummary();
        t2.setTxid(transactionTwoId);
        transactions.add(t2);
        transactionIds[1] = t2.getTxid();

        txResponse1 = buildTransactionResponse(TestData.TRANSACTIONS_ONE);
        txResponse2 = buildTransactionResponse(TestData.TRANSACTIONS_TWO);

        error = Response.error(ResponseBody.create(null, ""), new okhttp3.Response.Builder()
                .code(404)
                .message("")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("https://someuri.com").build())
                .build());

        txStatsResponse1 = buildTransactionStatsResponse(TestData.TRANSACTION_ONE_STATS);
        txStatsResponse2 = buildTransactionStatsResponse(TestData.TRANSACTION_TWO_STATS);

        txConfirmationResponse1 = buildTransactionConfirmationResponse(TestData.TRANSACTION_ONE_CONFIRMATION);
        txConfirmationResponse2 = buildTransactionConfirmationResponse(TestData.TRANSACTION_ONE_CONFIRMATION);

    }

    private Response buildTransactionResponse(String data) {
        TransactionDetail block = new Gson().fromJson(data, new TypeToken<TransactionDetail>() {
        }.getType());
        return Response.success(block);
    }

    private Response buildTransactionStatsResponse(String data) {
        TransactionStats block = new Gson().fromJson(data, new TypeToken<TransactionStats>() {
        }.getType());
        return Response.success(block);
    }

    private Response buildTransactionConfirmationResponse(String data) {
        TransactionConfirmation block = new Gson().fromJson(data, new TypeToken<TransactionConfirmation>() {
        }.getType());
        return Response.success(block);
    }

    @Test
    public void can_merge_null_resposne() {
        List<TransactionSummary> transactions = new ArrayList<>();
        TransactionSummary transaction = new TransactionSummary();
        transaction.setTxid("--foo--");
        transactions.add(transaction);
        Response response = Response.success(null);
        when(apiClient.getTransactions(any())).thenReturn(response);

        List<TransactionDetail> transactionDetails = apiUtil.fetchPartialTransactions(transactions);

        assertThat(transactionDetails, equalTo(new ArrayList<TransactionDetail>()));
    }

    @Test
    public void does_fetch_historic_pricing_from_client() {
        transactions.clear();
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        transactions.add(transactionSummary);
        when(transactionSummary.getTxid()).thenReturn(transactionOneId);
        CNPricing pricing = new CNPricing();
        pricing.setAverage(new BigDecimal(300.00));
        when(apiClient.getHistoricPrice(transactionOneId)).thenReturn(Response.success(pricing));

        apiUtil.updateHistoricPricingIfNecessary(transactions);

        verify(apiClient, times(1)).getHistoricPrice(transactionOneId);
        verify(transactionSummary).setHistoricPrice(30000L);
        verify(transactionSummary).update();
    }

    @Test
    public void does_not_save_unexpected_data_fetch_historic_pricing_from_client() {
        transactions.clear();
        TransactionSummary transactionSummary = mock(TransactionSummary.class);
        transactions.add(transactionSummary);
        when(transactionSummary.getTxid()).thenReturn(transactionOneId);
        CNPricing pricing = new CNPricing();
        pricing.setAverage(new BigDecimal(0.01));
        when(apiClient.getHistoricPrice(transactionOneId)).thenReturn(Response.success(pricing));

        apiUtil.updateHistoricPricingIfNecessary(transactions);

        verify(apiClient, times(1)).getHistoricPrice(transactionOneId);
        verify(transactionSummary, times(0)).setHistoricPrice(anyLong());
        verify(transactionSummary, times(0)).update();
    }

    @Test
    public void does_not_fetch_transactions_when_none_are_provided() {
        transactions.clear();

        apiUtil.fetchPartialTransactions(transactions);

        verify(apiClient, times(0)).getTransactions(any(String[].class));
    }

    @Test
    public void hunks_transactions() {
        mock55transactions();

        List<TransactionDetail> transactionDetails = apiUtil.fetchPartialTransactions(transactions);

        verify(apiClient).getTransactions(hunk1);
        verify(apiClient).getTransactions(hunk2);
        verify(apiClient).getTransactions(hunk3);

        assertThat(transactionDetails.size(), equalTo(transactions.size()));
    }

    @Test
    public void fetches_transactions_in_batches() {
        List<TransactionDetail> expectedResposne = new ArrayList<>();
        expectedResposne.add((TransactionDetail) txResponse1.body());
        expectedResposne.add((TransactionDetail) txResponse2.body());
        Response response = Response.success(expectedResposne);

        when(apiClient.getTransactions(transactionIds)).thenReturn(response);

        List<TransactionDetail> transactionDetails = apiUtil.fetchPartialTransactions(transactions);

        assertThat(transactionDetails, equalTo(expectedResposne));
    }

    @Test
    public void returns_empty_transaction_list_when_not_successfull() {
        ResponseBody body = ResponseBody.create(MediaType.parse("application/json"),
                "[]");
        Response response = Response.error(404, body);

        when(apiClient.getTransactions(transactionIds)).thenReturn(response);

        List<TransactionDetail> transactionDetails = apiUtil.fetchPartialTransactions(transactions);

        verify(apiClient, times(1)).getTransactions(transactionIds);
        assertThat(transactionDetails.size(), equalTo(0));
    }

    @Test
    public void fetchFeeInformation() {
        TransactionSummary transaction = mock(TransactionSummary.class);
        TransactionStats stat = new TransactionStats();
        Response response = Response.success(200, stat);
        when(transaction.getTxid()).thenReturn(transactionOneId);
        when(apiClient.getTransactionStats(transactionOneId)).thenReturn(response);

        TransactionStats transactionStat = apiUtil.fetchFeesFor(transaction);

        verify(apiClient).getTransactionStats(any());
        assertThat(transactionStat, equalTo(stat));
    }

    @Test
    public void returns_null_for_unsuccessful_requests() {
        TransactionSummary transaction = mock(TransactionSummary.class);
        when(transaction.getTxid()).thenReturn(transactionOneId);
        when(apiClient.getTransactionStats(transactionOneId)).thenReturn(error);

        assertNull(apiUtil.fetchFeesFor(transaction));
    }

    private void mock55transactions() {
        hunk1 = buildTransactions("txid batch 1 -- ", CoinKeeperClient.TRANSACTIONS_TO_QUERY_AT_A_TIME);
        hunk2 = buildTransactions("txid batch 2 -- ", CoinKeeperClient.TRANSACTIONS_TO_QUERY_AT_A_TIME);
        hunk3 = buildTransactions("txid batch 3 -- ", 7);

        Response res1 = Response.success(buildTransactionResposne(hunk1));
        when(apiClient.getTransactions(hunk1)).thenReturn(res1);

        Response res2 = Response.success(buildTransactionResposne(hunk2));
        when(apiClient.getTransactions(hunk2)).thenReturn(res2);

        Response res3 = Response.success(buildTransactionResposne(hunk3));
        when(apiClient.getTransactions(hunk3)).thenReturn(res3);

        List<String> feed = new ArrayList<>();
        feed.addAll(Arrays.asList(hunk1));
        feed.addAll(Arrays.asList(hunk2));
        feed.addAll(Arrays.asList(hunk3));

        transactions.clear();
        TransactionSummary txSummary;
        for (String txid : feed) {
            txSummary = new TransactionSummary();
            txSummary.setTxid(txid);
            transactions.add(txSummary);
        }
    }

    private List<TransactionDetail> buildTransactionResposne(String[] hunk) {
        List<TransactionDetail> details = new ArrayList<>();

        TransactionDetail detail;

        for (String txid : hunk) {
            detail = new TransactionDetail();
            detail.setTxid(txid);
            details.add(detail);
        }

        return details;
    }

    private String[] buildTransactions(String label, int transactionsToQueryAtATime) {
        String[] txids = new String[transactionsToQueryAtATime];

        for (int i = 0; i < transactionsToQueryAtATime; i++) {
            txids[i] = label + String.valueOf(i);
        }

        return txids;
    }
}