package com.coinninja.coinkeeper.view.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interactor.PinInteractor;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.edittext.PinEditText;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import static com.coinninja.coinkeeper.util.Intents.EXTRA_AUTHORIZED_ACTION_MESSAGE;

public class AuthorizedActionActivity extends SecuredActivity implements PinEditText.OnSixDigitsEnteredListener {

    public static final int MAX_ATTEMPTS = 2;

    public static final int FLAG_PIN_ONLY = 1;

    public static final int RESULT_AUTHORIZED = 8;

    @Inject
    PinInteractor pinInteractor;
    private PinEditText pin;
    private int errorCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_authenticate);
        pin = findViewById(R.id.pin_entry_edittext);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            displayAuthMessage(extras);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pin.setOnSixDigitsEnteredListener(this);
        showKeyboard(pin);
    }

    public void onAuthorized() {
        setResult(RESULT_AUTHORIZED);
        finish();
    }

    @Override
    public void onSixDigits() {
        if (pinInteractor.hashThenVerify(pin.getText().toString())) {
            onAuthorized();
        } else {
            onNotAuthorized();
        }
    }

    private void onNotAuthorized() {
        if (errorCount < MAX_ATTEMPTS) {
            showStandardError();
        } else {
            showCanceledError();
        }
    }

    public void displayAuthMessage(Bundle extras) {
        String authorizedActionMessage = extras.getString(EXTRA_AUTHORIZED_ACTION_MESSAGE);

        TextView authMessageDisplay = findViewById(R.id.authenticate_message_textview);
        authMessageDisplay.setVisibility(View.VISIBLE);
        authMessageDisplay.setText(authorizedActionMessage);
    }

    private void showStandardError() {
        TextView errorMessage = findViewById(R.id.error_message);
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setText(getText(R.string.pin_mismatch_error));
        pin.setText("");
        errorCount += 1;
    }

    private void showCanceledError() {
        Toast.makeText(this, getText(R.string.pin_mismatch_error_toast_fatal_re_enter), Toast.LENGTH_LONG).show();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
