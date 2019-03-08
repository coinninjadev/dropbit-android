package com.coinninja.coinkeeper.util.file;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import androidx.core.content.FileProvider;

public class FileProviderUtil {
    public Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context.getApplicationContext(),
                context.getPackageName() + ".provider",
                file
        );
    }
}
