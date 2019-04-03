package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.BuildConfig;
import com.google.gson.JsonObject;

import retrofit2.Response;

import static com.coinninja.coinkeeper.service.client.CoinKeeperClient.ADDRESSES_RESULT_LIMIT;

public class CoinKeeperApiClient extends NetworkingApiClient {

    private final static String URI_BASE = BuildConfig.COIN_NINJA_API_BASE;
    private final static String API_VERSION = "v1";
    public final static String API_BASE_ROUTE = URI_BASE + "/api/" + API_VERSION + "/";

    private CoinKeeperClient client;

    protected CoinKeeperClient getClient() {
        return client;
    }

    public CoinKeeperApiClient(CoinKeeperClient client) {
        this.client = client;
    }

    public Response queryAddressesFor(String[] inAddresses, int page) {
        JsonObject query = createQuery("address", inAddresses);
        return executeCall(client.getAddresses(query, page, ADDRESSES_RESULT_LIMIT));
    }

    public Response getTransactions(String[] txids) {
        JsonObject query = createQuery("txid", txids);

        return executeCall(client.queryTransactions(query));
    }

    public Response getHistoricPrice(String id) {
        return executeCall(getClient().getHistoricPrice(id));
    }

    public Response getTransactionStats(String transactionId) {
        return executeCall(client.getTransactionStats(transactionId));
    }

    public Response getCurrentState() {
        Response response = executeCall(client.checkIn());
        return response;
    }

    public Response checkHealth() {
        return executeCall(client.checkHealth());
    }

}
