package com.coinninja.coinkeeper.view.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionInflater;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;
import com.coinninja.coinkeeper.ui.backup.SkipBackupPresenter;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.fragment.VerifyRecoverywordsFragment;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class VerifyRecoverywordsActivity extends SecuredActivity implements VerifyRecoveryWordsPresenter.View {

    public static final String DATA_RECOVERY_WORDS = "DATA_RECOVERY_WORDS";
    public static final String TAG_FRAGMENT = "CHALLENGE";
    private static final int NUM_CHALLENGES = 2;

    @Inject
    ActivityNavigationUtil activityNavigationUtil;

    @Inject
    NotificationUtil notificationUtil;

    @Inject
    SkipBackupPresenter skipBackupPresenter;

    @Inject
    VerifyRecoveryWordsPresenter presenter;

    private String[] recoveryWords;
    private int numChallenges = 1;
    private String currentTag;
    private int viewState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewState = getIntent().getIntExtra(Intents.EXTRA_VIEW_STATE, Intents.EXTRA_CREATE);
        if (Intents.EXTRA_BACKUP == viewState) {
            setTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff_CloseOn);
        } else {
            setTheme(R.style.CoinKeeperTheme_LightActionBar_UpOn_SkipOn);
        }
        setContentView(R.layout.verify_recovery_words_activity);
        recoveryWords = (String[]) getIntent().getExtras().get(DATA_RECOVERY_WORDS);
        presenter.attach(this, recoveryWords, NUM_CHALLENGES);
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentTag = getNextTag();
        Fragment fragment = VerifyRecoverywordsFragment.newInstance(presenter);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.wrapper, fragment, currentTag);
        fragmentTransaction.commit();
    }

    @Override
    public void onSkipClicked() {
        skipBackupPresenter.presentSkip(this, recoveryWords);
    }

    @Override
    public void showRecoveryWords() {
        Intent intent = new Intent(this, BackupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Intents.EXTRA_RECOVERY_WORDS, recoveryWords);
        intent.putExtra(Intents.EXTRA_VIEW_STATE, viewState);
        startActivity(intent);
    }

    @Override
    public void showNextChallenge() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(currentTag);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fragment.setExitTransition(TransitionInflater.from(this).inflateTransition(R.transition.slide_out_to_left));
        }

        currentTag = getNextTag();
        Fragment nextFragment = VerifyRecoverywordsFragment.newInstance(presenter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nextFragment.setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.slide_in_from_right));
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.wrapper, nextFragment, currentTag);
        fragmentTransaction.commit();
    }

    @Override
    public void onChallengeCompleted() {
        reportAnalytics();
        if (viewState == Intents.EXTRA_BACKUP) {
            notificationUtil.dispatchInternal(getString(R.string.message_successful_wallet_backup));
            activityNavigationUtil.navigateToHome(this);
        } else {
            activityNavigationUtil.navigateToRegisterPhone(this);
        }
    }


    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    private String getNextTag() {
        String tag = new StringBuilder(TAG_FRAGMENT).
                append("_").append(numChallenges).toString();
        numChallenges += 1;
        return tag;
    }

    private void reportAnalytics() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_WALLET_BACKUP_SUCCESSFUL);
            analytics.trackEvent(Analytics.EVENT_WALLET_CREATE);
        }

    }
}