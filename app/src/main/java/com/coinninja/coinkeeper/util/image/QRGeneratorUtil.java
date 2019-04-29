package com.coinninja.coinkeeper.util.image;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.coinninja.coinkeeper.util.DropbitIntents;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QRGeneratorUtil {

    private QRCodeWriter qrCodeWriter;

    public QRGeneratorUtil(QRCodeWriter qrCodeWriter) {
        this.qrCodeWriter = qrCodeWriter;
    }

    public byte[] generateFrom(String data) throws WriterException {
        Bitmap bitmap = toBitmap(qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, DropbitIntents.QR_WIDTH, DropbitIntents.QR_HEIGHT));
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        compress(bitmap, byteStream);

        byte[] bytes = byteStream.toByteArray();

        try {
            byteStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    private void compress(Bitmap bitmap, ByteArrayOutputStream bytes) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bytes);
    }

    private Bitmap toBitmap(BitMatrix bitMatrix) {
        int height = bitMatrix.getHeight();
        int width = bitMatrix.getWidth();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
