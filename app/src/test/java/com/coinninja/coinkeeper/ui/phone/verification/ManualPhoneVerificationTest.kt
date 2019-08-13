package com.coinninja.coinkeeper.ui.phone.verification

import org.junit.Test

import java.util.Locale

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ManualPhoneVerificationTest {

    @Test
    fun new_zealand_is_manual() {
        assertTrue(ManualPhoneVerification.shouldManuallyVerify(Locale("en", "NZ")))
    }

    @Test
    fun united_states_is_not_manual() {
        assertFalse(ManualPhoneVerification.shouldManuallyVerify(Locale("en", "US")))
    }
}