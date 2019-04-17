package com.coinninja.coinkeeper.service.tasks;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;

import javax.inject.Inject;

public class CNHealthCheckTask extends AsyncTask<Void, Void, Boolean> {
    private final CoinKeeperApiClient apiClient;
    private HealthCheckCallback callback;


    @Inject
    CNHealthCheckTask(CoinKeeperApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private CNHealthCheckTask(CoinKeeperApiClient apiClient, HealthCheckCallback callback) {
        this(apiClient);
        setCallback(callback);
    }

    public void setCallback(HealthCheckCallback callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return apiClient.checkHealth().isSuccessful();
    }

    @Override
    protected void onPostExecute(Boolean isSuccss) {
        if (isSuccss)
            callback.onHealthSuccess();
        else
            callback.onHealthFail();
    }

    @Override
    public CNHealthCheckTask clone() {
        return new CNHealthCheckTask(apiClient, callback);
    }

    public interface HealthCheckCallback {
        void onHealthSuccess();

        void onHealthFail();
    }
}
