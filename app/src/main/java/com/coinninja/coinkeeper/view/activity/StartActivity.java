package com.coinninja.coinkeeper.view.activity;

import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.animation.StartScreenAnimation;

import javax.inject.Inject;

public class StartActivity extends SecuredActivity {

    private StartScreenAnimation startScreenAnimation;

    @Inject
    CNWalletManager cnWalletManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViewById(R.id.start_btn_new_wallet).setOnClickListener(v -> createWordsAndSkipBackup());
        findViewById(R.id.start_btn_restore).setOnClickListener(v -> gotoRestoreWalletActivity());
        setStartScreenAnimation(new StartScreenAnimation(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScreenAnimation.animateIn();
    }

    @Override
    public void onBackPressed() {
        startScreenAnimation.animateOut();
        super.onBackPressed();
        finish();
    }

    void setStartScreenAnimation(StartScreenAnimation startScreenAnimation) {
        this.startScreenAnimation = startScreenAnimation;
    }

    private void createWordsAndSkipBackup() {
        showCreatePinCreateWalletThenVerifyPhone();
    }

    private void gotoRestoreWalletActivity() {
        cnWalletManager.createWallet();
        navigateTo(RestoreWalletActivity.class);
    }
}
