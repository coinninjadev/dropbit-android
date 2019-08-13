package com.coinninja.coinkeeper.ui.phone.verification

import java.util.Arrays
import java.util.HashSet
import java.util.Locale

object ManualPhoneVerification {
    private val COUNTRIES_REQUIRING_MANUAL_SUBMISSION = HashSet(Arrays.asList(
            "AD", "AR", "AT", "AX", "BA", "BG", "BR", "CD", "CW",
            "DE", "EE", "FI", "GA", "HR", "ID", "IS", "IT", "IL", "KH",
            "KR", "LB", "LI", "LR", "MC", "MM", "MY", "NZ", "PA",
            "RS", "SB", "SO", "TK", "TV", "VA", "WS", "ZA", "ZW"))

    fun shouldManuallyVerify(locale: Locale): Boolean {
        return COUNTRIES_REQUIRING_MANUAL_SUBMISSION.contains(locale.country)
    }
}
