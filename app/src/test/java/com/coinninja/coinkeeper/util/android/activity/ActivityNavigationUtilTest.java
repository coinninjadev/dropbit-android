package com.coinninja.coinkeeper.util.android.activity;

import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder;
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
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

import java.util.HashMap;

import static com.coinninja.android.helpers.Resources.getString;
import static com.coinninja.matchers.ActivityMatchers.activityWithIntentStarted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ActivityNavigationUtilTest {

    @Mock
    ActivityController<StartActivity> activityController;

    @Mock
    StartActivity activity;

    @Mock
    Analytics analytics;

    private CoinNinjaUriBuilder coinNinjaUriBuilder = new CoinNinjaUriBuilder();
    private ActivityNavigationUtil activityNavigationUtil;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activityNavigationUtil = new ActivityNavigationUtil(coinNinjaUriBuilder, analytics);
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
        analytics = null;
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
    public void navigates_to_buy_bitcoin_with_credit_card() {
        Uri uri = Uri.parse("https://coinninja.com/buybitcoin/creditcards");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        activityNavigationUtil.navigateToBuyBitcoinWithCreditCard(activity);

        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics).trackEvent(Analytics.EVENT_BUY_BITCOIN_CREDIT_CARD);
    }

    @Test
    public void navigates_to_buy_bitcoin_with_gift_card() {
        Uri uri = Uri.parse("https://coinninja.com/buybitcoin/giftcards");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        activityNavigationUtil.navigateToBuyBitcoinWithGiftCard(activity);

        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics).trackEvent(Analytics.EVENT_BUY_BITCOIN_GIFT_CARD);
    }

    @Test
    public void navigates_to_where_to_spend() {
        Uri uri = Uri.parse("https://coinninja.com/news/webview/load-online");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        activityNavigationUtil.navigateToWhereToSpend(activity);

        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics).trackEvent(Analytics.EVENT_SPEND_ONLINE);
    }

    @Test
    public void navigates_to_buy_gift_card() {
        Uri uri = Uri.parse("https://www.bitrefill.com/buy");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        activityNavigationUtil.navigateToBuyGiftCard(activity);

        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics).trackEvent(Analytics.EVENT_SPEND_GIFT_CARDS);
    }

    @Test
    public void navigate_to_around_me() {
        HashMap<CoinNinjaParameter, String> parameters = new HashMap<>();
        parameters.put(CoinNinjaParameter.TYPE, "atm");
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(87d);
        location.setLongitude(25d);

        activityNavigationUtil.navigatesToMapWith(activity, parameters, location, Analytics.EVENT_BUY_BITCOIN_AT_ATM);

        Uri uri = Uri.parse("https://coinninja.com/news/webview/load-map?lat=87.0&long=25.0&type=atm");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics, times(1)).trackEvent(Analytics.EVENT_BUY_BITCOIN_AT_ATM);
    }

    @Test
    public void navigate_to_around_me__without_location() {
        HashMap<CoinNinjaParameter, String> parameters = new HashMap<>();
        parameters.put(CoinNinjaParameter.TYPE, "atm");

        activityNavigationUtil.navigatesToMapWith(activity, parameters, null, Analytics.EVENT_BUY_BITCOIN_AT_ATM);

        Uri uri = Uri.parse("https://coinninja.com/news/webview/load-map?type=atm");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics, times(1)).trackEvent(Analytics.EVENT_BUY_BITCOIN_AT_ATM);
    }

    @Test
    public void navigate_to_around_me__without_location_and_parameters() {
        activityNavigationUtil.navigatesToMapWith(activity, null, null, Analytics.EVENT_BUY_BITCOIN_AT_ATM);

        Uri uri = Uri.parse("https://coinninja.com/news/webview/load-map");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        assertThat(activity, activityWithIntentStarted(intent));
        verify(analytics, times(1)).trackEvent(Analytics.EVENT_BUY_BITCOIN_AT_ATM);
    }
}