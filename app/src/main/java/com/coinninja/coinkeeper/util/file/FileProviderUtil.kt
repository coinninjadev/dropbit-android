package com.coinninja.coinkeeper.util.file

import android.content.Context
import android.net.Uri

import androidx.core.content.FileProvider
import app.dropbit.annotations.Mockable

import java.io.File

@Mockable
class FileProviderUtil {
    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context.applicationContext,
                context.packageName + ".provider",
                file
        )
    }
}
