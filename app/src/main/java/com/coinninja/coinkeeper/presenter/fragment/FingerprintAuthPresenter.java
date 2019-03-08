package com.coinninja.coinkeeper.presenter.fragment;


import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

import com.coinninja.coinkeeper.view.fragment.FingerprintAuthDialog;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthPresenter extends FingerprintManager.AuthenticationCallback implements FingerprintAuthDialog.FingerprintAuthUIPresentor {

    private FingerprintManager fingerprintManager = null;
    private FingerprintManager.CryptoObject cryptoObject;
    private FingerprintAuthDialog dialog;
    private CancellationSignal cancellationSignal;
    private FingerprintAuthPresenter.View view;

    public static FingerprintAuthPresenter newInstance(FingerprintAuthPresenter.View view) {
        return new FingerprintAuthPresenter(view);
    }

    public FingerprintAuthPresenter(FingerprintAuthPresenter.View view) {
        this.view = view;
        fingerprintManager = view.getFingerprintManager();
        cancellationSignal = FingerprintAuthPresenter.newCancellationSignal();
    }

    public static CancellationSignal newCancellationSignal() {
        return new CancellationSignal();
    }

    @Override
    public void setDialog(FingerprintAuthDialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        dialog.onSucces();
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        dialog.onError(errorCode, errString);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        dialog.onFailure();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
        dialog.onHelp(helpCode, helpString);
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

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void stopListeningForTouch() {
        cancellationSignal.cancel();
    }

    public void tearDown() {
        view = null;
    }

    private boolean hasFingerprintSupport() {
        return fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
    }

    public interface View {

        FingerprintManager getFingerprintManager();

        void showAuthenticateWithFingerprint();

        void onFingerprintAuthenticationNotAvailable();

        void onFingerprintAuthenticationComplete();

        void onFingerprintAuthenticationCanceled();

    }
}
