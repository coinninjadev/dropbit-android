package com.coinninja.coinkeeper.model.helpers;

import android.os.AsyncTask;

import com.coinninja.coinkeeper.di.interfaces.UUID;

import javax.inject.Inject;


public class CreateUserTask extends AsyncTask<Void, Void, UserHelper> {

    private UserHelper user;
    private OnUserCreatedListener onUserCreatedListener;

    @Inject
    public CreateUserTask(UserHelper user) {
        this.user = user;
    }

    @Override
    protected UserHelper doInBackground(Void... params) {
        if (user.getUser() == null) {
            user.createFirstUser();
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
