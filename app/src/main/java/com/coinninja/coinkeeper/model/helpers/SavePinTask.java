package com.coinninja.coinkeeper.model.helpers;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.CoinKeeperApplication;

public class SavePinTask extends AsyncTask<String, Void, Void> {
    private CoinKeeperApplication application;

    public SavePinTask(CoinKeeperApplication application) {
        this.application = application;
    }

    @Override
    protected Void doInBackground(String... args) {
        String pin = args[0];
        application.getUser().savePin(pin);
        application = null;
        return null;
    }
}
