package com.coinninja.coinkeeper.view.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.receiver.StartupCompleteReceiver;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import javax.inject.Inject;


public class SplashActivity extends BaseActivity {

    Runnable displayDelayRunnable;

    @Inject
    CNWalletManager cnWalletManager;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
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
        displayDelayRunnable = () -> runOnUiThread(this::showNextActivity);
        authentication.forceDeAuthenticate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.img_logo).postDelayed(displayDelayRunnable, 500);
    }

    @Override
    public void onPause() {
        super.onPause();
        findViewById(R.id.img_logo).removeCallbacks(displayDelayRunnable);
    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadCastUtil.sendGlobalBroadcast(StartupCompleteReceiver.class,
                DropbitIntents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP);
    }

    @SuppressLint("NewApi")
    private void showNextActivity() {
        if (cnWalletManager.getHasWallet()) {
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

}
