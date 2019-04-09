package com.coinninja.coinkeeper.ui.settings;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.cn.wallet.dust.DustProtectionPreference;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.uri.DropbitUriBuilder;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.LicensesActivity;
import com.coinninja.coinkeeper.view.activity.SplashActivity;
import com.coinninja.matchers.IntentMatcher;

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
import org.robolectric.shadows.ShadowActivity;

import static com.coinninja.android.helpers.Views.clickOn;
import static com.coinninja.android.helpers.Views.withId;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {

    @Mock
    DustProtectionPreference dustProtectionPreference;
    @Mock
    PhoneNumberUtil phoneNumberUtil;
    @Mock
    private CNWalletManager cnWalletManager;
    @Mock
    private DeleteWalletPresenter deleteWalletPresenter;
    @Mock
    private SyncWalletManager syncWalletManager;
    private DropbitUriBuilder dropbitUriBuilder = new DropbitUriBuilder();
    private ActivityController<SettingsActivity> activityController;
    private SettingsActivity activity;
    private ShadowActivity shadowActivity;

    @After
    public void tearDown() {
        cnWalletManager = null;
        deleteWalletPresenter = null;
        syncWalletManager = null;
        activity = null;
        activityController = null;
        shadowActivity = null;
        phoneNumberUtil = null;
        dropbitUriBuilder = null;
        dustProtectionPreference = null;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestCoinKeeperApplication coinKeeperApplication = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        when(coinKeeperApplication.authentication.isAuthenticated()).thenReturn(true);
        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(false);
        activityController = Robolectric.buildActivity(SettingsActivity.class);
        activityController.create();
        activity = activityController.get();
        activity.deleteWalletPresenter = deleteWalletPresenter;
        activity.cnWalletManager = cnWalletManager;
        activity.syncWalletManager = syncWalletManager;
        activity.phoneNumberUtil = phoneNumberUtil;
        activity.dustProtectionPreference = dustProtectionPreference;
        activity.dropbitUriBuilder = dropbitUriBuilder;
        shadowActivity = shadowOf(activity);
    }

    @Test
    public void shows_not_backed_up_when_user_skipped_backup() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(true);
        start();

        assertThat(activity.findViewById(R.id.not_backed_up_message).getVisibility(),
                equalTo(View.VISIBLE));

    }

    @Test
    public void hides_not_backed_up_when_words_backedUp() {
        when(cnWalletManager.hasSkippedBackup()).thenReturn(false);
        start();

        assertThat(activity.findViewById(R.id.not_backed_up_message).getVisibility(),
                equalTo(View.GONE));
    }

    @Test
    public void shows_recovery_words_help_screen() {
        start();

        activity.findViewById(R.id.recover_wallet).performClick();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(),
                equalTo(BackupRecoveryWordsStartActivity.class.getName()));
    }

    @Test
    public void shows_licenses() {
        start();

        activity.findViewById(R.id.open_source).performClick();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(),
                equalTo(LicensesActivity.class.getName()));
    }

    @Test
    public void clicking_sync_triggers_sync() {
        start(true);

        activity.findViewById(R.id.settings_sync).performClick();

        verify(syncWalletManager).syncNow();
    }

    @Test
    public void shows_sync_when_debug_is_on() {
        start(true);

        assertThat(activity.findViewById(R.id.settings_sync).getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void hides_sync_when_debug_is_off() {
        start();

        assertThat(activity.findViewById(R.id.settings_sync).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void navigates_to_splash_activity_on_Delete() {
        start();

        activity.onDeleted();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(), equalTo(SplashActivity.class.getName()));
        assertThat(startedActivity.getFlags(), equalTo(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Test
    public void performs_authorization_for_deleting_wallet() {
        start();
        activity.findViewById(R.id.delete_wallet).performClick();
        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET);

        activity.onClick(dialog.getDialog(), DialogInterface.BUTTON_POSITIVE);

        ShadowActivity.IntentForResult activityForResult = shadowActivity.getNextStartedActivityForResult();
        assertThat(activityForResult.requestCode, equalTo(SettingsActivity.DELETE_WALLET_REQUEST_CODE));
        assertThat(activityForResult.intent.getComponent().getClassName(), equalTo(AuthorizedActionActivity.class.getName()));
    }

    @Test
    public void performs_delete() {
        start();

        activity.onActivityResult(SettingsActivity.DELETE_WALLET_REQUEST_CODE, AuthorizedActionActivity.RESULT_AUTHORIZED, null);

        verify(deleteWalletPresenter).onDelete();
    }

    @Test
    public void negative_delete_confirmation_dismisses_only() {
        start();
        activity.findViewById(R.id.delete_wallet).performClick();
        DialogFragment dialog = (DialogFragment) activity.getFragmentManager().findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET);

        activity.onClick(dialog.getDialog(), DialogInterface.BUTTON_NEGATIVE);

        assertNull(activity.getFragmentManager().findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET));
    }

    @Test
    public void prompts_to_confirm_delete() {
        start();

        activity.findViewById(R.id.delete_wallet).performClick();

        verify(deleteWalletPresenter).setCallback(any());
        assertNotNull(activity.getFragmentManager().findFragmentByTag(SettingsActivity.TAG_CONFIRM_DELETE_WALLET));
    }

    @Test
    public void show_auth_delete_with_message() {
        String expectedAuthMessage = "Enter pin to confirm deletion of your wallet.";
        start();

        activity.authorizeDelete();
        Intent intent = shadowActivity.getNextStartedActivity();

        Bundle extras = intent.getExtras();
        String authorizedActionMessage = extras.getString(Intents.EXTRA_AUTHORIZED_ACTION_MESSAGE);

        assertThat(authorizedActionMessage, equalTo(expectedAuthMessage));
    }

    @Test
    public void renders_state_of_dust_protection_preference() {
        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(true);
        start();

        Switch view = withId(activity, R.id.dust_protection_toggle);

        assertTrue(view.isChecked());
    }

    @Test
    public void observes_switch_toggle() {
        when(dustProtectionPreference.isDustProtectionEnabled()).thenReturn(true);
        start();
        clickOn(withId(activity, R.id.dust_protection_toggle));

        verify(dustProtectionPreference).setProtection(false);
    }

    @Test
    public void clicking_on_tooltip_for_dust_protection_navigates_to_website() {
        start();

        clickOn(withId(activity, R.id.dust_protection_tooltip));

        Intent intent = shadowOf(activity).getNextStartedActivity();
        Intent expectedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dropbit.app/tooltips/dustprotection"));
        assertThat(intent, IntentMatcher.equalTo(expectedIntent));
    }

    private void start() {
        start(false);
    }

    private void start(boolean isDebug) {
        activity.isDebugBuild = isDebug;
        activityController.start().resume().visible();
    }
}