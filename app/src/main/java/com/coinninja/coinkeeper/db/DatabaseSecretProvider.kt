package com.coinninja.coinkeeper.db

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.di.interfaces.AppSecret
import com.coinninja.coinkeeper.di.interfaces.DefaultSecret
import com.coinninja.coinkeeper.util.Hasher
import javax.inject.Inject

@Mockable
class DatabaseSecretProvider internal constructor(
        internal val hasher: Hasher,
        @AppSecret internal val appSecret: CharArray,
        @DefaultSecret internal val default: CharArray
) {

    val secret: CharArray
        get() = if (VERSION.SDK_INT < VERSION_CODES.O)
            default
        else
            hasher.hash(appSecret)

}