package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.TransactionStats;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class CoinKeeperApiClientTest {

    private CoinKeeperApiClient apiClient;

    private MockWebServer server;

    private CoinKeeperApiClient createClient(String host) {
        CoinKeeperClient client = new Retrofit.Builder().
                baseUrl(host).
                client(new OkHttpClient.Builder()
                        .build())
                .addConverterFactory(GsonConverterFactory.create()).
                        build().create(CoinKeeperClient.class);

        return new CoinKeeperApiClient(client);
    }

    @Before
    public void setUp() {
        server = new MockWebServer();
        apiClient = createClient(server.url("").toString());
    }

    @After
    public void tearDown() {
        try {
            server.shutdown();
        } catch (Exception e) {

        }
    }

    @Test
    public void exposes_health_check_api_call() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\n" + "" +
                "\"message\": \"OK\"\n" + "}"));

        Response response = apiClient.checkHealth();

        assertTrue(response.isSuccessful());
    }

    @Test
    public void fetches_current_state_information() {
        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody(MockAPIData.PRICING));

        Response response = apiClient.getCurrentState();

        CurrentState state = (CurrentState) response.body();
        assertThat(state.getBlockheight(), equalTo(518631));
        assertThat(state.getFees().getMin(), equalTo(40.1));
        assertThat(state.getFees().getAvg(), equalTo(20.1));
        assertThat(state.getFees().getMax(), equalTo(10.0));
        assertThat(state.getLatestPrice().toFormattedCurrency(), equalTo("$418.66"));
    }

    @Test
    public void itFetchesStatsForATransaction() {

        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody(MockAPIData.TRANSACTION_STATS));

        String transactionId = "7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03";
        Response response = apiClient.getTransactionStats(transactionId);

        TransactionStats transactionStats = (TransactionStats) response.body();

        assertThat(transactionStats.getTransactionId(), equalTo("7f3a2790d59853fdc620b8cd23c8f68158f8bbdcd337a5f2451620d6f76d4e03"));
        assertThat(transactionStats.isCoinBase(), equalTo(false));
        assertThat(transactionStats.getFeesRate(), equalTo(1015821L));
        assertThat(transactionStats.getFees(), equalTo(170658L));
        assertThat(transactionStats.getMiner(), equalTo("GBMiners"));
        assertThat(transactionStats.getVinValue(), equalTo(180208833L));
        assertThat(transactionStats.getVoutValue(), equalTo(179858833L));
    }

    @Test
    public void sends_expected_contract_for_multiple_transactions() {
        ArgumentCaptor<JsonObject> argumentCaptor = ArgumentCaptor.forClass(JsonObject.class);

        String[] txids = {"---txid--1---", "---txid--2---"};
        CoinKeeperClient client = mock(CoinKeeperClient.class);
        when(client.queryTransactions(Matchers.any(JsonObject.class))).thenReturn(mock(Call.class));
        CoinKeeperApiClient apiClient = new CoinKeeperApiClient(client);

        apiClient.getTransactions(txids);

        verify(client).queryTransactions(argumentCaptor.capture());

        JsonArray jsonArray = argumentCaptor.getValue().getAsJsonObject("query").
                getAsJsonObject("terms").
                getAsJsonArray("txid");

        Gson gson = new Gson();
        String[] strings = gson.fromJson(jsonArray, String[].class);

        assertThat(strings, equalTo(txids));
    }

    @Test
    public void sends_expected_contract_when_quering_for_addresses() {
        ArgumentCaptor<JsonObject> argumentCaptor = ArgumentCaptor.forClass(JsonObject.class);

        String[] inAddresses = {"1Gy2Ast7uT13wQByPKs9Vi9Qj1BVcARgVQ", "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX"};
        CoinKeeperClient client = mock(CoinKeeperClient.class);
        when(client.getAddresses(Matchers.any(JsonObject.class), anyInt(), anyInt())).thenReturn(mock(Call.class));
        CoinKeeperApiClient apiClient = new CoinKeeperApiClient(client);

        apiClient.queryAddressesFor(inAddresses, 1);

        verify(client).getAddresses(argumentCaptor.capture(), eq(1), anyInt());

        JsonArray jsonArray = argumentCaptor.getValue().getAsJsonObject("query").
                getAsJsonObject("terms").
                getAsJsonArray("address");

        Gson gson = new Gson();
        String[] strings = gson.fromJson(jsonArray, String[].class);

        assertThat(strings, equalTo(inAddresses));
    }

    @Test
    public void gets_historical_pricing_for_transaction_id() throws InterruptedException {
        String JSON = "{\n" +
                "\"time\": \"2016-05-17 00:00:00\",\n" +
                "\"average\": 457.91\n" +
                "}";

        server.enqueue(new MockResponse().setResponseCode(200).setBody(JSON));
        String txid = "--txid--";

        Response response = apiClient.getHistoricPrice(txid);
        CNPricing pricing = (CNPricing) response.body();
        assertThat(pricing.getAverage(), equalTo(45791L));
        assertThat(server.takeRequest().getPath(), equalTo(String.format("/pricing/%s", txid)));
    }

    @Test
    public void queries_multiple_addresses_in_blocks() {
        String[] inAddresses = {"1Gy2Ast7uT13wQByPKs9Vi9Qj1BVcARgVQ", "3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX"};

        server.enqueue(new MockResponse().setResponseCode(200)
                .setBody(MockAPIData.ADDRESS_QUERY_RESPONSE__PAGE_1));

        Response response = apiClient.queryAddressesFor(inAddresses, 1);

        List<GsonAddress> addresses = (List<GsonAddress>) response.body();
        assertThat(addresses.size(), equalTo(3));
        assertThat(addresses.get(0).getAddress(), equalTo("1Gy2Ast7uT13wQByPKs9Vi9Qj1BVcARgVQ"));
        assertThat(addresses.get(1).getAddress(), equalTo("3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX"));
        assertThat(addresses.get(2).getAddress(), equalTo("3PxEH5t91Cio4B7LCZCEWQEGGxaqGW5HkX"));
    }
}