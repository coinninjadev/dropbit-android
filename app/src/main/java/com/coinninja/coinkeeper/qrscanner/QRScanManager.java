package com.coinninja.coinkeeper.qrscanner;

import android.app.Activity;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.EnumSet;
import java.util.Set;

public class QRScanManager extends CaptureManager {
    private static final String TAG = QRScanManager.class.getSimpleName();

    private final DecoratedBarcodeView barcodeScannerView;
    private final OnScanListener onScanListener;
    private boolean isTorchOn = false;

    public QRScanManager(Activity activity, DecoratedBarcodeView barcodeScannerView, OnScanListener onScanListener) {
        super(activity, barcodeScannerView);
        this.barcodeScannerView = barcodeScannerView;
        this.onScanListener = onScanListener;

        Set<BarcodeFormat> formats = EnumSet.noneOf(BarcodeFormat.class);
        formats.add(BarcodeFormat.QR_CODE);
        barcodeScannerView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats, null, null));
        barcodeScannerView.getStatusView().setVisibility(android.view.View.INVISIBLE);

        barcodeScannerView.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                isTorchOn = true;
            }

            @Override
            public void onTorchOff() {
                isTorchOn = false;
            }
        });
    }

    @Override
    protected void returnResult(BarcodeResult rawResult) {
        String rawResultText = rawResult.getText();
        Log.d(TAG, rawResultText);
        onScanListener.onScanComplete(rawResultText);
    }

    public void toggleFlash() {
        if (!isTorchOn) {
            barcodeScannerView.setTorchOn();
        } else {
            barcodeScannerView.setTorchOff();
        }
    }

    public void startCapture() {
        CaptureManager captureManager = this;
        forceDecodingAndResume(captureManager);
    }

    public void stopCapture() {
        CaptureManager captureManager = this;
        captureManager.onPause();
    }

    private void forceDecodingAndResume(CaptureManager captureManager) {
        captureManager.decode();
        captureManager.onResume();
    }


    public interface OnScanListener {
        void onScanComplete(String rawScannedResult);
    }
}