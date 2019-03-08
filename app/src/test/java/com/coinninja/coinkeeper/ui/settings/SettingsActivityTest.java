package com.coinninja.coinkeeper.ui.settings;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.TestCoinKeeperApplication;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.model.db.Account;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.LicensesActivity;
import com.coinninja.coinkeeper.view.activity.SplashActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneNumberActivity;


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

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {

    @Mock
    private CNWalletManager cnWalletManager;

    @Mock
    private WalletHelper walletHelper;

    @Mock
    private DeleteWalletPresenter deleteWalletPresenter;

    @Mock
    private SyncWalletManager syncWalletManager;

    @Mock
    private RemovePhoneNumberController removePhoneNumberController;

    @Mock
    private LocalBroadCastUtil localBroadCastUtil;

    @Mock
    PhoneNumberUtil phoneNumberUtil;

    private ActivityController<SettingsActivity> activityController;
    private SettingsActivity activity;
    private ShadowActivity shadowActivity;
    private PhoneNumber phoneNumber;

    @After
    public void tearDown() {
        cnWalletManager = null;
        walletHelper = null;
        deleteWalletPresenter = null;
        syncWalletManager = null;
        removePhoneNumberController = null;
        activity = null;
        activityController = null;
        shadowActivity = null;
        localBroadCastUtil = null;
        phoneNumberUtil = null;
    }

    @Before
    public void setUp() {
        TestCoinKeeperApplication coinKeeperApplication = (TestCoinKeeperApplication) RuntimeEnvironment.application;
        when(coinKeeperApplication.authentication.isAuthenticated()).thenReturn(true);
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(SettingsActivity.class);
        activityController.create();
        activity = activityController.get();
        activity.deleteWalletPresenter = deleteWalletPresenter;
        activity.cnWalletManager = cnWalletManager;
        activity.walletHelper = walletHelper;
        activity.syncWalletManager = syncWalletManager;
        activity.removePhoneNumberController = removePhoneNumberController;
        activity.localBroadCastUtil = localBroadCastUtil;
        activity.phoneNumberUtil = phoneNumberUtil;
        shadowActivity = shadowOf(activity);
        phoneNumber = new PhoneNumber();
    }

    private void start() {
        start(false);
    }

    private void start(boolean isDebug) {
        activity.isDebugBuild = isDebug;
        activityController.start().resume().visible();
    }

    @Test
    public void instructs_controller_to_start() {
        start();

        verify(removePhoneNumberController).onStart();
    }

    @Test
    public void requests_controllers_to_clean_up_when_destroyed() {
        activityController.stop();

        verify(removePhoneNumberController).onStop();
    }

    @Test
    public void observes_deverification_completed() {
        start();

        assertThat(activity.intentFilter.getAction(0), equalTo(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED));
        verify(localBroadCastUtil).registerReceiver(activity.receiver, activity.intentFilter);
    }

    @Test
    public void stops_observing_deverification_completed() {
        start();

        activityController.stop();

        verify(localBroadCastUtil).unregisterReceiver(activity.receiver);
    }

    @Test
    public void redraws_phone_verification_when_deverification_completed() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true).thenReturn(false);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        start();

        activity.receiver.onReceive(activity, new Intent(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED));

        verify(walletHelper, times(2)).hasVerifiedAccount();
        TextView phoneNumberValue = activity.findViewById(R.id.verified_number_value);
        assertThat(phoneNumberValue.getText().toString(), equalTo(activity.getString(R.string.not_verified)));
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
    public void selecting_verification_prompts_to_remove_phone_number() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        start();

        View view = activity.findViewById(R.id.verified_number);
        view.performClick();

        assertThat(activity.findViewById(R.id.verified_number_arrow).getVisibility(), equalTo(View.INVISIBLE));
        verify(removePhoneNumberController).onRemovePhoneNumber(view);
    }

    @Test
    public void phone_verification_preference_shows_deverify_message() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);

        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        start();

        TextView deverifyTextView = activity.findViewById(R.id.deverify_phone_number);
        assertThat(deverifyTextView.getVisibility(), equalTo(View.VISIBLE));
    }

    @Test
    public void phone_verification_preference_show_phone_number() {
        Account account = mock(Account.class);
        when(walletHelper.hasVerifiedAccount()).thenReturn(true);
        when(walletHelper.getUserAccount()).thenReturn(account);
        PhoneNumber phoneNumber = new PhoneNumber("+12223334444");
        when(account.getPhoneNumber()).thenReturn(phoneNumber);
        String national = "(222) 333-4444";
        start();

        TextView phoneNumberValue = activity.findViewById(R.id.verified_number_value);
        assertThat(phoneNumberValue.getText().toString(), equalTo(national));
    }

    @Test
    public void navigates_to_phone_verification_screen() {
        start();

        TextView deverifyTextView = activity.findViewById(R.id.deverify_phone_number);
        assertThat(deverifyTextView.getVisibility(), equalTo(View.GONE));

        activity.findViewById(R.id.verified_number).performClick();

        Intent startedActivity = shadowActivity.getNextStartedActivity();
        assertThat(startedActivity.getComponent().getClassName(), equalTo(VerifyPhoneNumberActivity.class.getName()));
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
}