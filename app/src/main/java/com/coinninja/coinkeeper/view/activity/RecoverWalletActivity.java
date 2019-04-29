package com.coinninja.coinkeeper.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.interfaces.CNWalletServicesInterface;
import com.coinninja.coinkeeper.cn.wallet.service.CNServiceConnection;
import com.coinninja.coinkeeper.cn.wallet.service.CNWalletService;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.crypto.BitcoinUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class RecoverWalletActivity extends SecuredActivity {

    private String[] recoveryWords;
    Button button;
    ImageView icon;
    TextView title;
    TextView message;
    BroadcastReceiver receiver;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    BitcoinUtil bitcoinUtil;
    @Inject
    CNServiceConnection cnServiceConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_wallet);

        if (getIntent().hasExtra(DropbitIntents.EXTRA_RECOVERY_WORDS)) {
            recoveryWords = getIntent().getExtras().getStringArray(DropbitIntents.EXTRA_RECOVERY_WORDS);
        }

        button = findViewById(R.id.ok);
        title = findViewById(R.id.title);
        icon = findViewById(R.id.icon);
        message = findViewById(R.id.message);

        receiver = new RecoverWalletReceiver();

        Intent intent = new Intent(this, CNWalletService.class);
        bindService(intent, cnServiceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isValid(recoveryWords)) {
            showFail();
        } else {
            registerForLocalBroadcast();
            startSaveRecoveryWordsService();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterForLocalBroadcast();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(cnServiceConnection);
        cnServiceConnection.setBounded(false);
    }

    private void startSaveRecoveryWordsService() {
        if (cnServiceConnection.isBounded()) {
            CNWalletServicesInterface cnServices = cnServiceConnection.getCNWalletServicesInterface();
            cnServices.saveSeedWords(recoveryWords);
        }
    }

    protected void registerForLocalBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED);
        intentFilter.addAction(DropbitIntents.ACTION_SAVE_RECOVERY_WORDS);
        intentFilter.addAction(DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS);
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    private void unRegisterForLocalBroadcast() {
        if (receiver != null) {
            localBroadCastUtil.unregisterReceiver(receiver);
        }
    }

    public void onSaveRecoveryWordsSuccess() {
        showSuccess();
    }

    public void onSaveRecoveryWordsFail() {
        showFail();
    }

    private void showSuccess() {
        icon.setImageResource(R.drawable.ic_restore_success);
        title.setText(getText(R.string.recover_wallet_success_title));
        button.setBackground(getResources().getDrawable(R.drawable.primary_button));
        message.setTextColor(getResources().getColor(R.color.font_default));
        message.setText(R.string.recover_wallet_success_message);
        findViewById(R.id.close).setVisibility(View.INVISIBLE);
        button.setOnClickListener(this::showNext);
        button.setText(R.string.recover_wallet_success_button_text);
    }

    private void showFail() {
        title.setText(getText(R.string.recover_wallet_error_title));
        icon.setImageResource(R.drawable.ic_restore_fail);
        button.setText(R.string.recover_wallet_error_button_text);
        button.setBackground(getResources().getDrawable(R.drawable.error_button));
        button.setOnClickListener(this::onErrorOk);
        View close = findViewById(R.id.close);
        close.setVisibility(View.VISIBLE);
        close.setOnClickListener(this::onClose);
        message.setText(R.string.recover_wallet_error_message);
        message.setTextColor(getResources().getColor(R.color.color_error));
    }

    private void onClose(View view) {
        navigateTo(StartActivity.class);
        finish();
    }

    private void showNext(View view) {
        Intent intent = new Intent(this, VerifyPhoneNumberActivity.class);
        navigateTo(intent);
        finish();
    }

    private void onErrorOk(View view) {
        navigateTo(RestoreWalletActivity.class);
        finish();
    }

    private boolean isValid(String[] recoveryWords) {
        return bitcoinUtil.isValidBIP39Words(recoveryWords);
    }

    class RecoverWalletReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DropbitIntents.ACTION_ON_SERVICE_CONNECTION_BOUNDED.equals(intent.getAction())) {
                startSaveRecoveryWordsService();
            } else if (DropbitIntents.ACTION_SAVE_RECOVERY_WORDS.equals(intent.getAction())) {
                onSaveRecoveryWordsSuccess();
            } else if (DropbitIntents.ACTION_UNABLE_TO_SAVE_RECOVERY_WORDS.equals(intent.getAction())) {
                onSaveRecoveryWordsFail();
            }
        }
    }
}
