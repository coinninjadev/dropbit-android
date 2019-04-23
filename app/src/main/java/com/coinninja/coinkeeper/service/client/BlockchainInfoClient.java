package com.coinninja.coinkeeper.service.client;

import com.coinninja.coinkeeper.service.client.model.BlockchainTX;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface BlockchainInfoClient {

    @GET("rawtx/{txid}")
    Call<BlockchainTX> getTransactionFor(@Path("txid") String transactionId);

    @POST("pushtx")
    @FormUrlEncoded
    Call<ResponseBody> pushTX(@Field("tx") String rawTX);
}
