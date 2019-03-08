package com.coinninja.coinkeeper.service.client;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BlockchainClient extends NetworkingApiClient {
    protected final static String URI_BASE = "https://blockchain.info/";
    private BlockchainInfoClient client;


    public static BlockchainClient newInstance(String url) {
        BlockchainInfoClient client = new Retrofit.Builder().
                baseUrl(url).
                addConverterFactory(GsonConverterFactory.create()).
                build().create(BlockchainInfoClient.class);
        return new BlockchainClient(client);
    }

    public static BlockchainClient newInstance() {
        return newInstance(URI_BASE);
    }

    private BlockchainClient(BlockchainInfoClient client) {
        this.client = client;
    }

    public Response getTransactionFor(String txid) {
        return executeCall(client.getTransactionFor(txid));
    }

    public Response broadcastTransaction(String rawTx) {
        return executeCall(client.pushTX(rawTx));
    }
}
