package com.coinninja.coinkeeper.model.helpers;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.CoinKeeperApplication;

import javax.inject.Inject;

public class SavePinTask extends AsyncTask<String, Void, Void> {
    private final UserHelper userHelper;

    @Inject
    SavePinTask(UserHelper userHelper) {
        this.userHelper = userHelper;
    }

    @Override
    protected Void doInBackground(String... args) {
        String pin = args[0];
        userHelper.savePin(pin);
        return null;
    }
}
