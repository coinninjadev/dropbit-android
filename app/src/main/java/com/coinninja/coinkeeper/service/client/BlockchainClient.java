package com.coinninja.coinkeeper.service.client;

import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.bitcoin.BroadcastProvider;
import com.coinninja.coinkeeper.bitcoin.BroadcastingClient;

import org.jetbrains.annotations.NotNull;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BlockchainClient extends NetworkingApiClient implements BroadcastingClient {

    protected final static String URI_BASE = "https://blockchain.info/";
    private BlockchainInfoClient client;


    private BlockchainClient(BlockchainInfoClient client) {
        this.client = client;
    }

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

    public Response getTransactionFor(String txid) {
        return executeCall(client.getTransactionFor(txid));
    }

    @Override
    public Response broadcastTransaction(Transaction transaction) {
        return executeCall(client.pushTX(transaction.rawTx));
    }

    @NotNull
    @Override
    public BroadcastProvider broadcastProvider() {
        return BroadcastProvider.BLOCKCHAIN_INFO;
    }
}
