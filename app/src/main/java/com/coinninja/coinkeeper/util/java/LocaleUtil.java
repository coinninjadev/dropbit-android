package com.coinninja.coinkeeper.util.java;

import java.util.Locale;

import javax.inject.Inject;

public class LocaleUtil {
    @Inject
    LocaleUtil() {
    }

    public Locale[] getAvailableLocales() {
        return Locale.getAvailableLocales();
    }
}
