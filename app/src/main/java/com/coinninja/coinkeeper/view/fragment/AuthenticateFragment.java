package com.coinninja.coinkeeper.view.fragment;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.presenter.fragment.AuthenticateFragmentPresenter;
import com.coinninja.coinkeeper.presenter.fragment.FingerprintAuthPresenter;
import com.coinninja.coinkeeper.ui.base.BaseFragment;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.edittext.PinEditText;

import javax.inject.Inject;

public class AuthenticateFragment extends BaseFragment implements AuthenticateFragmentPresenter.View,
        PinEditText.OnDismissRequestListener, PinEditText.OnSixDigitsEnteredListener,
        FingerprintAuthPresenter.View {

    public static final String IS_AUTHENTICATING_WITH_FINGERPRINT = "isAuthenticatingWithFingers";
    public static final String DIALOG_PREF_FINGERPRINT = "DIALOG_PREF_FINGERPRINT";

    @Inject
    AuthenticateFragmentPresenter authPresenter;
    FingerprintAuthDialog fingerprintAuthDialog;
    FingerprintAuthPresenter fingerprintAuthPresenter;

    boolean isMuted;
    private PinEditText pinInput;
    private View fingerPrintButton;
    private OnUserHasAuthenticated onUserHasAuthenticated;
    private TextView pinErrorDisplay;
    private boolean isAuthenticatingWithFingerprint = false;
    private boolean forceAuth = false;

    @Inject
    public AuthenticateFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintAuthPresenter = FingerprintAuthPresenter.newInstance(this);
            fingerprintAuthDialog = FingerprintAuthDialog.newInstance(fingerprintAuthPresenter, R.layout.dialog_fingerprint);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authenticate, container, false);
        pinInput = view.findViewById(R.id.pin_entry_edittext);
        pinErrorDisplay = view.findViewById(R.id.error_message);
        fingerPrintButton = view.findViewById(R.id.finger_btn_fragment_pin);

        pinInput.setOnDismissRequestListener(this);
        pinInput.setOnSixDigitsEnteredListener(this);
        fingerPrintButton.setEnabled(false);
        fingerPrintButton.setVisibility(View.INVISIBLE);
        pinErrorDisplay.setVisibility(View.INVISIBLE);
        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            isAuthenticatingWithFingerprint = savedInstanceState.getBoolean(IS_AUTHENTICATING_WITH_FINGERPRINT);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isMuted) return;

        if (isAuthenticatingWithFingerprint) {
            authenticateWithFingerprint();
        } else {
            authPresenter.startAuth(forceAuth);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_AUTHENTICATING_WITH_FINGERPRINT, isAuthenticatingWithFingerprint);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        authPresenter.onPause();
        if (fingerprintAuthDialog != null && fingerprintAuthDialog.isAdded()) {
            isAuthenticatingWithFingerprint = true;
            fingerprintAuthDialog.dismiss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        authPresenter.attach(this);
    }

    public void setOnUserHasAuthenticated(OnUserHasAuthenticated onUserHasAuthenticated) {
        this.onUserHasAuthenticated = onUserHasAuthenticated;
    }

    @Override
    public void userHasAuthenticated() {
        if (onUserHasAuthenticated == null) return;
        onUserHasAuthenticated.onAuthenticated();
    }

    @Override
    public void showFingerprintAuth() {
        fingerPrintButton.setOnClickListener(v -> fingerPrintButtonClicked());
        fingerPrintButton.setEnabled(true);
        fingerPrintButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showPinAuth() {
        forceShowSoftKey();
    }

    @Override
    public void onPinMismatch() {
        pinErrorDisplay.setText(getResources().getText(R.string.pin_mismatch_error));
        pinErrorDisplay.setVisibility(View.VISIBLE);
        pinInput.onError();
    }

    @Override
    public void onWalletLock() {
        pinInput.setEnabled(false);
        pinInput.setVisibility(View.INVISIBLE);
        pinErrorDisplay.setVisibility(View.INVISIBLE);
        pinInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((SecuredActivity) getActivity()).muteViewsWithMessage(getString(R.string.locked_out_message));
            }
        }, 100);
    }

    @Override
    public void authenticateWithFingerprint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintAuthPresenter.captureFingerprintAuth();
        }
    }

    @Override
    public void onWalletLockRemoved() {
        try {
            ((SecuredActivity) getActivity()).teardownMute();
        } catch (Exception e) {
            //pass
        }
        pinInput.setVisibility(View.VISIBLE);
        pinInput.setEnabled(true);
        authPresenter.startAuth(forceAuth);
    }

    @Override
    public void onSixDigits() {
        int[] enteredPin = pinInput.getPin();
        pinInput.clearPin();
        authPresenter.verifyPin(enteredPin);
    }

    @Override
    public FingerprintManager getFingerprintManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (FingerprintManager) getContext().getSystemService(Context.FINGERPRINT_SERVICE);
        }
        return null;
    }

    @Override
    public void showAuthenticateWithFingerprint() {
        if (getFragmentManager().findFragmentByTag(DIALOG_PREF_FINGERPRINT) != null) return;

        fingerprintAuthDialog.show(getFragmentManager(), DIALOG_PREF_FINGERPRINT);
    }

    @Override
    public void onFingerprintAuthenticationNotAvailable() {
        fingerPrintButton.setEnabled(false);
        fingerPrintButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onFingerprintAuthenticationComplete() {
        authPresenter.onFingerprintAuthenticated();
        fingerprintAuthDialog.dismiss();
    }

    @Override
    public void onFingerprintAuthenticationCanceled() {
        fingerprintAuthDialog.dismiss();
        forceShowSoftKey();
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void muteViews() {
        isMuted = true;

    }

    public void teardownMute() {
        isMuted = false;
        onResume();
    }

    public void forceShowSoftKey() {
        Handler runOnUI = new Handler(Looper.getMainLooper());
        runOnUI.postDelayed(() -> {
            if (pinInput == null) {
                return;
            }
            pinInput.requestFocus();
            if (this == null || getActivity() == null) {
                return;
            }
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(pinInput, InputMethodManager.SHOW_IMPLICIT);
        }, 500);
    }

    @Override
    public void onDismissRequest() {
        getActivity().finish();
    }

    private void fingerPrintButtonClicked() {
        authenticateWithFingerprint();
    }

    public interface OnUserHasAuthenticated {
        void onAuthenticated();
    }
}
