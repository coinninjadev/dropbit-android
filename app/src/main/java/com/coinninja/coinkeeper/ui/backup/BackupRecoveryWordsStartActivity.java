package com.coinninja.coinkeeper.ui.backup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.AuthorizedActionActivity;
import com.coinninja.coinkeeper.view.activity.BackupActivity;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class BackupRecoveryWordsStartActivity extends SecuredActivity {

    public static final int AUTHORIZE_VIEW_REQUEST_CODE = 1;
    public static final int AUTHORIZE_BACKUP_REQUEST_CODE = 2;

    @Inject
    CNWalletManager cnWalletManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AuthorizedActionActivity.RESULT_AUTHORIZED && requestCode == AUTHORIZE_VIEW_REQUEST_CODE)
            navigateToViewRecoveryWords(cnWalletManager.getRecoveryWords(), Intents.EXTRA_VIEW);
        else if (resultCode == AuthorizedActionActivity.RESULT_AUTHORIZED && requestCode == AUTHORIZE_BACKUP_REQUEST_CODE)
            navigateToViewRecoveryWords(cnWalletManager.getRecoveryWords(), Intents.EXTRA_BACKUP);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (cnWalletManager.hasWallet()) {
            setTheme(R.style.CoinKeeperTheme_DarkActionBar_UpOff_CloseOn);
        } else {
            setTheme(R.style.CoinKeeperTheme_LightActionBar_UpOff);
        }
        setContentView(R.layout.layout_activity_backup_recovery_words);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (cnWalletManager.hasWallet() && cnWalletManager.hasSkippedBackup()) {
            setupSkippedBackup();
        } else if (cnWalletManager.hasWallet()) {
            setupViewBackup();
        } else {
            setupCreateBackup();
        }
    }

    private void setupSkippedBackup() {
        setupBackup();
        findViewById(R.id.view_recovery_words).setOnClickListener(v -> onAuthorizeBackupRecoveryWords());
    }


    private void setupViewBackup() {
        findViewById(R.id.view_recovery_words).setOnClickListener(v -> onViewRecoveryWordsClicked());
    }

    private void setupCreateBackup() {
        findViewById(R.id.view_recovery_words).setOnClickListener(v -> backupRecoveryWords());
        setupBackup();
    }

    private void setupBackup() {
        findViewById(R.id.time_to_complete).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.view_recovery_words))
                .setText(R.string.view_recovery_words_instructions_button__not_backedup);
    }


    private void backupRecoveryWords() {
        navigateToViewRecoveryWords(cnWalletManager.generateRecoveryWords(), Intents.EXTRA_CREATE);
    }

    private void onAuthorizeBackupRecoveryWords() {
        Intent intent = new Intent(this, AuthorizedActionActivity.class);
        startActivityForResult(intent, AUTHORIZE_BACKUP_REQUEST_CODE);
    }

    private void onViewRecoveryWordsClicked() {
        Intent intent = new Intent(this, AuthorizedActionActivity.class);
        startActivityForResult(intent, AUTHORIZE_VIEW_REQUEST_CODE);
    }

    private void navigateToViewRecoveryWords(String[] seedWords, int extraState) {
        analytics.trackEvent(Analytics.EVENT_VIEW_RECOVERY_WORDS);
        Intent intent = new Intent(this, BackupActivity.class);
        intent.putExtra(Intents.EXTRA_RECOVERY_WORDS, seedWords);
        intent.putExtra(Intents.EXTRA_VIEW_STATE, extraState);
        startActivity(intent);
        finish();
    }
}
