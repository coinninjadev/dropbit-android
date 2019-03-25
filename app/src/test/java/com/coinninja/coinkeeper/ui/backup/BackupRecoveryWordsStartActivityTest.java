package com.coinninja.coinkeeper.ui.backup;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BackupActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import androidx.appcompat.widget.Toolbar;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BackupRecoveryWordsStartActivityTest {

    private ActivityController<BackupRecoveryWordsStartActivity> activityController;
    private BackupRecoveryWordsStartActivity activity;
    private ShadowActivity shadowActivity;

    private String[] words = {"word", "word", "word", "word", "word", "word", "word",
            "word", "word", "word", "word", "word"};

    @Mock
    private CNWalletManager cnWalletManager;

    @Mock
    private Analytics analytics;
    private TestCoinKeeperApplication application;

    @Mock
    private SkipBackupPresenter skipBackupPresenter;

    @After
    public void tearDown() {
        activityController = null;
        activity = null;
        shadowActivity = null;
        words = null;
        cnWalletManager = null;
        analytics = null;
        application = null;
        skipBackupPresenter = null;
    }

    @Before
    public void setUp() {
        application = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        when(application.authentication.isAuthenticated()).thenReturn(true);
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(BackupRecoveryWordsStartActivity.class);
        activity = activityController.get();
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void shows_time_to_complete_when_wallet_does_not_exist() {
        start(false);

        assertThat(activity.findViewById(R.id.time_to_complete).getVisibility(),
                equalTo(View.VISIBLE));
    }

    @Test
    public void show_write_down_words_as_primary_button_test_when_wallet_does_not_exist() {
        start(false);

        assertThat(((Button) activity.findViewById(R.id.view_recovery_words)).getText().toString(),
                equalTo("Write down words + Back up"));
    }

    @Test
    public void shows_user_skip_and_backup_button_when_wallet_does_not_exist() {
        start(false);

        assertThat(activity.findViewById(R.id.skip_and_backup_later).getVisibility(),
                equalTo(View.VISIBLE));
    }

    // Does Not Have Wallet

    @Test
    public void does_not_enable_close() {
        start(false);
        Toolbar toolbar = activity.findViewById(R.id.toolbar);

        assertFalse(toolbar.getMenu().hasVisibleItems());
    }

    @Test
    public void proides_generated_seedwords_when_backing_up_new_wallet() {
        when(cnWalletManager.generateRecoveryWords()).thenReturn(words);
        start(false);

        activity.findViewById(R.id.view_recovery_words).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getExtras().getStringArray(Intents.EXTRA_RECOVERY_WORDS),
                equalTo(words));
    }

    @Test
    public void navigate_to_backup_recovery_words_when_has_no_wallet() {
        start(false);

        activity.findViewById(R.id.view_recovery_words).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(BackupActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void navigate_to_backup_activity_with_extras_create_view_state_when_no_wallet() {
        start(false);

        activity.findViewById(R.id.view_recovery_words).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();


        assertThat(intent.getIntExtra(Intents.EXTRA_VIEW_STATE, -1),
                equalTo(Intents.EXTRA_CREATE));
    }

    @Test
    public void allow_user_to_skip_backingup_recovery_words() {
        when(cnWalletManager.generateRecoveryWords()).thenReturn(words);
        start(false);

        activity.findViewById(R.id.skip_and_backup_later).performClick();

        verify(skipBackupPresenter).presentSkip(activity, words);
    }

    // Has Wallet but skipped Backup
    @Test
    public void sets_view_state_to_backup() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_BACKUP_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getExtras().getInt(Intents.EXTRA_VIEW_STATE), equalTo(Intents.EXTRA_BACKUP));
    }

    @Test
    public void forwards_recovery_words_to_backup_activity_on_backup_auth() {
        when(cnWalletManager.getRecoveryWords()).thenReturn(words);
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_BACKUP_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getExtras().getStringArray(Intents.EXTRA_RECOVERY_WORDS),
                equalTo(words));
    }

    @Test
    public void navigates_to_backup_recovery_words() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_BACKUP_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(BackupActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void do_not_navigate_to_backup_recovery_words_when_not_auth() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_BACKUP_REQUEST_CODE,
                Activity.RESULT_CANCELED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();

        assertNull(intent);
    }

    @Test
    public void requests_authorization_to_view_words_for_Backup() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();

        activity.findViewById(R.id.view_recovery_words).performClick();

        Intent intent = shadowActivity.getNextStartedActivity();

        ShadowActivity.IntentForResult nextStartedActivityForResult = shadowActivity.getNextStartedActivityForResult();

        assertThat(nextStartedActivityForResult.intent.getComponent().getClassName(), equalTo(AuthorizedActionActivity.class.getName()));
        assertThat(nextStartedActivityForResult.requestCode, equalTo(BackupRecoveryWordsStartActivity.AUTHORIZE_BACKUP_REQUEST_CODE));
    }

    @Test
    public void shows_time_to_complete_when_wallet_does_not_exist_and_user_has_skipped_backup() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();

        assertThat(activity.findViewById(R.id.time_to_complete).getVisibility(),
                equalTo(View.VISIBLE));
        assertThat(((Button) activity.findViewById(R.id.view_recovery_words)).getText().toString(),
                equalTo("Write down words + Back up"));
    }

    @Test
    public void hides_time_to_complete_when_wallet_does_not_exist() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();

        assertThat(activity.findViewById(R.id.time_to_complete).getVisibility(),
                equalTo(View.GONE));
    }

    @Test
    public void sets_view_state_to_view() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_VIEW_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getExtras().getInt(Intents.EXTRA_VIEW_STATE), equalTo(Intents.EXTRA_VIEW));
    }

    // Has Backed Up Wallet

    @Test
    public void forwards_recovery_words_to_backup_activity() {
        when(cnWalletManager.getRecoveryWords()).thenReturn(words);
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_VIEW_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();
        assertThat(intent.getExtras().getStringArray(Intents.EXTRA_RECOVERY_WORDS),
                equalTo(words));
    }

    @Test
    public void navigates_to_view_recovery_words() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_VIEW_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(BackupActivity.class.getName()));
        assertTrue(shadowActivity.isFinishing());
    }

    @Test
    public void do_not_navigate_to_view_recovery_words_when_not_auth() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_VIEW_REQUEST_CODE,
                Activity.RESULT_CANCELED,
                null);

        Intent intent = shadowActivity.getNextStartedActivity();

        assertNull(intent);
    }

    @Test
    public void authorizes_request_before_showing_recovery_words() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();
        activity.findViewById(R.id.view_recovery_words).performClick();

        ShadowActivity.IntentForResult nextStartedActivityForResult = shadowActivity.getNextStartedActivityForResult();

        assertThat(nextStartedActivityForResult.intent.getComponent().getClassName(), equalTo(AuthorizedActionActivity.class.getName()));
        assertThat(nextStartedActivityForResult.requestCode, equalTo(BackupRecoveryWordsStartActivity.AUTHORIZE_VIEW_REQUEST_CODE));
    }

    @Test
    public void reports_recovery_words_viewing_to_analytics() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();
        activity.onActivityResult(BackupRecoveryWordsStartActivity.AUTHORIZE_VIEW_REQUEST_CODE,
                AuthorizedActionActivity.RESULT_AUTHORIZED,
                null);

        verify(analytics).trackEvent(Analytics.EVENT_VIEW_RECOVERY_WORDS);
    }

    private void start() {
        start(true);
    }

    private void start(boolean hasWallet) {
        when(cnWalletManager.hasWallet()).thenReturn(hasWallet);
        if (hasWallet) {
            when(application.walletHelper.getSeedWords()).thenReturn(words);
        } else {
            when(application.walletHelper.getSeedWords()).thenReturn(new String[0]);
        }
        activityController.create();
        activity.analytics = analytics;
        activity.cnWalletManager = cnWalletManager;
        activity.skipBackupPresenter = skipBackupPresenter;
        activityController.start().resume().visible();
    }
}