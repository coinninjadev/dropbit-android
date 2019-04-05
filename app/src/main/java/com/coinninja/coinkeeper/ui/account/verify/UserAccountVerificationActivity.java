package com.coinninja.coinkeeper.ui.account.verify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.helpers.WalletHelper;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.ui.settings.RemovePhoneNumberController;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class UserAccountVerificationActivity extends SecuredActivity {

    @Inject
    public RemovePhoneNumberController removePhoneNumberController;

    @Inject
    public WalletHelper walletHelper;

    @Inject
    public LocalBroadCastUtil localBroadCastUtil;

    public IntentFilter intentFilter;

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED.equals(intent.getAction())) {
                setupPhoneVerification();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_account_verification);
        intentFilter = new IntentFilter(Intents.ACTION_DEVERIFY_PHONE_NUMBER_COMPLETED);
    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
        removePhoneNumberController.onStart();
        setupOnClickListeners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removePhoneNumberController.onStop();
        localBroadCastUtil.unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupPhoneVerification();
    }

    private void verifyPhoneNumber() {
        startActivity(new Intent(this, VerifyPhoneNumberActivity.class));
    }

    private void setupPhoneVerification() {
        if (walletHelper.hasVerifiedAccount()) {
            setupVerifiedAccount();
        } else {
            setupUnVerifiedAccount();
        }
    }

    private void setupUnVerifiedAccount() {
        findViewById(R.id.unverified_phone_group).setVisibility(View.VISIBLE);
        findViewById(R.id.verified_phone_group).setVisibility(View.GONE);
    }

    private void setupOnClickListeners() {
        findViewById(R.id.verify_phone_button).setOnClickListener(V -> verifyPhoneNumber());
        findViewById(R.id.change_remove_button).setOnClickListener(removePhoneNumberController::onRemovePhoneNumber);
    }

    private void setupVerifiedAccount() {
        findViewById(R.id.unverified_phone_group).setVisibility(View.GONE);
        findViewById(R.id.verified_phone_group).setVisibility(View.VISIBLE);
        TextView phoneNumber = findViewById(R.id.verified_number_text_view);
        phoneNumber.setText(walletHelper.getUserAccount().getPhoneNumber().toNationalDisplayText());
    }
}