package com.coinninja.coinkeeper.service.client;

import android.net.Uri;

import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.service.client.model.MerchantResponse;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Url;

@CoinkeeperApplicationScope
public class Bip70Client extends NetworkingApiClient {

    private Bip70Service bip70Service;

    @Inject
    Bip70Client(Bip70Service bip70Service) {
        this.bip70Service = bip70Service;
    }

    public interface Bip70Service {
        @Headers("Accept: application/payment-request")
        @GET
        Call<MerchantResponse> getMerchantInformation(@Url String fullUrl);
    }

    public void getMerchantInformation(Uri bip70Uri, Callback<MerchantResponse> callback) {
        Call<MerchantResponse> merchantInformation = bip70Service.getMerchantInformation(bip70Uri.toString());
        merchantInformation.enqueue(callback);
    }
}
