package com.coinninja.coinkeeper.service.client;

import com.coinninja.bindings.model.Transaction;
import com.coinninja.coinkeeper.bitcoin.BroadcastProvider;
import com.coinninja.coinkeeper.bitcoin.BroadcastingClient;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BlockstreamClient extends NetworkingApiClient implements BroadcastingClient {
    protected final static String URI_BASE = "https://blockstream.info/";
    private BlockstreamInfoClient client;

    @Inject
    BlockstreamInfoClient blockstreamInfoClient;

    public static BlockstreamClient newInstance(String url) {
        BlockstreamInfoClient client = new Retrofit.Builder().
                baseUrl(url).
                addConverterFactory(GsonConverterFactory.create()).
                build().create(BlockstreamInfoClient.class);
        return new BlockstreamClient(client);
    }

    public static BlockstreamClient newInstance() {
        return newInstance(URI_BASE);
    }

    BlockstreamClient(BlockstreamInfoClient client) {
        this.client = client;
    }

    @NotNull
    @Override
    public Response broadcastTransaction(Transaction transaction) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), transaction.rawTx);
        return executeCall(client.pushTX(requestBody));
    }

    @NotNull
    @Override
    public BroadcastProvider broadcastProvider() {
        return BroadcastProvider.BLOCK_STREAM;
    }
}
