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
import com.coinninja.coinkeeper.view.activity.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class SettingsActivity extends SecuredActivity implements DialogInterface.OnClickListener {
    public static final String TAG_CONFIRM_DELETE_WALLET = "tag_confirm_delete_wallet";
    public static final int DELETE_WALLET_REQUEST_CODE = 12;
    public IntentFilter intentFilter;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED.equals(intent.getAction())) {
                setupPhoneVerification();
            }
        }
    };

    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    @Inject
    RemovePhoneNumberController removePhoneNumberController;

    @Inject
    SyncWalletManager syncWalletManager;

    @Inject
    CNWalletManager cnWalletManager;

    @Inject
    WalletHelper walletHelper;

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
        intentFilter = new IntentFilter(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
        removePhoneNumberController.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupRecoverWallet();
        setupDeleteWallet();
        setupPhoneVerification();
        setupSync();
        setupLicenses();
    }


    @Override
    protected void onStop() {
        super.onStop();
        removePhoneNumberController.onStop();
        localBroadCastUtil.unregisterReceiver(receiver);
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

    private void setupPhoneVerification() {
        if (walletHelper.hasVerifiedAccount()) {
            setupVerifiedAccount();
        } else {
            setupUnVerifiedAccount();
        }
    }

    private void setupUnVerifiedAccount() {
        findViewById(R.id.verified_number).setOnClickListener(V -> verifyPhoneNumber());
        findViewById(R.id.verified_number_arrow).setVisibility(View.VISIBLE);
        TextView phoneNumber = findViewById(R.id.verified_number_value);
        phoneNumber.setText(R.string.not_verified);
        phoneNumber.setTextColor(getResources().getColor(R.color.color_error));
        findViewById(R.id.deverify_phone_number).setVisibility(View.GONE);
    }

    private void setupVerifiedAccount() {
        TextView phoneNumber = findViewById(R.id.verified_number_value);
        phoneNumber.setText(walletHelper.getUserAccount().getPhoneNumber().toNationalDisplayText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            phoneNumber.setTextAppearance(R.style.TextAppearance_Small_PrimaryDark);
        } else {
            phoneNumber.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        findViewById(R.id.verified_number_arrow).setVisibility(View.INVISIBLE);
        findViewById(R.id.deverify_phone_number).setVisibility(View.VISIBLE);
        findViewById(R.id.verified_number).setOnClickListener(removePhoneNumberController::onRemovePhoneNumber);
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

    private void verifyPhoneNumber() {
        startActivity(new Intent(this, VerifyPhoneNumberActivity.class));
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
