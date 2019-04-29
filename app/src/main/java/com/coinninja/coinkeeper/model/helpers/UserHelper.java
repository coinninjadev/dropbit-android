package com.coinninja.coinkeeper.model.helpers;

import com.coinninja.coinkeeper.model.db.User;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public class UserHelper {

    private final DaoSessionManager daoSessionManager;

    @Inject
    public UserHelper(@NonNull DaoSessionManager daoSessionManager) {
        this.daoSessionManager = daoSessionManager;
    }

    public void savePin(String pin) {
        User user = getUser();
        user.setPin(pin);
        user.update();
    }

    public String getPin() {
        User user = getUser();
        return user == null ? null : user.getPin();
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

    public void setCompletedTraining(boolean completedTraining) {
        User user = getUser();
        user.setCompletedTraining(completedTraining);
        user.update();
        user.refresh();
    }
}
