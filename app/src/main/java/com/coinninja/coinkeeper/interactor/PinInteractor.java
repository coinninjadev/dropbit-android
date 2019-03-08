package com.coinninja.coinkeeper.interactor;

import android.content.Context;

import com.coinninja.coinkeeper.CoinKeeperApplication;
import com.coinninja.coinkeeper.di.interfaces.ApplicationContext;
import com.coinninja.coinkeeper.model.helpers.SavePinTask;
import com.coinninja.coinkeeper.util.Hasher;

import javax.inject.Inject;

public class PinInteractor {
    private static final String TAG = PinInteractor.class.getSimpleName();
    private CoinKeeperApplication application;
    private SavePinTask savePinTask;

    @Inject
    public PinInteractor(@ApplicationContext Context context) {
        application = (CoinKeeperApplication) context.getApplicationContext();
        savePinTask = new SavePinTask(application);
    }

    public void savePin(String pin) {
        savePinTask.execute(pin);
    }


    public String getSavedPin() {
        String pin = application.getUser().getPin();
        if (pin == null || pin.isEmpty())
            return null;
        return pin;
    }


    public boolean verifyPin(String pin) {
        String savedPin = getSavedPin();
        return (savedPin != null && pin != null) && savedPin.contentEquals(pin);
    }

    public boolean hashThenVerify(String pin) {
        return verifyPin(new Hasher().hash(pin));
    }
}
