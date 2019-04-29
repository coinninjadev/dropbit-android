package com.coinninja.coinkeeper.view.activity.base;

import android.content.Intent;
import android.os.Bundle;

import com.coinninja.coinkeeper.cn.wallet.CNWalletManager;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.interfaces.PinEntry;
import com.coinninja.coinkeeper.receiver.AuthenticationCompleteReceiver;
import com.coinninja.coinkeeper.service.WalletCreationIntentService;
import com.coinninja.coinkeeper.ui.phone.verification.VerifyPhoneNumberActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.view.activity.AuthenticateActivity;
import com.coinninja.coinkeeper.view.activity.CreatePinActivity;
import com.coinninja.coinkeeper.view.activity.RecoverWalletActivity;
import com.coinninja.coinkeeper.view.activity.RestoreWalletActivity;
import com.coinninja.coinkeeper.view.activity.StartActivity;
import com.coinninja.coinkeeper.view.activity.TrainingActivity;

import java.util.Arrays;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public abstract class SecuredActivity extends MessengerActivity {

    static final int AUTHENTICATION_REQUEST_CODE = 9999;

    @Inject
    protected CNWalletManager cnWalletManager;
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
            case AppCompatActivity.RESULT_CANCELED:
                moveTaskToBack(true);
                finish();
                break;
            case AppCompatActivity.RESULT_OK:
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
                showCreatePinCreateWalletThenVerifyPhone();
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
        localBroadCastUtil.sendGlobalBroadcast(AuthenticationCompleteReceiver.class, DropbitIntents.ACTION_ON_USER_AUTH_SUCCESSFULLY);
    }

    private void showStartActivity() {
        if (Arrays.asList(PIN_IGNORE_LIST).contains(getClass().getName())) return;
        navigateTo(StartActivity.class);
        finish();
    }

    protected void showCreatePinCreateWalletThenVerifyPhone() {
        cnWalletManager.createWallet();
        Intent intent = new Intent(this, CreatePinActivity.class);
        intent.putExtra(DropbitIntents.EXTRA_NEXT, VerifyPhoneNumberActivity.class.getName());
        intent.putExtra(DropbitIntents.EXTRA_ON_COMPLETION, new Intent(this, WalletCreationIntentService.class));
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
        onCompletion();
        if (getIntent().hasExtra(DropbitIntents.EXTRA_NEXT)) {
            try {
                Class<?> nextClass = Class.forName(getIntent().getStringExtra(DropbitIntents.EXTRA_NEXT));
                showNext(nextClass);

            } catch (ClassNotFoundException e) {
                showNext(StartActivity.class);
            }
        } else {
            showNext(StartActivity.class);
        }
    }


    protected void onCompletion() {
        if (getIntent() == null || getIntent().getExtras() == null) {
            return;
        }
        Intent completionIntent = (Intent) getIntent().getExtras().get(DropbitIntents.EXTRA_ON_COMPLETION);
        if (completionIntent != null) {
            startService(completionIntent);
        }
    }

    protected void showNext(@NonNull Class nextClass) {
        finish();
        Intent intent = new Intent(this, nextClass);
        if (getIntent().hasExtra(DropbitIntents.EXTRA_NEXT_BUNDLE)) {
            Bundle bundle = getIntent().getBundleExtra(DropbitIntents.EXTRA_NEXT_BUNDLE);
            intent.replaceExtras(bundle);
        }

        startActivity(intent);
    }

    protected PinEntry getPinEntry() {
        return pinEntry;
    }

}
