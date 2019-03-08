package com.coinninja.coinkeeper.service.tasks;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.service.client.CoinKeeperApiClient;

public class CNHealthCheckTask extends AsyncTask<Void, Void, Boolean> {
    private final CoinKeeperApiClient apiClient;
    private HealthCheckCallback callback;


    public static CNHealthCheckTask newInstance(CoinKeeperApplication application, HealthCheckCallback callback) {
        return new CNHealthCheckTask(application, callback);
    }

    public CNHealthCheckTask(CoinKeeperApplication application, HealthCheckCallback callback) {
        this.callback = callback;
        apiClient = application.getAPIClient();
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

    public interface HealthCheckCallback {
        void onHealthSuccess();

        void onHealthFail();
    }
}
