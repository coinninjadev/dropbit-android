package com.coinninja.coinkeeper.ui.phone.verification;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManualPhoneVerificationTest {

    @Test
    public void new_zealand_is_manual() {
        assertTrue(ManualPhoneVerification.shouldManuallyVerify(new Locale("en","NZ")));
    }

    @Test
    public void united_states_is_not_manual() {
        assertFalse(ManualPhoneVerification.shouldManuallyVerify(new Locale("en","US")));
    }
}