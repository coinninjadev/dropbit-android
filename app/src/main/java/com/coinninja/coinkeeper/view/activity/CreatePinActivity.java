package com.coinninja.coinkeeper.view.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.presenter.PreferencePresentor;
import com.coinninja.coinkeeper.presenter.fragment.FingerprintAuthPresenter;
import com.coinninja.coinkeeper.presenter.fragment.PinFragmentPresenter;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;
import com.coinninja.coinkeeper.view.activity.base.SecuredActivity;
import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;
import com.coinninja.coinkeeper.view.fragment.PinConfirmFragment;
import com.coinninja.coinkeeper.view.fragment.PinCreateFragment;
import com.coinninja.coinkeeper.view.util.AlertDialogBuilder;

import javax.inject.Inject;

import androidx.annotation.Nullable;

public class CreatePinActivity extends SecuredActivity implements PinFragmentPresenter.View, FingerprintAuthPresenter.View, PreferencePresentor.View {

    public static final String TAG_CREATE_PIN = "CREATE_PIN";
    public static final String TAG_CONFIRM_PIN = "CONFIRM_PIN";

    private PinFragmentPresenter pinFragmentPresenter;
    FingerprintAuthPresenter fingerprintAuthPresenter;

    PinCreateFragment pinFragment;
    public PinConfirmFragment confirmFragment;
    FingerprintAuthDialog fingerprintAuthDialog;
    PreferencePresentor preferencePresentor;
    boolean isAuthenticatingWithFingerprint = false;

    @Inject
    Authentication authentication;
    @Inject
    PreferencesUtil preferencesUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        pinFragmentPresenter = PinFragmentPresenter.newInstance(getPinEntry());
    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistableBundle) {
        isAuthenticatingWithFingerprint = savedInstanceState.getBoolean("isAuthenticatingWithFingerprint", false);
        super.onRestoreInstanceState(savedInstanceState, persistableBundle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean("isAuthenticatingWithFingerprint", isAuthenticatingWithFingerprint);

        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pinFragmentPresenter.attachView(this);
        requestPin();
        if (isAuthenticatingWithFingerprint) authenticateWithFingerprint();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (fingerprintAuthDialog != null && isAuthenticatingWithFingerprint) {
            authenticationCompleted();
            isAuthenticatingWithFingerprint = true;
        }
    }

    private void requestPin() {
        if (getPinEntry().hasExistingPin()) {
            showNext();
        } else {
            showCreatePin();
        }
    }

    public void showCreatePin() {
        pinFragment = new PinCreateFragment();
        pinFragment.setPresenter(pinFragmentPresenter);
        replaceFragment(pinFragment, TAG_CREATE_PIN);
    }

    @Override
    public void showConfirmPin() {
        confirmFragment = new PinConfirmFragment();
        confirmFragment.setPresenter(pinFragmentPresenter);
        addFragment(confirmFragment, TAG_CONFIRM_PIN);
    }

    @Override
    public void forceSoftKey() {
        if (pinFragment != null) {
            pinFragment.forceShowSoftKey();
        }
    }

    // Checking Required API in class and when grabbing the manager itself
    @SuppressLint("NewApi")
    @Override
    public void onPinConfirmedAndSaved(String userPinHashed) {
        pinFragmentPresenter.clearPin();
        authentication.setAuthenticated();
        authenticateWithFingerprint();
    }

    @Override
    public void onPinMismatch() {
        confirmFragment.showPinMismatch();
    }

    @Override
    public void onPinMismatchFATAL() {
        pinFragmentPresenter.clearPin();
        confirmFragment.onDismissRequest();

        AlertDialog.Builder alertDialogBuilder = AlertDialogBuilder
                .build(this, getResources().getString(R.string.pin_mismatch_error_fatal));
        AlertDialog alertDialog = alertDialogBuilder.show();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
    }

    private void replaceFragment(Fragment currentFragment, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.authenticate_frame, currentFragment, tag);
        fragmentTransaction.commit();
    }

    private void addFragment(Fragment currentFragment, String tag) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.authenticate_frame, currentFragment, tag);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }


    /**
     * This is a very rare situation. In the case that the user entered a malformed pin such as a pin
     * with four digits or a pin where each digit has multiple values. The way the application is designed,
     * it should be impossible for a malformed pin. But if a malicious actor connected the Android device
     * to some sort of workstation, it may be possible to bypass the on-screen keyboard and enter keystrokes
     * bypassing any protections. If this method is called then that means somehow the inputted pin is
     * not formatted in the way we expected. Again this is a very very rare case and should never happen but
     * we protect against it just in case.
     *
     * @param msg
     */
    @Override
    public void onMalformedPin(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public FingerprintManager getFingerprintManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getSystemService(FingerprintManager.class);
        }
        return null;
    }

    @Override
    public void showAuthenticateWithFingerprint() {
        fingerprintAuthDialog = FingerprintAuthDialog.newInstance(fingerprintAuthPresenter, R.layout.dialog_fingerprint_preference);
        fingerprintAuthDialog.show(getFragmentManager(), "DIALOG_PREF_FINGERPRINT");
    }

    @Override
    public void onFingerprintAuthenticationNotAvailable() {
        isAuthenticatingWithFingerprint = false;
        showNext();
    }

    @Override
    public void onFingerprintAuthenticationComplete() {
        preferencePresentor.usersFingerprintAuthPreferenceCaptured(true);
        authenticationCompleted();
    }

    @Override
    public void onFingerprintAuthenticationCanceled() {
        authenticationCompleted();
        showNext();
    }

    @Override
    public void preferenceCaptureComplete(PreferencePresentor.Preference which) {
        showNext();
    }

    public void authenticateWithFingerprint() {
        isAuthenticatingWithFingerprint = true;
        preferencePresentor = PreferencePresentor.newInstance(this, preferencesUtil);
        fingerprintAuthPresenter = FingerprintAuthPresenter.newInstance(this);
        fingerprintAuthPresenter.captureFingerprintAuth();
    }

    protected void authenticationCompleted() {
        fingerprintAuthDialog.dismiss();
        isAuthenticatingWithFingerprint = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            finish();
        }
    }

    void setPinFragmentPresenter(PinFragmentPresenter pinFragmentPresenter) {
        this.pinFragmentPresenter = pinFragmentPresenter;
    }

    void setFingerPrintAuthDialog(FingerprintAuthDialog fingerPrintAuthDialog) {
        fingerprintAuthDialog = fingerPrintAuthDialog;
    }
}
