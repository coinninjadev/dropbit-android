package com.coinninja.coinkeeper.di.module;

import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.di.interfaces.CoinkeeperApplicationScope;
import com.coinninja.coinkeeper.di.interfaces.FCMAppID;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperClient;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;
import com.coinninja.coinkeeper.service.interceptors.SignedRequestInterceptor;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApiClientModule {

    @Provides
    @CoinkeeperApplicationScope
    SignedCoinKeeperApiClient signedCoinKeeperApiClient(CoinKeeperClient client, @FCMAppID String fcmAppId) {
        return new SignedCoinKeeperApiClient(client, fcmAppId);
    }

    @Provides
    @CoinkeeperApplicationScope
    CoinKeeperApiClient coinKeeperApiClient() {
        CoinKeeperClient client = new Retrofit.Builder().
                baseUrl(CoinKeeperApiClient.API_BASE_ROUTE).
                client(new OkHttpClient.Builder().
                        build()).
                addConverterFactory(GsonConverterFactory.create()).
                build().create(CoinKeeperClient.class);

        return new CoinKeeperApiClient(client);
    }

    @Provides
    CoinKeeperClient signedCoinKeeperClient(SignedRequestInterceptor signedRequestInterceptor) {
        CoinKeeperClient client = new Retrofit.Builder().
                baseUrl(CoinKeeperApiClient.API_BASE_ROUTE).
                client(new OkHttpClient.Builder().
                        addInterceptor(signedRequestInterceptor).build()).
                addConverterFactory(GsonConverterFactory.create()).
                build().create(CoinKeeperClient.class);

        return client;
    }

    @Provides
    BlockchainClient blockchainClient() {
        return BlockchainClient.newInstance();
    }

    @Provides
    @FCMAppID
    String fcmAppID() {
        return BuildConfig.FCM_APPLICATION_KEY;
    }

}
