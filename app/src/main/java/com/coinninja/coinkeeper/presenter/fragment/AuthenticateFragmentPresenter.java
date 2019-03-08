package com.coinninja.coinkeeper.presenter.fragment;

import android.os.CountDownTimer;

import com.coinninja.coinkeeper.interactor.PinEntryImpl;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.service.tasks.LockUserTask;
import com.coinninja.coinkeeper.util.DateUtil;

import javax.inject.Inject;

public class AuthenticateFragmentPresenter {

    private final Authentication authentication;
    private final PinEntry pinEntry;
    private LockUserTask lockUserTask;
    private View view;
    private static final int MAX_NUM_ATTEMPTS_BEFORE_LOCK = 6;
    private int invalidPinEntryCount = 0;
    private final UserHelper user;
    private final DateUtil dateUtil;
    private CountDownTimer timer;

    @Inject
    public AuthenticateFragmentPresenter(UserHelper userHelper, Authentication authentication,
                                         PinEntry pinEntry, DateUtil dateUtil, LockUserTask lockUserTask) {
        user = userHelper;
        this.dateUtil = dateUtil;
        this.authentication = authentication;
        this.pinEntry = pinEntry;
        this.lockUserTask = lockUserTask;
    }

    public void attach(View view) {
        this.view = view;
    }

    public void startAuth(boolean forceAuth) {
        if (forceAuth || !authentication.isAuthenticated()) {
            authenticate();
        } else {
            bypassAuth();
        }
    }

    private void authenticate() {
        if (isLockedOut()) {
            establishLock();
        } else {
            if (authentication.hasOptedIntoFingerprintAuth()) {
                view.showFingerprintAuth();
                view.authenticateWithFingerprint();
            } else {
                view.showPinAuth();
            }
        }
    }

    private void establishLock() {
        view.onWalletLock();
        timer = new CountDownTimer(getLockDuration(), 1000) {

            @Override
            public void onFinish() {
                view.onWalletLockRemoved();
                timer = null;
            }

            @Override
            public void onTick(long millisUntilFinished) {

            }
        };
        timer.start();
    }

    private long getLockDuration() {
        return user.getLockedUntilTime() - dateUtil.getCurrentTimeInMillis() + 500;
    }

    private void bypassAuth() {
        authentication.setAuthenticated();
        view.userHasAuthenticated();
    }

    public void verifyPin(int[] pin) {
        String enteredPinHashed = PinEntryImpl.HASH_PIN(pin);
        PinEntry.PinCompare pinMatch = pinEntry.comparePins(enteredPinHashed, pinEntry.getSavedPin());

        switch (pinMatch) {
            case MATCH:
                authentication.setAuthenticated();
                view.userHasAuthenticated();
                break;
            case NON_MATCH:
                onInvalidPinEntry();
                break;
        }
    }

    private void onInvalidPinEntry() {
        invalidPinEntryCount += 1;

        if (invalidPinEntryCount >= MAX_NUM_ATTEMPTS_BEFORE_LOCK) {
            lockUserTask.execute();
            establishLock();
            lockUserTask = lockUserTask.clone();
            invalidPinEntryCount = 0;

        } else {
            view.onPinMismatch();
        }
    }

    public void onFingerprintAuthenticated() {
        authentication.setAuthenticated();
        view.userHasAuthenticated();
    }

    public boolean isLockedOut() {
        return user.getLockedUntilTime() > dateUtil.getCurrentTimeInMillis();
    }

    public void onPause() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    public interface View {
        void userHasAuthenticated();

        void showFingerprintAuth();

        void showPinAuth();

        void onPinMismatch();

        void onWalletLock();

        void authenticateWithFingerprint();

        void onWalletLockRemoved();
    }
}
