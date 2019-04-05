package com.coinninja.coinkeeper.util.android.activity;

import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.service.client.model.InviteMetadata;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneVerificationCodeActivity;
import com.coinninja.coinkeeper.view.activity.VerifyRecoverywordsActivity;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    CoinNinjaUriBuilder coinNinjaUriBuilder = new CoinNinjaUriBuilder();
    ActivityNavigationUtil activityNavigationUtil = new ActivityNavigationUtil(coinNinjaUriBuilder);

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
        coinNinjaUriBuilder = null;
    }

    @Test
    public void navigates_to_verify_phone_number() {
        activityNavigationUtil.navigateToRegisterPhone(activity);

        Intent intent = new Intent(activity, VerifyPhoneNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigate_to_BackupRecoveryWordsStartActivity() {
        activityNavigationUtil.navigateToBackupRecoveryWords(activity);

        Intent intent = new Intent(activity, BackupRecoveryWordsStartActivity.class);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void navigate_to_home___TransactionHistoryActivity() {
        activityNavigationUtil.navigateToHome(activity);

        Intent intent = new Intent(activity, TransactionHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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
    public void navigate_to_verify_recovery_words_with_view_state() {
        String[] words = new String[]{"WORD1", "WORD2", "WORD3", "WORD4", "WORD5",
                "WORD6", "WORD7", "WORD8", "WORD9", "WORD10", "WORD11", "WORD12"};

        activityNavigationUtil.navigateToVerifyRecoveryWords(activity, words, Intents.EXTRA_BACKUP);

        Intent intent = new Intent(activity, VerifyRecoverywordsActivity.class);
        intent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, words);
        intent.putExtra(Intents.EXTRA_VIEW_STATE, Intents.EXTRA_BACKUP);
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
    public void navigates_to_verify_phone_number_code() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setNationalNumber(3305555555L);
        phoneNumber.setCountryCode(1);
        PhoneNumber number = new PhoneNumber(phoneNumber);

        activityNavigationUtil.navigateToVerifyPhoneNumberCode(activity, number);

        Intent intent = new Intent(activity, VerifyPhoneVerificationCodeActivity.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER, number);
        assertThat(activity, activityWithIntentStarted(intent));
    }

    @Test
    public void shares_drop_bit_manually() {
        DropBitInvitation dropBitInvitation = new DropBitInvitation();
        InviteMetadata metadata = new InviteMetadata();
        metadata.setAmount(new InviteMetadata.MetadataAmount(1000L, 1000L));
        metadata.setReceiver(new InviteMetadata.MetadataContact(1, "3305551111"));
        dropBitInvitation.setMetadata(metadata);

        activityNavigationUtil.shareDropbitManually(activity, dropBitInvitation);

        String receiver = dropBitInvitation.getMetadata().getReceiver().getPhoneNumber().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("sms", receiver, null));
        intent.putExtra("sms_body",  activity.getString(R.string.manual_send_sms_message, "$10.00"));
        assertThat(activity, activityWithIntentStarted(intent));
    }
}