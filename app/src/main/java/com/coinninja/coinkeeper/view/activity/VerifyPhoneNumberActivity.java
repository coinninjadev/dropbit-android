package com.coinninja.coinkeeper.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.db.PhoneNumber;
import com.coinninja.coinkeeper.service.RegisterUsersPhoneService;
import com.coinninja.coinkeeper.text.PhoneNumberFormattingTextWatcher;
import com.coinninja.coinkeeper.util.Intents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.google.i18n.phonenumbers.Phonenumber;

import androidx.annotation.Nullable;

public class VerifyPhoneNumberActivity extends SecuredActivity implements PhoneNumberFormattingTextWatcher.Callback {

    private EditText phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone);
        phoneNumber = findViewById(R.id.phone_number);
        phoneNumber.addTextChangedListener(new PhoneNumberFormattingTextWatcher(getResources().getConfiguration().locale, this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        showKeyboard(phoneNumber);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideError();
        clearPhoneNumber();
    }

    @Override
    public void onPhoneNumberValid(Phonenumber.PhoneNumber phoneNumber) {
        Intent serviceIntent = new Intent(this, RegisterUsersPhoneService.class);
        serviceIntent.putExtra(Intents.EXTRA_PHONE_NUMBER, new PhoneNumber(phoneNumber));
        startService(serviceIntent);

        Intent activityIntent = new Intent(this, VerifyPhoneVerificationCodeActivity.class);
        activityIntent.putExtra(Intents.EXTRA_PHONE_NUMBER, new PhoneNumber(phoneNumber));
        startActivity(activityIntent);
    }

    @Override
    public void onPhoneNumberInValid(String text) {
        clearPhoneNumber();
        showError();
    }

    private void hideError() {
        findViewById(R.id.error_message).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSkipClicked() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SKIPPED);
        }
        super.onSkipClicked();
    }


    private void showError() {
        findViewById(R.id.error_message).setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.shake_view);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        phoneNumber.startAnimation(animation);
        phoneNumber.postDelayed(new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
            }
        }, 250);
        long[] pattern = {25, 100, 25, 100};
        vibrator.vibrate(pattern, 0);
    }

    private void clearPhoneNumber() {
        phoneNumber.setText("");
    }
}
