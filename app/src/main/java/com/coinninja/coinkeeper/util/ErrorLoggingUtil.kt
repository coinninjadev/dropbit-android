package com.coinninja.coinkeeper.util

import app.dropbit.annotations.Mockable
import com.coinninja.coinkeeper.interfaces.ErrorLogging
import com.crashlytics.android.Crashlytics
import javax.inject.Inject

@Mockable
class ErrorLoggingUtil @Inject constructor() : ErrorLogging {
    override fun logError(error: Throwable) {
        Crashlytics.logException(error)
    }
}