package com.coinninja.coinkeeper.view.activity;

import android.app.Activity;
import android.os.Bundle;

import com.coinninja.coinkeeper.qrscanner.QRScanManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import javax.inject.Inject;

class CNQRScanManager {

    private QRScanManager qrScanManager;

    @Inject
    CNQRScanManager() {

    }

    public void initializeFromIntent(Activity activity, DecoratedBarcodeView barcodeScannerView, QRScanManager.OnScanListener onScanComplete, Bundle savedInstanceState) {
        qrScanManager = new QRScanManager(activity, barcodeScannerView, onScanComplete);
        qrScanManager.initializeFromIntent(activity.getIntent(), savedInstanceState);
    }

    public void startCapture() {
        qrScanManager.startCapture();
    }

    public void stopCapture() {
        qrScanManager.startCapture();
    }

    public void toggleFlash() {
        qrScanManager.toggleFlash();
    }

    public void onDestroy() {
        qrScanManager.onDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        qrScanManager.onSaveInstanceState(outState);
    }
}
