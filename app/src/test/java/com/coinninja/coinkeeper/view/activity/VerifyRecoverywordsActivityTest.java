package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.presenter.fragment.VerifyRecoveryWordsPresenter;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.Shuffler;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class VerifyRecoverywordsActivityTest {

    private static final String[] recoveryWords = {
            "word1",
            "word2",
            "word3",
            "word4",
            "word5",
            "word6",
            "word7",
            "word8",
            "word9",
            "word10",
            "word11",
            "word12"
    };

    private VerifyRecoverywordsActivity activity;
    private Resources resources;

    Analytics analytics;

    @Mock
    NotificationUtil notificationUtil;

    @Mock
    ActivityNavigationUtil activityNavigationUtil;

    private Intent initIntent;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication application = ApplicationProvider.getApplicationContext();
        analytics = application.analytics;
        application.notificationUtil = notificationUtil;
        application.activityNavigationUtil = activityNavigationUtil;
        application.verifyRecoveryWordsPresenter = new VerifyRecoveryWordsPresenter(mock(CNWalletManager.class), new Shuffler());

        initIntent = new Intent();
        initIntent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, recoveryWords);
    }

    @After
    public void tearDown() throws Exception {
        activity = null;
        resources = null;
        analytics = null;
        notificationUtil = null;
        initIntent = null;
        activityNavigationUtil = null;
    }

    private void start() {
        start(DropbitIntents.EXTRA_CREATE);
    }

    private void start(int viewState) {
        initIntent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, viewState);
        ActivityController<VerifyRecoverywordsActivity> activityController = Robolectric.buildActivity(VerifyRecoverywordsActivity.class, initIntent);
        activity = activityController.get();
        activityController.create();
        activity.notificationUtil = notificationUtil;
        activity.activityNavigationUtil = activityNavigationUtil;
        activityController.start().resume();
        resources = activity.getResources();
        activity.analytics = analytics;
    }


    @Test
    public void does_not_message_user_when_creating_wallet() {
        start();

        activity.onChallengeCompleted();

        verifyZeroInteractions(notificationUtil);
    }

    @Test
    public void queues_notification_for_backing_up_words() {
        start(DropbitIntents.EXTRA_BACKUP);

        activity.onChallengeCompleted();

        verify(notificationUtil).dispatchInternal(activity.getString(R.string.message_successful_wallet_backup));
    }

    @Test
    public void backing_up_wallet_navigates_to_home_screen() {
        start(DropbitIntents.EXTRA_BACKUP);

        activity.onChallengeCompleted();

        verify(activityNavigationUtil).navigateToHome(activity);
        verify(analytics).trackEvent(Analytics.Companion.EVENT_WALLET_BACKUP_SUCCESSFUL);
        verify(analytics).trackEvent(Analytics.Companion.EVENT_WALLET_CREATE);
    }

    @Test
    public void failingTheChallengeHasUserRewriteTheirRecoveryWords__with_backup_view_state() {
        start(DropbitIntents.EXTRA_BACKUP);

        activity.showRecoveryWords();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getComponent().getClassName(), equalTo(BackupActivity.class.getName()));
        assertThat(nextStartedActivity.getIntExtra(DropbitIntents.EXTRA_VIEW_STATE, -1), equalTo(DropbitIntents.EXTRA_BACKUP));
    }

    @Test
    public void itHasInstructions() {
        start();

        TextView instructions = activity.findViewById(R.id.instructions);
        assertThat(instructions.getText(), equalTo(resources.getText(R.string.verify_recovery_words_instructions)));
    }

    @Test
    public void showsVerifyPhoneNumberActivityWhenCompleted() {
        start();

        activity.onChallengeCompleted();

        verify(activityNavigationUtil).navigateToRegisterPhone(activity);
        verify(analytics).trackEvent(Analytics.Companion.EVENT_WALLET_BACKUP_SUCCESSFUL);
        verify(analytics).trackEvent(Analytics.Companion.EVENT_WALLET_CREATE);
    }

    @Test
    public void failingTheChallengeHasUserRewriteTheirRecoveryWords() {
        start();

        activity.showRecoveryWords();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getComponent().getClassName(), equalTo(BackupActivity.class.getName()));
        assertThat(nextStartedActivity.getIntExtra(DropbitIntents.EXTRA_VIEW_STATE, -1), equalTo(DropbitIntents.EXTRA_CREATE));
    }

    @Test
    public void showing_recovery_words_relays_words() {
        start();

        activity.showRecoveryWords();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getStringArrayExtra(DropbitIntents.EXTRA_RECOVERY_WORDS), equalTo(recoveryWords));
    }

    @Test
    public void showingRecoveryWordsClearsTop() {
        start();

        activity.showRecoveryWords();

        ShadowActivity shadowActivity = shadowOf(activity);
        Intent nextStartedActivity = shadowActivity.getNextStartedActivity();
        assertThat(nextStartedActivity.getFlags(), equalTo(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

}