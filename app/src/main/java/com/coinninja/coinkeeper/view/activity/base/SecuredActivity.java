package com.coinninja.coinkeeper.view.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.receiver.AuthenticationCompleteReceiver;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.AuthenticateActivity;
import com.coinninja.coinkeeper.view.activity.CreatePinActivity;
import com.coinninja.coinkeeper.view.activity.RecoverWalletActivity;
import com.coinninja.coinkeeper.view.activity.RestoreWalletActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
import com.coinninja.coinkeeper.view.activity.TrainingActivity;
import com.coinninja.coinkeeper.view.activity.VerifyPhoneNumberActivity;

import java.util.Arrays;

import javax.inject.Inject;

import androidx.annotation.NonNull;

public abstract class SecuredActivity extends MessagegerActivity {

    static final int AUTHENTICATION_REQUEST_CODE = 9999;

    @Inject
    CNWalletManager cnWalletManager;
    @Inject
    PinEntry pinEntry;
    @Inject
    Authentication authentication;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;

    private static final String[] PIN_IGNORE_LIST = {
            CreatePinActivity.class.getName(),
            StartActivity.class.getName(),
            RestoreWalletActivity.class.getName(),
            RecoverWalletActivity.class.getName(),
            AuthenticateActivity.class.getName(),
            TrainingActivity.class.getName()
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AUTHENTICATION_REQUEST_CODE:
                onAuthenticationResult(resultCode);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void onAuthenticationResult(int resultCode) {
        switch (resultCode) {
            case Activity.RESULT_CANCELED:
                moveTaskToBack(true);
                finish();
                break;
            case Activity.RESULT_OK:
                if (!(authentication.isAuthenticated())) {
                    authenticate();
                } else {
                    broadcastAuthSuccessful();
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Arrays.asList(PIN_IGNORE_LIST).contains(getClass().getName()))
            return;

        boolean hasWallet = cnWalletManager.hasWallet();

        if (!authentication.isAuthenticated()) {
            if (!pinEntry.hasExistingPin() && !hasWallet) {
                showStartActivity();
            } else if (!pinEntry.hasExistingPin() && hasWallet
                    && !(getClass().getName().equals(CreatePinActivity.class.getName()))) {
                showCreatePinThenVerifyPhone();
            } else if (pinEntry.hasExistingPin() && hasWallet) {
                authenticate();
            }
        }
    }

    private void authenticate() {
        Intent intent = new Intent(this, AuthenticateActivity.class);
        startActivityForResult(intent, AUTHENTICATION_REQUEST_CODE);
    }

    private void broadcastAuthSuccessful() {
        localBroadCastUtil.sendGlobalBroadcast(AuthenticationCompleteReceiver.class, Intents.ACTION_ON_USER_AUTH_SUCCESSFULLY);
    }

    private void showStartActivity() {
        if (Arrays.asList(PIN_IGNORE_LIST).contains(getClass().getName())) return;
        navigateTo(StartActivity.class);
        finish();
    }

    private void showCreatePinThenVerifyPhone() {
        Intent intent = new Intent(this, CreatePinActivity.class);
        intent.putExtra(Intents.EXTRA_NEXT, VerifyPhoneNumberActivity.class.getName());
        navigateTo(intent);
        finish();
    }


    protected void navigateTo(Intent intent) {
        startActivity(intent);
    }

    protected void navigateTo(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    protected void showNext() {
        if (getIntent().hasExtra(Intents.EXTRA_NEXT)) {
            try {
                Class<?> nextClass = Class.forName(getIntent().getStringExtra(Intents.EXTRA_NEXT));
                showNext(nextClass);

            } catch (ClassNotFoundException e) {
                showNext(StartActivity.class);
            }
        } else {
            showNext(StartActivity.class);
        }
    }

    protected void showNext(@NonNull Class nextClass) {
        finish();
        Intent intent = new Intent(this, nextClass);
        if (getIntent().hasExtra(Intents.EXTRA_NEXT_BUNDLE)) {
            Bundle bundle = getIntent().getBundleExtra(Intents.EXTRA_NEXT_BUNDLE);
            intent.replaceExtras(bundle);
        }

        startActivity(intent);
    }

    protected PinEntry getPinEntry() {
        return pinEntry;
    }

}
