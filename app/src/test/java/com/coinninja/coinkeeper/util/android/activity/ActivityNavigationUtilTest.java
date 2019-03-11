package com.coinninja.coinkeeper.util.android.activity;

import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneVerificationCodeActivity;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

import static com.coinninja.android.helpers.Resources.getString;
import static com.coinninja.matchers.ActivityMatchers.activityWithIntentStarted;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ActivityNavigationUtilTest {

    @Mock
    ActivityController<StartActivity> activityController;

    @Mock
    StartActivity activity;

    @InjectMocks
    ActivityNavigationUtil activityNavigationUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activityController = Robolectric.buildActivity(StartActivity.class);
        activity = activityController.get();
        activityController.setup();
    }

    @After
    public void tearDown() throws Exception {
        activityController.pause().stop().destroy();
        activityController = null;
        activity = null;
        activityNavigationUtil = null;
    }

    @Test
    public void navigate_to_BackupRecoveryWordsStartActivity() {
        activityNavigationUtil.navigateToBackupRecoveryWords(activity);

        Intent intent = new Intent(activity, BackupRecoveryWordsStartActivity.class);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigate_to_TransactionHistoryActivity() {
        activityNavigationUtil.navigateToTransactionHistory(activity);

        Intent intent = new Intent(activity, TransactionHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigate_to_SettingsActivity() {
        activityNavigationUtil.navigateToSettings(activity);

        Intent intent = new Intent(activity, SettingsActivity.class);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigate_to_CoinKeeperSupportActivity() {
        activityNavigationUtil.navigateToSupport(activity);

        Intent intent = new Intent(activity, CoinKeeperSupportActivity.class);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigate_to_website() {
        String sampleWebsite = "https://coinninja.com";

        activityNavigationUtil.openWebsite(activity, Uri.parse(sampleWebsite));

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sampleWebsite));
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void shares_transaction_with_other_applications() {
        String txid = "--txid--";
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse("https://coinninja.com/tx/--txid--");
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        Intent chooser = Intent.createChooser(intent, getString(activity, R.string.share_transaction_intent_title));

        activityNavigationUtil.shareTransaction(activity, txid);

        assertThat(activity, activityWithIntentStarted(chooser));
    }

    @Test
    public void opens_transaction_id_on_block_explorer() {
        String txid = "--txid--";
        Uri uri = Uri.parse("https://coinninja.com/tx/--txid--");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        activityNavigationUtil.showTxidOnBlock(activity, txid);

        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void opens_address_on_block_explorer() {
        String address = "--address--";
        Uri uri = Uri.parse("https://coinninja.com/address/--address--");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        activityNavigationUtil.showAddressOnBlock(activity, address);

        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void explains_shared_memos() {
        activityNavigationUtil.explainSharedMemos(activity);

        Intent intent = new Intent(Intent.ACTION_VIEW, Intents.URI_SHARED_MEMOS);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigates_to_verify_phone_number() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        activityNavigationUtil.navigateToVerifyPhoneNumber(activity, number);

        Intent intent = new Intent(activity, VerifyPhoneVerificationCodeActivity.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER, number);
        assertThat(activity, activityWithIntentStarted(intent));
    }
}