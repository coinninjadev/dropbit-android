package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.User;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class UserHelper {

    private final DaoSessionManager daoSessionManager;
    private WalletHelper walletHelper;

    @Inject
    public UserHelper(@NonNull DaoSessionManager daoSessionManager, WalletHelper walletHelper) {
        this.daoSessionManager = daoSessionManager;
        this.walletHelper = walletHelper;
    }

    public User createFirstUser() {
        User user = new User();
        daoSessionManager.getUserDao().insert(user);
        daoSessionManager.attach(user);
        daoSessionManager.createWallet(user);
        return user;
    }

    public void savePin(String pin) {
        User user = getUser();
        user.refresh();
        user.setPin(pin);
        user.update();
    }

    public String getPin() {
        return getUser().getPin();
    }

    public User getUser() {
        return daoSessionManager.getUserDao().queryBuilder().limit(1).unique();
    }

    public void lockOutUntil(long unlockTime) {
        User user = getUser();
        user.setLockedUntilTime(unlockTime);
        user.update();
    }

    public long getLockedUntilTime() {
        return getUser().getLockedUntilTime();
    }

    public boolean hasCompletedTraining() {
        if (getUser() != null) {
            return getUser().getCompletedTraining();
        }
        return false;
    }

    public void setCompletedTraining(boolean completedTraining) {
        User user = getUser();
        user.setCompletedTraining(completedTraining);
        user.update();
        user.refresh();
    }
}
