package com.coinninja.coinkeeper.view.activity;

import android.os.Bundle;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.view.animation.StartScreenAnimation;

import javax.inject.Inject;

public class StartActivity extends BaseActivity {

    private StartScreenAnimation startScreenAnimation;

    @Override
    public void onBackPressed() {
        startScreenAnimation.animateOut();
        super.onBackPressed();
    }

    void setStartScreenAnimation(StartScreenAnimation startScreenAnimation) {
        this.startScreenAnimation = startScreenAnimation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        findViewById(R.id.start_btn_new_wallet).setOnClickListener(v -> newWalletButtonClicked());
        findViewById(R.id.start_btn_restore).setOnClickListener(v -> walletRestoreButtonClicked());
        findViewById(R.id.start_bitcoin_invite).setOnClickListener(v -> inviteButtonClicked());
        setStartScreenAnimation(new StartScreenAnimation(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScreenAnimation.animateIn();
    }

    private void inviteButtonClicked() {
        startInviteFlow();
    }

    private void newWalletButtonClicked() {
        startNewWalletFlow();
    }

    private void walletRestoreButtonClicked() {
        startRecoveryFlow();
    }
}
