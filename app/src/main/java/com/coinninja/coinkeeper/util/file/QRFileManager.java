package com.coinninja.coinkeeper.util.file;

import android.content.Context;
import android.net.Uri;

import com.coinninja.coinkeeper.util.crypto.BitcoinUri;
import com.coinninja.coinkeeper.util.image.QRGeneratorUtil;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;

public class QRFileManager {
    static final String TMP_QR_DIIRECTORY = "tmp-qr";
    static final String QR_CODE_FILENAME = "qr_code_%s_%s.png";
    private Context context;
    private QRGeneratorUtil qrGeneratorUtil;
    private FileUtil fileUtil;
    private FileProviderUtil fileProviderUtil;


    public QRFileManager(Context context, QRGeneratorUtil qrGeneratorUtil, FileUtil fileUtil, FileProviderUtil fileProviderUtil) {
        this.context = context;
        this.qrGeneratorUtil = qrGeneratorUtil;
        this.fileUtil = fileUtil;
        this.fileProviderUtil = fileProviderUtil;
    }


    public Uri createQrCode(BitcoinUri uri) {

        try {
            byte[] qrCode = qrGeneratorUtil.generateFrom(uri.toString());
            File file = createCacheFile(uri);
            fileUtil.delete(file);
            fileUtil.createFile(file);
            fileUtil.writeBytes(qrCode, file);
            return fileProviderUtil.getUriForFile(context, file);
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @NonNull
    private File createCacheFile(BitcoinUri uri) {
        File directory = context.getExternalFilesDir(TMP_QR_DIIRECTORY);
        return new File(directory, String.format(QR_CODE_FILENAME, uri.getAddress(), uri.getSatoshiAmount()));
    }
}
