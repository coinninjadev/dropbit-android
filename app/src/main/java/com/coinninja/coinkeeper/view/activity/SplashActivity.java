package com.coinninja.coinkeeper.view.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.model.helpers.CreateUserTask;
import com.coinninja.coinkeeper.model.helpers.UserHelper;
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import javax.inject.Inject;


public class SplashActivity extends BaseActivity {

    Runnable displayDelayRunnable;

    @Inject
    CreateUserTask createUserTask;
    @Inject
    CNWalletManager cnWalletManager;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    UserHelper userHelper;
    @Inject
    Authentication authentication;
    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);
        displayDelayRunnable = () -> runOnUiThread(() -> onDelayComplete());
        createUserTask.setOnUserCreatedListener(() -> onUserCreated());
        authentication.forceDeAuthenticate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        createUserTask.execute();
        findViewById(R.id.img_logo).postDelayed(displayDelayRunnable, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        findViewById(R.id.img_logo).removeCallbacks(displayDelayRunnable);
    }

    void onUserCreated() {
        createUserTask = null;
        if (asynctaskscompleted()) {
            showNextActivity();
        }
    }

    void onDelayComplete() {
        displayDelayRunnable = null;
        if (asynctaskscompleted()) {
            showNextActivity();
        }
    }

    synchronized boolean asynctaskscompleted() {
        return createUserTask == null && displayDelayRunnable == null;
    }

    @SuppressLint("NewApi")
    private void showNextActivity() {
        localBroadCastUtil.sendGlobalBroadcast(StartupCompleteReceiver.class,
                Intents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP);
        if (cnWalletManager.hasWallet()) {
            activityNavigationUtil.navigateToHome(this);
        } else {
            navigateToStartActivity();
        }
    }

    @SuppressLint("NewApi")
    private void navigateToStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);

        View imgLogo = findViewById(R.id.img_logo);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, imgLogo, getString(R.string.logo_slide));
        startActivity(intent, options.toBundle());
    }


    public CreateUserTask getCreateUserTask() {
        return createUserTask;
    }

}
