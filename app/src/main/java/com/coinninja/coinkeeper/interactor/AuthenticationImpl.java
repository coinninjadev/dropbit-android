package com.coinninja.coinkeeper.interactor;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;

import com.coinninja.coinkeeper.BuildConfig;
import com.coinninja.coinkeeper.CoinKeeperLifecycleListener;
import com.coinninja.coinkeeper.ForegroundStatusChangeReceiver;
import com.coinninja.coinkeeper.cn.wallet.SyncWalletManager;
import com.coinninja.coinkeeper.interfaces.Authentication;
import com.coinninja.coinkeeper.util.android.PreferencesUtil;

public class AuthenticationImpl implements Authentication, ForegroundStatusChangeReceiver {
    private static final long TIME_OUT_MS = BuildConfig.AUTHENTICATION_TIME_OUT_MS;

    Context context;
    PreferencesUtil preferencesUtil;
    Handler timeOutHandler;
    SyncWalletManager syncWalletManager;
    boolean isAuthenticated = false;
    private final Runnable timeOutRunnable = () -> onTimeout();

    public AuthenticationImpl(Context context, PreferencesUtil preferencesUtil,
                              Handler timeOutHandler, SyncWalletManager syncWalletManager,
                              CoinKeeperLifecycleListener coinKeeperLifecycleListener) {
        this.context = context;
        this.preferencesUtil = preferencesUtil;
        this.timeOutHandler = timeOutHandler;
        this.syncWalletManager = syncWalletManager;
        coinKeeperLifecycleListener.registerReceiver(this);
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    void startDeAuthentication() {
        cancelDeAuthentication();
        if (isAuthenticated) {
            timeOutHandler.postDelayed(timeOutRunnable, TIME_OUT_MS);
        }
    }

    void cancelDeAuthentication() {
        timeOutHandler.removeCallbacks(timeOutRunnable);
    }

    @Override
    public void setAuthenticated() {
        cancelDeAuthentication();
        isAuthenticated = true;
        syncWalletManager.schedule60SecondSync();
    }

    @Override
    public void forceDeAuthenticate() {
        cancelDeAuthentication();
        onTimeout();
    }

    @Override
    public boolean hasOptedIntoFingerprintAuth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected() &&
                    fingerprintManager.hasEnrolledFingerprints() && preferencesUtil.getBoolean(UserPreferences.PREFERENCE_FINGERPRINT)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBroughtToForeground() {
        cancelDeAuthentication();
    }

    @Override
    public void onSentToBackground() {
        startDeAuthentication();
    }

    private void onTimeout() {
        isAuthenticated = false;
        syncWalletManager.cancel60SecondSync();
    }
}
