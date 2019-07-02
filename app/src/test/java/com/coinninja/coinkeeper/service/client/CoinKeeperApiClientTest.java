package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.model.CNPricing;
import com.coinninja.coinkeeper.service.client.model.GsonAddress;
import com.coinninja.coinkeeper.service.client.model.TransactionDetail;
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
import static org.hamcrest.MatcherAssert.assertThat;
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
    public void order_of_tx_resposne() {
        String json = "[{\"hash\":\"82a2d283543af94a31ba835bc95c2049e2bb4fb75a91b79a94c137c5e40ae2ee\",\"txid\":\"82a2d283543af94a31ba835bc95c2049e2bb4fb75a91b79a94c137c5e40ae2ee\"," +
                "\"size\":247,\"vsize\":166,\"weight\":661,\"version\":1,\"locktime\":0,\"coinbase\":false,\"blockhash\":\"0000000000000000002b47fa958ff8cd3d7e865ef08cb2230b1805fbddadd300\"" +
                ",\"blockheight\":562165,\"height\":103,\"time\":1549642028," +
                "\"received_time\":1549642028,\"blocktime\":1549642028,\"vin\":" +
                "[{\"txid\":\"11fb6b149f6952d2ec2c643ebddd0a08cade890e2818c85e6ae1db43b43f714a\",\"vout\":1,\"scriptSig\":" +
                "{\"asm\":\"00144a95b3c08579531f841a8aee18d2d5c31c1bb6a6\",\"hex\":\"1600144a95b3c08579531f841a8aee18d2d5c31c1bb6a6\"}," +
                "\"txinwitness\":[\"304402201149422cd5906f69daf3bd8f78fc06ae9bf8c8bf2fc3b56ca641d79ca044e4fe022001dbe82065943ea0dc75b5e956a0f5f2fe060993803c51ccb5e71637b46c4a4701\",\"03befc7e3f868f4cd123badabb8df0ae49b1d45e221c39d8cd9f56fdaadc035d5a\"],\"sequence\":4294967295,\"previousoutput\":{\"value\":1020110,\"n\":1,\"scriptPubKey\":{\"asm\":\"OP_HASH160 8f98f8a5e5110306c89fd04d8fd877bdcb135406 OP_EQUAL\",\"hex\":\"a9148f98f8a5e5110306c89fd04d8fd877bdcb13540687\",\"reqsigs\":1,\"type\":\"scripthash\",\"addresses\":[\"3EnHoc8QCGSCgiPoAjAEewKsu2fpuw7qh2\"]}}}],\"vout\":[{\"value\":725308,\"n\":0,\"scriptPubKey\":{\"asm\":\"OP_HASH160 082578c0addb14c6782bd41bdc3ad1b5fe34e106 OP_EQUAL\",\"hex\":\"a914082578c0addb14c6782bd41bdc3ad1b5fe34e10687\",\"reqsigs\":1,\"type\":\"scripthash\",\"addresses\":" +
                "[\"32S6Df4d3skdaR8oPNYLY1A73qrRo4jpgT\"]}},{\"value\":286094,\"n\":1,\"scriptPubKey\":{\"asm\":\"OP_HASH160 770929bd1d7ca5d7d014b535ac17227fe3a8f412 OP_EQUAL\",\"hex\":\"a914770929bd1d7ca5d7d014b535ac17227fe3a8f41287\",\"reqsigs\":1,\"type\":\"scripthash\",\"addresses\":[\"3CYRK2kACzepjdnx2nmN2XnCYhRP2DbCYS\"]}}],\"blocks\":[\"0000000000000000002b47fa958ff8cd3d7e865ef08cb2230b1805fbddadd300\"]}]";
        server.enqueue(new MockResponse().setResponseCode(200).setBody(json));

        String[] txids = new String[1];
        txids[0] = "txid";
        Response response = apiClient.getTransactions(txids);

        List<TransactionDetail> details = (List<TransactionDetail>) response.body();
        TransactionDetail transactionDetail = details.get(0);
        assertThat(transactionDetail.getHash(), equalTo("82a2d283543af94a31ba835bc95c2049e2bb4fb75a91b79a94c137c5e40ae2ee"));
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
        assertThat(state.getFees().getSlow(), equalTo(40.1));
        assertThat(state.getFees().getMed(), equalTo(20.1));
        assertThat(state.getFees().getFast(), equalTo(10.0));
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