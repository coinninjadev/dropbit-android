package com.coinninja.coinkeeper.service.tasks;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.util.DateUtil;
import com.coinninja.coinkeeper.util.DropbitIntents;

import javax.inject.Inject;

public class LockUserTask extends AsyncTask<Void, Void, Void> {

    private final DateUtil dateUtil;
    private final UserHelper user;

    @Inject
    public LockUserTask(UserHelper userHelper, DateUtil dateUtil) {
        user = userHelper;
        this.dateUtil = dateUtil;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        user.lockOutUntil(dateUtil.getCurrentTimeInMillis() + DropbitIntents.LOCK_DURRATION);
        return null;
    }

    @Override
    public LockUserTask clone() {
        return new LockUserTask(user, dateUtil);
    }
}
