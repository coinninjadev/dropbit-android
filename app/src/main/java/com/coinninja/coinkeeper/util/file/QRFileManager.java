package com.coinninja.coinkeeper.util.file;

import android.content.Context;
import android.net.Uri;

import com.coinninja.coinkeeper.util.image.QRGeneratorUtil;
import com.google.zxing.WriterException;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;

public class QRFileManager {
    public static final String TMP_QR_DIIRECTORY = "tmp-qr";
    public static final String QR_CODE_FILENAME = "qr_code.png";
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


    public boolean createQrCode(String data) {
        boolean created = false;

        try {
            byte[] qrCode = qrGeneratorUtil.generateFrom(data);
            File file = createCacheFile();
            fileUtil.delete(file);
            fileUtil.createFile(file);
            fileUtil.writeBytes(qrCode, file);
            created = true;
        } catch (WriterException | IOException e) {
            e.printStackTrace();
        }

        return created;
    }

    @NonNull
    private File createCacheFile() {
        File directory = context.getExternalFilesDir(TMP_QR_DIIRECTORY);
        File file = new File(directory, QR_CODE_FILENAME);
        return file;
    }

    public Uri getSharableURI() {
        return fileProviderUtil.getUriForFile(context, createCacheFile());
    }
}
