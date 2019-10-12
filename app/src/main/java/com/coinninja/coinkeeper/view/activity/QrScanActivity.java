package com.coinninja.coinkeeper.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.coinninja.coinkeeper.R;
import com.coinninja.coinkeeper.ui.base.BaseActivity;
import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.PermissionsUtil;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import javax.inject.Inject;

public class QrScanActivity extends BaseActivity {

    @Inject
    CNQRScanManager qrScanManager;
    @Inject
    PermissionsUtil permissionsUtil;
    private DecoratedBarcodeView barcodeScannerView;
    private Bundle savedInstanceState;

    public void onScanComplete(String rawScannedResult) {
        Intent returnIntent = new Intent();
        try {
            returnIntent.putExtra(DropbitIntents.EXTRA_SCANNED_DATA, rawScannedResult);
            setResult(DropbitIntents.RESULT_SCAN_OK, returnIntent);
            finish();
        } catch (IllegalArgumentException ex) {
            onScanError();
        }
    }

    public void onScanError() {
        setResult(DropbitIntents.RESULT_SCAN_ERROR);
        finish();
    }

    public void requestCameraPermission() {
        permissionsUtil.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                DropbitIntents.REQUEST_PERMISSIONS_CAMERA);
    }

    public boolean hasCameraPermission() {
        return permissionsUtil.hasPermission(Manifest.permission.CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == DropbitIntents.REQUEST_PERMISSIONS_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                removeReAskPermissionBtn();
                qrScanManager.startCapture();
            } else {
                showReAskPermissionBtn();
            }
        }
    }

    @Override
    public void onCloseClicked() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_qr_scanner);
        this.savedInstanceState = savedInstanceState;
        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner);
        View flashView = findViewById(R.id.qr_scan_flash_btn_view);
        View flashBtn = findViewById(R.id.qr_scan_flash_btn);
        flashView.setOnClickListener(v -> onFlashPressed());
        flashBtn.setOnClickListener(v -> onFlashPressed());

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!hasCameraPermission()) {
            requestCameraPermission();
        }
        qrScanManager.initializeFromIntent(this, barcodeScannerView, this::onScanComplete, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (hasCameraPermission()) {
            qrScanManager.stopCapture();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasCameraPermission()) {
            qrScanManager.startCapture();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        qrScanManager.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        qrScanManager.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    private void onFlashPressed() {
        qrScanManager.toggleFlash();
    }

    private void showReAskPermissionBtn() {
        View reAskBtn = findViewById(R.id.qr_scan_reask_permission_btn);
        reAskBtn.setOnClickListener(v -> requestCameraPermission());

        reAskBtn.setVisibility(View.VISIBLE);
    }

    private void removeReAskPermissionBtn() {
        View reAskBtn = findViewById(R.id.qr_scan_reask_permission_btn);

        reAskBtn.setVisibility(View.GONE);
    }
}
