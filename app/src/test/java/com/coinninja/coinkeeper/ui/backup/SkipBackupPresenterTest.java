package com.coinninja.coinkeeper.ui.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.NotificationUtil;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.activity.CalculatorActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SkipBackupPresenterTest {

    private String[] words = new String[]{"word", "word", "word", "word", "word", "word", "word", "word", "word", "word", "word", "word"};

    private Activity activity;

    @Mock
    CNWalletManager cnWalletManager;

    @Mock
    Analytics analytics;

    @Mock
    NotificationUtil notificationUtil;

    @InjectMocks
    SkipBackupPresenter skipBackupPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = Robolectric.setupActivity(TestActivity.class);
    }

    @Test
    public void sends_local_message_warning_them_that_they_need_to_backup() {
        skipBackupPresenter.presentSkip(activity, words);
        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();

        latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        String message = activity.getString(R.string.message_dont_forget_to_backup);
        verify(notificationUtil).dispatchInternalError(message);
    }

    @Test
    public void reports_that_user_chose_to_skip() {
        skipBackupPresenter.presentSkip(activity, words);
        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();

        latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();

        verify(analytics).trackEvent(Analytics.EVENT_WALLET_BACKUP_SKIPPED);
    }

    @Test
    public void on_present_skip_show_dialog() {

        skipBackupPresenter.presentSkip(activity, words);

        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(latestAlertDialog);
        assertThat(((TextView) latestAlertDialog.findViewById(android.R.id.message)).getText().toString(),
                equalTo("You will have restricted use of the DropBit features until your wallet is backed up. " +
                        "Please backup as soon as you are able to."));
        assertThat(latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).getText().toString(), equalTo("OK, SKIP"));
        assertThat(latestAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText().toString(), equalTo("BACK UP NOW"));
    }

    @Test
    public void dismisses_on_negative_action() {
        skipBackupPresenter.presentSkip(activity, words);
        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();

        latestAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();


        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();

        assertNull(intent);
        assertFalse(latestAlertDialog.isShowing());
    }

    @Test
    public void navigate_to_calculator_screen_on_positive_clicked() {
        skipBackupPresenter.presentSkip(activity, words);
        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();

        latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();


        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = shadowActivity.getNextStartedActivity();

        assertThat(intent.getComponent().getClassName(), equalTo(CalculatorActivity.class.getName()));
        assertThat(intent.getFlags(), equalTo(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        assertFalse(latestAlertDialog.isShowing());
    }


    @Test
    public void positive_button_click_skips_backup() {
        skipBackupPresenter.presentSkip(activity, words);
        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();

        latestAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick();


        verify(cnWalletManager).skipBackup(words);
    }


    public static class TestActivity extends AppCompatActivity {
    }

}