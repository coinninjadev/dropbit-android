package com.coinninja.coinkeeper.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.model.PhoneNumber;
import com.coinninja.coinkeeper.service.ResendPhoneVerificationService;
import com.coinninja.coinkeeper.service.SyncDropBitService;
import com.coinninja.coinkeeper.service.UserPhoneConfirmationService;
import com.coinninja.coinkeeper.text.TextInputNotifierWatcher;
import com.coinninja.coinkeeper.ui.dropbit.me.DropbitMeConfiguration;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.analytics.Analytics;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.android.activity.ActivityNavigationUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.dialog.GenericAlertDialog;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class VerifyPhoneVerificationCodeActivity extends SecuredActivity implements TextInputNotifierWatcher.OnInputEventListener, View.OnFocusChangeListener, DialogInterface.OnClickListener {
    public static final String EXPIRED_CODE_FRAGMENT_TAG = "EXPIRED_CODE_FRAGMENT_TAG";
    public static final String VERIFICATION_CODE_SENT = "VERIFICATION_CODE_SENT";
    public static final String TOO_MANY_ATTEMPTS_FRAGMENT_TAG = "TOO_MANY_ATTEMPTS_FRAGMENT_TAG";
    public static final String TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG = "TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG";
    public static final String SERVER_ERROR_FRAGMENT_TAG = "SERVER_ERROR_FRAGMENT_TAG";

    @Inject
    ActivityNavigationUtil activityNavigationUtil;
    @Inject
    LocalBroadCastUtil localBroadCastUtil;
    @Inject
    DropbitMeConfiguration dropbitMeConfiguration;

    private TextInputNotifierWatcher watcher;
    private ViewGroup parent;
    private int errorCount = 0;
    private TextView resend_link;
    private TextView error_message;
    private GenericAlertDialog alertDialog;
    private GenericAlertDialog tooManyDialog;
    private PhoneNumber phoneNumber;
    BroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_code);
        watcher = new TextInputNotifierWatcher(this);
        parent = findViewById(R.id.pin_wrapper);
        error_message = findViewById(R.id.error_message);

        phoneNumber = getIntent().getParcelableExtra(DropbitIntents.EXTRA_PHONE_NUMBER);
        TextView headline = findViewById(R.id.headline);
        String headlineCopy = headline.getResources().
                getString(R.string.activity_verify_phone_code_headline, phoneNumber.toInternationalDisplayText());
        headline.setText(headlineCopy);

        resend_link = findViewById(R.id.resend_link);
        resend_link.setOnClickListener(V -> onResendClick());
        resend_link.setPaintFlags(resend_link.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        receiver = new InvalidPhoneCodeReceiver();
        setup();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE);
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE);
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS);
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR);
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR);
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR);
        intentFilter.addAction(DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT);
        localBroadCastUtil.registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        parent.getChildAt(0).requestFocus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            localBroadCastUtil.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearAll();
    }

    private void setup() {
        for (int i = 0; i < parent.getChildCount(); i++) {
            parent.getChildAt(i).setOnFocusChangeListener(this);
        }
    }

    @Override
    public void onInput(int numValues) {
        hideErrors();
        EditText focusedChild = (EditText) parent.getFocusedChild();
        EditText next = (EditText) parent.getChildAt(parent.indexOfChild(focusedChild) + 1);
        if (null != next && numValues == 1) {
            focusOn(next);
        } else if (numValues > 1) {
            handlePaste(focusedChild);
        } else {
            validateCode(getCode());
        }
    }

    @Override
    public void onRemove(int numValues) {
        EditText focusedChild = (EditText) parent.getFocusedChild();
        EditText previous = (EditText) parent.getChildAt(parent.indexOfChild(focusedChild) - 1);
        if (null != previous) {
            focusOn(previous);
        }
    }

    @Override
    public void onAfterChanged(String text) {
        // NA
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            int focusedIndex = parent.indexOfChild(view);
            if (focusedIndex != 0) {
                clearForward(focusedIndex);
                addWatcher((EditText) view);
                EditText previous = (EditText) parent.getChildAt(focusedIndex - 1);
                if (null != previous && previous.getText().toString().isEmpty()) {
                    removeWatcher((EditText) view);
                    focusOn(previous);
                }
            } else {
                addWatcher((EditText) view);
            }
        } else {
            removeWatcher((EditText) view);
        }
    }

    private void addWatcher(EditText view) {
        view.addTextChangedListener(watcher);
        view.setOnKeyListener(watcher);
    }


    private void clearAll() {
        clearForward(0);
        focusOn((EditText) parent.getChildAt(0));
    }

    private void clearForward(int focusedIndex) {
        for (int i = focusedIndex; i < parent.getChildCount(); i++) {
            EditText text = (EditText) parent.getChildAt(i);
            if (null != text && !text.getText().toString().isEmpty()) {
                text.setText("");
            }
        }
    }

    private void focusOn(EditText view) {
        view.setText("");
        view.setFocusable(true);
        view.requestFocus();
        view.requestFocusFromTouch();
        view.setOnFocusChangeListener(this);
    }

    private String getCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < parent.getChildCount(); i++) {
            code.append(((EditText) parent.getChildAt(i)).getText().toString());
        }
        return code.toString();
    }

    private void handlePaste(EditText focusedChild) {
        int i = parent.indexOfChild(focusedChild);
        removeWatcher(focusedChild);
        char[] text = focusedChild.getText().toString().toCharArray();
        View view;
        for (int j = 0; j < text.length; i++, j++) {
            view = parent.getChildAt(i);
            if (null != view) {
                ((EditText) view).setText(String.valueOf(text[j]));
            }
        }

        if (i < parent.getChildCount()) {
            view = parent.getChildAt(i);
        } else {
            view = parent.getChildAt(parent.getChildCount() - 1);
        }

        view.setOnFocusChangeListener(null);
        view.requestFocusFromTouch();
        view.setOnFocusChangeListener(this);
    }

    private void onResendClick() {
        Intent intent = new Intent(this, ResendPhoneVerificationService.class);
        intent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER, phoneNumber);

        startService(intent);
    }

    private void onVerificationCodeSent() {
        alertDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.activity_verify_code_sent),
                "ok",
                null,
                this::onVerificationCodeSentClickListener,
                false,
                false
        );

        alertDialog.show(getSupportFragmentManager(), VERIFICATION_CODE_SENT);
    }

    private void onVerificationCodeSentClickListener(DialogInterface dialogInterface, int i) {
        parent.getChildAt(0).requestFocus();
    }

    private void onExpiredCode() {
        alertDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.activity_verify_phonecode_expired),
                "ok",
                null,
                this,
                false,
                false
        );

        alertDialog.show(getSupportFragmentManager(), EXPIRED_CODE_FRAGMENT_TAG);
    }

    private void onRateLimitErrorCode() {
        alertDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.activity_verify_phonecode_ratelimit),
                "ok",
                null,
                null,
                false,
                false
        );

        alertDialog.show(getSupportFragmentManager(), TOO_FAST_SERVER_ATTEMPTS_FRAGMENT_TAG);
    }


    private void onServerErrorCode() {
        alertDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.activity_verify_phonecode_error_server),
                "ok",
                null,
                null,
                false,
                false
        );

        alertDialog.show(getSupportFragmentManager(), SERVER_ERROR_FRAGMENT_TAG);
    }

    private void onServerBlacklistErrorCode() {
        alertDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.activity_verify_phonecode_error_server_blacklist),
                "ok",
                null,
                null,
                false,
                false
        );

        alertDialog.show(getSupportFragmentManager(), SERVER_ERROR_FRAGMENT_TAG);
    }

    public void onInvalidCode() {
        clearAll();

        error_message.setVisibility(View.VISIBLE);
        errorCount += 1;

        if (errorCount > 2) {
            hideErrors();
            notifyOfToManyAttempts();
        }
    }

    private void onSuccess() {
        if (analytics != null) {
            analytics.trackEvent(Analytics.EVENT_PHONE_VERIFICATION_SUCCESSFUL);
        }
        startBTCInvitesService();
        dropbitMeConfiguration.setNewlyVerified();
        activityNavigationUtil.navigateToHome(this);
    }

    private void startBTCInvitesService() {
        startService(new Intent(this, SyncDropBitService.class));
    }


    private void hideErrors() {
        error_message.setVisibility(View.GONE);
    }

    private void notifyOfToManyAttempts() {
        tooManyDialog = GenericAlertDialog.newInstance(
                null,
                getString(R.string.verify_phone_code_too_many_tries),
                "OK", null,
                this, false, false
        );
        tooManyDialog.show(getSupportFragmentManager(), TOO_MANY_ATTEMPTS_FRAGMENT_TAG);
    }

    private void removeWatcher(EditText view) {
        view.removeTextChangedListener(watcher);
        view.setOnKeyListener(null);
    }

    private void validateCode(String code) {
        Intent serviceIntent = new Intent(this, UserPhoneConfirmationService.class);
        serviceIntent.putExtra(DropbitIntents.EXTRA_PHONE_NUMBER_CODE, code);
        startService(serviceIntent);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        onResendClick();
        dialog.dismiss();
    }

    class InvalidPhoneCodeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (DropbitIntents.ACTION_PHONE_VERIFICATION__INVALID_CODE.equals(intent.getAction())) {
                onInvalidCode();
            } else if (DropbitIntents.ACTION_PHONE_VERIFICATION__EXPIRED_CODE.equals(intent.getAction())) {
                onExpiredCode();
            } else if (DropbitIntents.ACTION_PHONE_VERIFICATION__RATE_LIMIT_ERROR.equals(intent.getAction())) {
                onRateLimitErrorCode();
            } else if (DropbitIntents.ACTION_PHONE_VERIFICATION__CN_HTTP_ERROR.equals(intent.getAction())) {
                onServerErrorCode();
            } else if (DropbitIntents.ACTION_PHONE_VERIFICATION__CN_BLACKLIST_ERROR.equals(intent.getAction())) {
                onServerBlacklistErrorCode();
            } else if (DropbitIntents.ACTION_PHONE_VERIFICATION__CODE_SENT.equals(intent.getAction())) {
                onVerificationCodeSent();
            } else if (DropbitIntents.ACTION_PHONE_VERIFICATION__SUCCESS.equals(intent.getAction())) {
                onSuccess();
            }
        }
    }
}
