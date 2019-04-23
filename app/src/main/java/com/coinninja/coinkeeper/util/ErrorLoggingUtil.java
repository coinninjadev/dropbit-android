package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.interfaces.ErrorLogging;
import com.crashlytics.android.Crashlytics;

public class ErrorLoggingUtil implements ErrorLogging {

    @Override
    public void logError(Throwable error) {
        Crashlytics.logException(error);
    }
}
