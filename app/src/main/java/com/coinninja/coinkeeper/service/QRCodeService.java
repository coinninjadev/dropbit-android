package com.coinninja.coinkeeper.service;

import android.app.IntentService;
import android.content.Intent;

import com.coinninja.coinkeeper.util.DropbitIntents;
import com.coinninja.coinkeeper.util.android.LocalBroadCastUtil;
import com.coinninja.coinkeeper.util.file.FileOutputUtil;
import com.coinninja.coinkeeper.util.file.FileProviderUtil;
import com.coinninja.coinkeeper.util.file.FileUtil;
import com.coinninja.coinkeeper.util.file.QRFileManager;
import com.coinninja.coinkeeper.util.image.QRGeneratorUtil;
import com.google.zxing.qrcode.QRCodeWriter;

import androidx.annotation.Nullable;

public class QRCodeService extends IntentService {
    private static final String TAG = QRCodeService.class.getSimpleName();


    QRFileManager qrFileManager;
    LocalBroadCastUtil localBroadCastUtil;

    public QRCodeService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        qrFileManager = new QRFileManager(getApplicationContext(), new QRGeneratorUtil(new QRCodeWriter()), new FileUtil(new FileOutputUtil()), new FileProviderUtil());
        localBroadCastUtil = new LocalBroadCastUtil(getApplicationContext());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String dataToEncode = intent.getStringExtra(DropbitIntents.EXTRA_TEMP_QR_SCAN);

        if (qrFileManager.createQrCode(dataToEncode)) {
            Intent broadcastIntent = new Intent(DropbitIntents.ACTION_VIEW_QR_CODE);
            broadcastIntent.putExtra(DropbitIntents.EXTRA_QR_CODE_LOCATION, qrFileManager.getSharableURI().toString());
            localBroadCastUtil.sendBroadcast(broadcastIntent);
        }
    }
}


