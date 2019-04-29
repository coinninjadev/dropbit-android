package com.coinninja.coinkeeper.presenter.fragment;


import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.RequiresApi;

import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintAuthPresenter extends FingerprintManager.AuthenticationCallback implements FingerprintAuthDialog.FingerprintAuthUIPresentor {

    CancellationSignal cancellationSignal;
    FingerprintManager fingerprintManager = null;
    FingerprintManager.CryptoObject cryptoObject;
    private FingerprintAuthDialog dialog;
    private FingerprintAuthPresenter.View view;

    public FingerprintAuthPresenter(FingerprintAuthPresenter.View view) {
        this.view = view;
        fingerprintManager = view.getFingerprintManager();
        cancellationSignal = FingerprintAuthPresenter.newCancellationSignal();
    }

    public static FingerprintAuthPresenter newInstance(FingerprintAuthPresenter.View view) {
        return new FingerprintAuthPresenter(view);
    }

    public static CancellationSignal newCancellationSignal() {
        return new CancellationSignal();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        dialog.onError(errorCode, errString);
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        dialog.onHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        dialog.onSucces();
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        dialog.onFailure();
    }

    public void captureFingerprintAuth() {

        if (hasFingerprintSupport())
            view.showAuthenticateWithFingerprint();
        else
            view.onFingerprintAuthenticationNotAvailable();
    }

    @Override
    public void onAuthCancel() {
        stopListeningForTouch();
        view.onFingerprintAuthenticationCanceled();
    }

    @Override
    public void onSuccessfulTransition() {
        view.onFingerprintAuthenticationComplete();
    }

    @Override
    public void startListeningForTouch() {
        if (!hasFingerprintSupport()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        }
    }

    @Override
    public void stopListeningForTouch() {
        cancellationSignal.cancel();
    }

    @Override
    public void setDialog(FingerprintAuthDialog dialog) {
        this.dialog = dialog;
    }

    public void tearDown() {
        view = null;
    }

    private boolean hasFingerprintSupport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        }
        return false;
    }

    public interface View {

        FingerprintManager getFingerprintManager();

        void showAuthenticateWithFingerprint();

        void onFingerprintAuthenticationNotAvailable();

        void onFingerprintAuthenticationComplete();

        void onFingerprintAuthenticationCanceled();

    }
}
