package com.coinninja.coinkeeper.util.android.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.ui.settings.SettingsActivity;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.view.activity.CalculatorActivity;
import com.coinninja.coinkeeper.view.activity.CoinKeeperSupportActivity;
import com.coinninja.coinkeeper.view.activity.TransactionHistoryActivity;

import javax.inject.Inject;

import static com.coinninja.android.helpers.Resources.getString;

public class ActivityNavigationUtil {
    public static final String TRANSACTION_ROUTE = "tx";
    public static final String ADDRESS_ROUTE = "address";
    public static final String COINNINJA_COM = "coinninja.com";

    @Inject
    public ActivityNavigationUtil() {
    }

    public void navigateToSettings(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public void navigateToSupport(Context context) {
        Intent intent = new Intent(context, CoinKeeperSupportActivity.class);
        context.startActivity(intent);
    }

    public void openWebsite(Context context, Uri uri) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    public void navigateToBackupRecoveryWords(Context context) {
        context.startActivity(new Intent(context, BackupRecoveryWordsStartActivity.class));
    }

    public void navigateToTransactionHistory(Context context) {
        Intent intent = new Intent(context, TransactionHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public void navigateToHome(Context context) {
        Intent intent = new Intent(context, CalculatorActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void shareTransaction(Context context, String txid) {
        Uri uri = getBlockExplorerUriForTransactionWithId(txid);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
        Intent chooser = Intent.createChooser(intent, getString(context, R.string.share_transaction_intent_title));
        context.startActivity(chooser);
    }

    public void showAddressOnBlock(Context context, String address) {
        openWebsite(context, getBlockExplorerUriForAddressWithId(address));
    }

    private Uri getBlockExplorerUriForAddressWithId(String address) {
        return new Uri.Builder().
                scheme("https").
                authority(COINNINJA_COM).
                appendPath(ADDRESS_ROUTE).
                appendPath(address).
                build();
    }

    public void showTxidOnBlock(Context context, String txid) {
        openWebsite(context, getBlockExplorerUriForTransactionWithId(txid));
    }

    private Uri getBlockExplorerUriForTransactionWithId(String txid) {
        return new Uri.Builder().
                scheme("https").
                authority(COINNINJA_COM).
                appendPath(TRANSACTION_ROUTE).
                appendPath(txid).
                build();
    }

    public void explainSharedMemos(Context context) {
        openWebsite(context, Intents.URI_SHARED_MEMOS);
    }
}
