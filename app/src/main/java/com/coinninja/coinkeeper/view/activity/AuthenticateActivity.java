package com.coinninja.coinkeeper.view.activity;

import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.fragment.AuthenticateFragment;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class AuthenticateActivity extends SecuredActivity {

    @Inject
    AuthenticateFragment authenticateFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        showReAuthentication();
    }

    @Override
    public void muteViews() {
        super.muteViews();
        authenticateFragment.muteViews();
    }

    @Override
    public void teardownMute() {
        super.teardownMute();
        if (isForeGrounded())
            authenticateFragment.teardownMute();
    }

    public void showReAuthentication() {
        authenticateFragment.setOnUserHasAuthenticated(this::onAuthenticated);
        replaceFragment(authenticateFragment);
    }

    private void replaceFragment(Fragment currentFragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.authenticate_frame, currentFragment);
        fragmentTransaction.commit();
    }

    public void onAuthenticated() {
        setResult(AppCompatActivity.RESULT_OK);
        finish();
    }
}
