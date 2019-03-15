package com.coinninja.coinkeeper.ui.phone.verification;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ManualPhoneVerification {
    private static final List<String> COUNTRIES_REQUIRING_MANUAL_SUBMISSION = Arrays.asList(
            "AD", "AR", "AT", "AX", "BA", "BG", "BR", "CD", "CW",
            "DE", "EE", "FI", "GA", "HR", "ID", "IS", "IT", "KH",
            "KR", "LB", "LI", "LR", "MC", "MM", "MY", "NZ", "PA",
            "RS", "SB", "SO", "TK", "TV", "VA", "WS", "ZA", "ZW");

    public static boolean shouldManuallyVerify(Locale locale) {
        return COUNTRIES_REQUIRING_MANUAL_SUBMISSION.contains(locale.getCountry());
    }
}
