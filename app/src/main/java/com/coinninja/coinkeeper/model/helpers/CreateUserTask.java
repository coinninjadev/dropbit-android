package com.coinninja.coinkeeper.model.helpers;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.di.interfaces.UUID;

import javax.inject.Inject;


public class CreateUserTask extends AsyncTask<Void, Void, UserHelper> {

    private String uuid;
    private UserHelper user;

    private OnUserCreatedListener onUserCreatedListener;

    @Inject
    public CreateUserTask(@UUID String uuid, UserHelper user) {
        this.uuid = uuid;
        this.user = user;
    }

    @Override
    protected UserHelper doInBackground(Void... params) {
        if (user.getUser() == null) {
            user.createFirstUser(uuid);
        }
        return user;
    }

    @Override
    protected void onPostExecute(UserHelper userHelper) {
        super.onPostExecute(userHelper);

        onUserCreatedListener.onCreatedSuccessfully();
    }

    public void setOnUserCreatedListener(OnUserCreatedListener onUserCreatedListener) {
        this.onUserCreatedListener = onUserCreatedListener;
    }

    public interface OnUserCreatedListener {
        void onCreatedSuccessfully();
    }
}
