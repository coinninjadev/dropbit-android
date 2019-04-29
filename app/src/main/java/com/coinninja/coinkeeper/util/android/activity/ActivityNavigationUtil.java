package com.coinninja.coinkeeper.util.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.ui.account.verify.UserAccountVerificationActivity;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.ui.spending.BuyBitcoinActivity;
import com.coinninja.coinkeeper.ui.spending.SpendBitcoinActivity;
import com.coinninja.coinkeeper.ui.transaction.history.TransactionHistoryActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;
import com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.TrainingActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneVerificationCodeActivity;
import com.coinninja.coinkeeper.view.activity.VerifyRecoverywordsActivity;

import java.util.HashMap;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import static com.coinninja.android.helpers.Resources.getString;
import static com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.LATITUDE;
import static com.coinninja.coinkeeper.util.uri.parameter.CoinNinjaParameter.LONGITUDE;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.ADDRESS;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.BUY_BITCOIN;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.NEWS;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.TRANSACTION;

public class ActivityNavigationUtil {

    private CoinNinjaUriBuilder coinNinjaUriBuilder;
    private final Analytics analytics;

    @Inject
    public ActivityNavigationUtil(CoinNinjaUriBuilder coinNinjaUriBuilder, Analytics analytics) {
        this.coinNinjaUriBuilder = coinNinjaUriBuilder;
        this.analytics = analytics;
    }

    public void navigateToSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public void navigateToUserVerification(Context context) {
        Intent intent = new Intent(context, UserAccountVerificationActivity.class);
        context.startActivity(intent);
    }

    public void navigateToSupport(Context context) {
        Intent intent = new Intent(context, CoinKeeperSupportActivity.class);
        context.startActivity(intent);
    }


    public void navigateToBackupRecoveryWords(Context context) {
        context.startActivity(new Intent(context, BackupRecoveryWordsStartActivity.class));
    }

    public void navigateToRegisterPhone(Context context) {
        Intent intent = new Intent(context, VerifyPhoneNumberActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void navigateToHome(Context context) {
        Intent intent = new Intent(context, TransactionHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void shareTransaction(Context context, String txid) {
        Uri uri = coinNinjaUriBuilder.build(TRANSACTION, txid);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        Intent chooser = Intent.createChooser(intent, getString(context, R.string.share_transaction_intent_title));
        context.startActivity(chooser);
    }

    public void showAddressOnBlock(Context context, String address) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(ADDRESS, address), (AppCompatActivity) context);
    }

    public void showTxidOnBlock(Context context, String txid) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(TRANSACTION, txid), (AppCompatActivity) context);
    }

    public void explainSharedMemos(Context context) {
        UriUtil.openUrl(DropbitIntents.URI_SHARED_MEMOS, (AppCompatActivity) context);
    }

    public void navigateToVerifyPhoneNumberCode(Context context, PhoneNumber phoneNumber) {
        Intent intent = new Intent(context, VerifyPhoneVerificationCodeActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber);
        context.startActivity(intent);
    }

    public void navigateToVerifyRecoveryWords(Context context, String[] seedWords, int viewState) {
        Intent intent = new Intent(context, VerifyRecoverywordsActivity.class);
        intent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, seedWords);
        intent.putExtra(DropbitIntents.EXTRA_VIEW_STATE, viewState);
        context.startActivity(intent);
    }

    public void navigtateToBuyBitcoin(Context context) {
        Intent intent = new Intent(context, BuyBitcoinActivity.class);
        context.startActivity(intent);
    }

    public void navigateToLearnBitcoin(Context context) {
        Intent intent = new Intent(context, TrainingActivity.class);
        context.startActivity(intent);
    }

    public void navigateToSpendBitcoin(Context context) {
        Intent intent = new Intent(context, SpendBitcoinActivity.class);
        context.startActivity(intent);
    }

    public void navigateToBuyGiftCard(Activity activity) {
        UriUtil.openUrl(Uri.parse("https://www.bitrefill.com/buy"), activity);
        analytics.trackEvent(Analytics.EVENT_SPEND_GIFT_CARDS);
    }

    public void navigateToWhereToSpend(Activity activity) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(NEWS, "webview", "load-online"), activity);
        analytics.trackEvent(Analytics.EVENT_SPEND_ONLINE);
    }

    public void navigatesToMapWith(Activity activity, @Nullable HashMap<CoinNinjaParameter, String> parameters, @Nullable Location location, String analyticsEvent) {
        if (parameters == null)
            parameters = new HashMap<>();

        if (location != null) {
            parameters.put(LATITUDE, String.valueOf(location.getLatitude()));
            parameters.put(LONGITUDE, String.valueOf(location.getLongitude()));
        }

        UriUtil.openUrl(coinNinjaUriBuilder.build(NEWS, parameters, "webview", "load-map"), activity);
        analytics.trackEvent(analyticsEvent);
    }

    public void navigateToBuyBitcoinWithCreditCard(Activity activity) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(BUY_BITCOIN, "creditcards"), activity);
        analytics.trackEvent(Analytics.EVENT_BUY_BITCOIN_CREDIT_CARD);
    }

    public void navigateToBuyBitcoinWithGiftCard(Activity activity) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(BUY_BITCOIN, "giftcards"), activity);
        analytics.trackEvent(Analytics.EVENT_BUY_BITCOIN_GIFT_CARD);
    }

    public void showDialogWithTag(FragmentManager fragmentManager, DialogFragment dialogFragment, String tag) {
        dialogFragment.show(fragmentManager, tag);
    }
}
