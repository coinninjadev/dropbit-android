package com.coinninja.coinkeeper.service.client;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface BlockstreamInfoClient {
    @POST("api/tx")
    @Streaming
    Call<ResponseBody> pushTX(@Body RequestBody body);
}
