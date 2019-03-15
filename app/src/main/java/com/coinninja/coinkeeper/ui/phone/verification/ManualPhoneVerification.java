package com.coinninja.coinkeeper.ui.phone.verification;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ManualPhoneVerification {
    private static final Set<String> COUNTRIES_REQUIRING_MANUAL_SUBMISSION = new HashSet<>(Arrays.asList(
            "AD", "AR", "AT", "AX", "BA", "BG", "BR", "CD", "CW",
            "DE", "EE", "FI", "GA", "HR", "ID", "IS", "IT", "KH",
            "KR", "LB", "LI", "LR", "MC", "MM", "MY", "NZ", "PA",
            "RS", "SB", "SO", "TK", "TV", "VA", "WS", "ZA", "ZW"));

    public static boolean shouldManuallyVerify(Locale locale) {
        return COUNTRIES_REQUIRING_MANUAL_SUBMISSION.contains(locale.getCountry());
    }
}
