package com.coinninja.coinkeeper.view.activity;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.adapter.SeedWordsPagerAdapter;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.DropbitIntents;
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(application = TestCoinKeeperApplication.class)
public class BackupActivityTest {
    String[] words = new String[]{"WORD1", "WORD2", "WORD3", "WORD4", "WORD5",
            "WORD6", "WORD7", "WORD8", "WORD9", "WORD10", "WORD11", "WORD12"};
    @Mock
    CNWalletManager cnWalletManager;
    @Mock
    ActivityNavigationUtil activityNavigationUtil;
    private BackupActivity activity;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(cnWalletManager.generateRecoveryWords()).thenReturn(words);
    }

    @After
    public void tearDown() {
        cnWalletManager = null;
        activityNavigationUtil = null;
        activity = null;
    }

    @Test
    public void sends_view_state_to_verification_screen_on_verify_with_out_backup() {
        start();

        activity.showNextActivity();

        verify(activityNavigationUtil).navigateToVerifyRecoveryWords(activity, words, DropbitIntents.EXTRA_CREATE);
    }

    @Test
    public void sends_view_state_to_verification_screen_on_verify_with_backup() {
        start(true, true);

        activity.showNextActivity();

        verify(activityNavigationUtil).navigateToVerifyRecoveryWords(activity, words, DropbitIntents.EXTRA_BACKUP);
    }

    @Test
    public void the_12_word_says_verify_when_skipped_backup() {
        start(true, true);
        TextView nextButton = activity.findViewById(R.id.seed_word_next_btn);

        activity.showLast();

        assertThat(nextButton.getText().toString(), equalTo("VERIFY"));
    }

    @Test
    public void provides_seed_words_on_adapter() {
        start();

        verify(activity.seedWordsPagerAdapter).setSeedWords(words);
    }

    @Test
    public void shows_recovery_words_when_created() {
        start();
        SeedWordsPagerAdapter adapter = (SeedWordsPagerAdapter) ((ViewPager)
                activity.findViewById(R.id.seed_words_pager)).getAdapter();
        assertThat(adapter, equalTo(activity.seedWordsPagerAdapter));
    }

    @Test
    public void remove_back_button_on_hide_first_call() {
        start();
        activity.hideFirst();

        assertThat(activity.findViewById(R.id.seed_word_back_btn).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void shows_back_button_on_show_first() {
        start();
        activity.hideFirst();

        activity.showFirst();

        assertThat(activity.findViewById(R.id.seed_word_back_btn).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void sets_theme_of_next_button_with_correct_text_on_show_next() {
        start();
        TextView nextButton = activity.findViewById(R.id.seed_word_next_btn);
        nextButton.setText("--- some text");

        activity.showNext();

        assertThat(nextButton.getText().toString(), equalTo("NEXT"));
    }

    @Test
    public void shows_last_button_with_verify() {
        start();
        activity.showNext();
        TextView nextButton = activity.findViewById(R.id.seed_word_next_btn);

        activity.showLast();

        assertThat(nextButton.getText().toString(), equalTo("VERIFY"));
    }

    @Test
    public void shows_last_button_with_verify_on_backup() {
        start();

        TextView nextButton = activity.findViewById(R.id.seed_word_next_btn);

        activity.showLast();

        assertThat(nextButton.getText().toString(), equalTo("VERIFY"));
    }

    @Test
    public void shows_last_button_with_finish() {
        start(true, false);

        TextView nextButton = activity.findViewById(R.id.seed_word_next_btn);

        activity.showLast();

        assertThat(nextButton.getText().toString(), equalTo("FINISH"));
    }

    @Test
    public void showing_next_activity_navigates_to_home() {
        start(true, false);

        activity.showNextActivity();

        verify(activityNavigationUtil).navigateToHome(activity);
    }

    private void start() {
        start(false, false);
    }

    private void start(boolean hasWallet, boolean didSkip) {
        Intent intent = new Intent();
        intent.putExtra(DropbitIntents.EXTRA_RECOVERY_WORDS, words);
        if (hasWallet && didSkip) {
            intent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, DropbitIntents.EXTRA_BACKUP);
        } else if (hasWallet) {
            intent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, DropbitIntents.EXTRA_VIEW);
        } else {
            intent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, DropbitIntents.EXTRA_CREATE);
        }
        ActivityController<BackupActivity> backupActivityActivityController = Robolectric.buildActivity(BackupActivity.class, intent).newIntent(intent).create();

        activity = backupActivityActivityController.get();
        activity.seedWordsPagerAdapter = mock(SeedWordsPagerAdapter.class);
        activity.cnWalletManager = cnWalletManager;
        activity.activityNavigationUtil = activityNavigationUtil;

        backupActivityActivityController.start().resume().visible();
    }
}