package com.coinninja.coinkeeper.model.db.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class IdentityTypeTest {
    @Test
    public void types_from_strings() {
        assertThat(IdentityType.Companion.from("phone"), equalTo(IdentityType.PHONE));
        assertThat(IdentityType.Companion.from("TWITTER"), equalTo(IdentityType.TWITTER));
        assertThat(IdentityType.Companion.from("twitter"), equalTo(IdentityType.TWITTER));
        assertThat(IdentityType.Companion.from(null), equalTo(IdentityType.UNKNOWN));
        assertThat(IdentityType.Companion.from(""), equalTo(IdentityType.UNKNOWN));
        assertThat(IdentityType.Companion.from("na"), equalTo(IdentityType.UNKNOWN));
    }

}