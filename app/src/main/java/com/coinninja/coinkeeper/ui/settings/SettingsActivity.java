package com.coinninja.coinkeeper.ui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.di.interfaces.DebugBuild;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.backup.BackupRecoveryWordsStartActivity;
import com.coinninja.coinkeeper.util.PhoneNumberUtil;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.LicensesActivity;
import com.coinninja.coinkeeper.view.activity.SplashActivity;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class SettingsActivity extends SecuredActivity implements DialogInterface.OnClickListener {
    public static final String TAG_CONFIRM_DELETE_WALLET = "tag_confirm_delete_wallet";
    public static final int DELETE_WALLET_REQUEST_CODE = 12;

    @Inject
    SyncWalletManager syncWalletManager;

    @Inject
    CNWalletManager cnWalletManager;

    @Inject
    @DebugBuild
    boolean isDebugBuild;

    @Inject
    DeleteWalletPresenter deleteWalletPresenter;

    @Inject
    PhoneNumberUtil phoneNumberUtil;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DELETE_WALLET_REQUEST_CODE && resultCode == AuthorizedActionActivity.RESULT_AUTHORIZED) {
            deleteWalletPresenter.onDelete();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecoverWallet();
        setupDeleteWallet();
        setupSync();
        setupLicenses();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE &&
                getFragmentManager().findFragmentByTag(TAG_CONFIRM_DELETE_WALLET) != null) {
            authorizeDelete();
        }

        dialog.dismiss();
    }

    private void onRecoverWalletClicked() {
        Intent intent = new Intent(this, BackupRecoveryWordsStartActivity.class);
        startActivity(intent);
    }

    private boolean deleteWallet() {
        GenericAlertDialog.newInstance(null,
                getString(R.string.delete_wallet_dialog_text),
                getString(R.string.delete_wallet_positive),
                getString(R.string.delete_wallet_negative),
                this,
                false,
                false).show(getFragmentManager(), TAG_CONFIRM_DELETE_WALLET);
        return true;
    }

    public void onDeleted() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    protected void authorizeDelete() {
        Intent authIntent = new Intent(this, AuthorizedActionActivity.class);

        String authMessage = getString(R.string.authorize_delete_message);
        authIntent.putExtra(Intents.EXTRA_AUTHORIZED_ACTION_MESSAGE, authMessage);
        startActivityForResult(authIntent, DELETE_WALLET_REQUEST_CODE);
    }

    private void setupLicenses() {
        findViewById(R.id.open_source).setOnClickListener(v -> onLicenseClicked());
    }

    private void onLicenseClicked() {
        startActivity(new Intent(this, LicensesActivity.class));
    }

    private void setupSync() {
        if (isDebugBuild) {
            View view = findViewById(R.id.settings_sync);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(V -> onSync());
        }
    }

    public boolean onSync() {
        syncWalletManager.syncNow();
        onBackPressed();
        return true;
    }

    private void setupRecoverWallet() {
        if (cnWalletManager.hasSkippedBackup()) {
            findViewById(R.id.not_backed_up_message).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.not_backed_up_message).setVisibility(View.GONE);
        }
        findViewById(R.id.recover_wallet).setOnClickListener(v -> onRecoverWalletClicked());
    }

    private void setupDeleteWallet() {
        findViewById(R.id.delete_wallet).setOnClickListener(V -> deleteWallet());
        deleteWalletPresenter.setCallback(this::onDeleted);
    }
}
