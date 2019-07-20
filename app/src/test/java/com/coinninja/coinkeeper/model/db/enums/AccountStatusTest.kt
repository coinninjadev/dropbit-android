package com.coinninja.coinkeeper.model.db.enums

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AccountStatusTest {
    @Test
    fun init_from_string() {
        assertThat(AccountStatus.from("verified")).isEqualTo(AccountStatus.VERIFIED)
        assertThat(AccountStatus.from("pending-verification")).isEqualTo(AccountStatus.PENDING_VERIFICATION)
        assertThat(AccountStatus.from("random")).isEqualTo(AccountStatus.UNVERIFIED)
    }

    @Test
    fun status_to_string() {
        assertThat(AccountStatus.asString(AccountStatus.UNVERIFIED)).isEqualTo("unverified")
        assertThat(AccountStatus.asString(AccountStatus.PENDING_VERIFICATION)).isEqualTo("pending-verification")
        assertThat(AccountStatus.asString(AccountStatus.VERIFIED)).isEqualTo("verified")
    }

    @Test
    fun ordinal() {
        assertThat(AccountStatus.UNVERIFIED.id).isEqualTo(0)
        assertThat(AccountStatus.PENDING_VERIFICATION.id).isEqualTo(10)
        assertThat(AccountStatus.VERIFIED.id).isEqualTo(100)
    }
}