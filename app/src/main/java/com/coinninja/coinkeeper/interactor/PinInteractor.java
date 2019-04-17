package com.coinninja.coinkeeper.interactor;

import com.coinninja.coinkeeper.model.helpers.SavePinTask;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.util.Hasher;

import javax.inject.Inject;

public class PinInteractor {
    private static final String TAG = PinInteractor.class.getSimpleName();
    private final UserHelper userHelper;
    private SavePinTask savePinTask;

    @Inject
    public PinInteractor(UserHelper userHelper, SavePinTask savePinTask) {
        this.userHelper = userHelper;
        this.savePinTask = savePinTask;
    }

    public void savePin(String pin) {
        savePinTask.execute(pin);
    }


    public String getSavedPin() {
        String pin = userHelper.getPin();
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
