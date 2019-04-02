package com.coinninja.coinkeeper.di.module;

import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.Bip70Client;
import com.coinninja.coinkeeper.service.client.BlockchainClient;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;
import com.coinninja.coinkeeper.service.client.SignedCoinKeeperApiClient;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.mockito.Mockito.mock;

@Module
public class ApiClientTestModule {

    @Provides
    SignedCoinKeeperApiClient signedCoinKeeperApiClient(TestCoinKeeperApplication app) {
        if (app.signedCoinKeeperApiClient == null)
            app.signedCoinKeeperApiClient = mock(SignedCoinKeeperApiClient.class);
        return app.signedCoinKeeperApiClient;
    }

    @Provides
    CoinKeeperApiClient coinKeeperApiClient(TestCoinKeeperApplication app) {
        if (app.coinKeeperApiClient == null)
            app.coinKeeperApiClient = mock(CoinKeeperApiClient.class);
        return app.coinKeeperApiClient;
    }

    @Provides
    BlockchainClient blockchainClient(TestCoinKeeperApplication app) {
        if (app.blockchainClient == null)
            app.blockchainClient = mock(BlockchainClient.class);
        return app.blockchainClient;
    }

    @Provides
    Bip70Client.Bip70Service bip70Service() {
        return new Retrofit.Builder()
                .baseUrl("https://localhost/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Bip70Client.Bip70Service.class);
    }
}
