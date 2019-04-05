package com.coinninja.coinkeeper.util.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.client.model.DropBitInvitation;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.currency.USDCurrency;
import com.coinninja.coinkeeper.util.uri.CoinNinjaUriBuilder;
import com.coinninja.coinkeeper.util.uri.UriUtil;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneVerificationCodeActivity;
import com.coinninja.coinkeeper.view.activity.VerifyRecoverywordsActivity;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Resources.getString;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.ADDRESS;
import static com.coinninja.coinkeeper.util.uri.routes.CoinNinjaRoute.TRANSACTION;

public class ActivityNavigationUtil {

    private CoinNinjaUriBuilder coinNinjaUriBuilder;

    @Inject
    public ActivityNavigationUtil(CoinNinjaUriBuilder coinNinjaUriBuilder) {
        this.coinNinjaUriBuilder = coinNinjaUriBuilder;
    }

    public void navigateToSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
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
        UriUtil.openUrl(coinNinjaUriBuilder.build(ADDRESS, address), (Activity) context);
    }

    public void showTxidOnBlock(Context context, String txid) {
        UriUtil.openUrl(coinNinjaUriBuilder.build(TRANSACTION, txid), (Activity) context);
    }

    public void explainSharedMemos(Context context) {
        UriUtil.openUrl(Intents.URI_SHARED_MEMOS, (Activity) context);
    }

    public void navigateToVerifyPhoneNumberCode(Context context, PhoneNumber phoneNumber) {
        Intent intent = new Intent(context, VerifyPhoneVerificationCodeActivity.class);
        intent.putExtra(Intents.EXTRA_PHONE_NUMBER, phoneNumber);
        context.startActivity(intent);
    }

    public void navigateToVerifyRecoveryWords(Context context, String[] seedWords, int viewState) {
        Intent intent = new Intent(context, VerifyRecoverywordsActivity.class);
        intent.putExtra(VerifyRecoverywordsActivity.DATA_RECOVERY_WORDS, seedWords);
        intent.putExtra(Intents.EXTRA_VIEW_STATE, viewState);
        context.startActivity(intent);
    }

    public void shareDropbitManually(Activity activity, DropBitInvitation dropBitInvitation) {
        PhoneNumber phoneNumber = dropBitInvitation.getMetadata().getReceiver().getPhoneNumber();
        USDCurrency fiatCurrency = new USDCurrency(dropBitInvitation.getMetadata().getAmount().getUsd());
        String smsBody = activity.getString(R.string.manual_send_sms_message, fiatCurrency.toFormattedCurrency());
        Uri uri = Uri.fromParts("sms", phoneNumber.toString(), null);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra("sms_body", smsBody);
        activity.startActivity(intent);
    }
}
