package com.coinninja.coinkeeper.view.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;

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

    private int delayMillis = 1000;
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
        findViewById(R.id.img_logo).postDelayed(displayDelayRunnable, delayMillis);
    }

    @Override
    protected void onPause() {
        super.onPause();
        findViewById(R.id.img_logo).removeCallbacks(displayDelayRunnable);
    }

    void onUserCreated() {
        createUserTask = null;
        boolean isAllTaskDone = asyncTasksEvaluation();
        if (isAllTaskDone) {
            showNextActivity();
        }
    }

    void onDelayComplete() {
        displayDelayRunnable = null;
        boolean isAllTaskDone = asyncTasksEvaluation();

        if (isAllTaskDone) {
            showNextActivity();
        }
    }

    synchronized boolean asyncTasksEvaluation() {

        boolean isTimerRunning = displayDelayRunnable != null;

        boolean isCreateUserRunning = createUserTask != null;

        return !isTimerRunning && !isCreateUserRunning;
    }

    @SuppressLint("NewApi")
    private void showNextActivity() {
        localBroadCastUtil.sendGlobalBroadcast(StartupCompleteReceiver.class,
                Intents.ACTION_ON_APPLICATION_FOREGROUND_STARTUP);
        if (cnWalletManager.hasWallet()) {
            activityNavigationUtil.navigateToHome(this);
        } else {
            newUserEvaluation();
        }
    }

    private void newUserEvaluation() {
        boolean hasSeenTrainingScreens = userHelper.hasCompletedTraining();

        if (hasSeenTrainingScreens) {
            navigateToStartActivity();
        } else {
            navigateToTrainingActivity();
        }
    }

    private void navigateToTrainingActivity() {
        Intent intent = new Intent(SplashActivity.this, TrainingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @SuppressLint("NewApi")
    private void navigateToStartActivity() {
        Intent intent = new Intent(this, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        View imgLogo = findViewById(R.id.img_logo);
        AlphaAnimation anim = new AlphaAnimation(1.0f, 0.0f);
        anim.setDuration(500);
        imgLogo.startAnimation(anim);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, imgLogo, imgLogo.getTransitionName());
        startActivity(intent, options.toBundle());
        overridePendingTransition(0, 0);
    }


    public CreateUserTask getCreateUserTask() {
        return createUserTask;
    }

}
