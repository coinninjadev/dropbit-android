package com.coinninja.coinkeeper.model.db.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class IdentityTypeTest {
    @Test
    public void types_from_strings() {
        assertThat(IdentityType.from("phone"), equalTo(IdentityType.PHONE));
        assertThat(IdentityType.from("TWITTER"), equalTo(IdentityType.TWITTER));
        assertThat(IdentityType.from("twitter"), equalTo(IdentityType.TWITTER));
        assertThat(IdentityType.from(null), equalTo(IdentityType.UNKNOWN));
        assertThat(IdentityType.from(""), equalTo(IdentityType.UNKNOWN));
        assertThat(IdentityType.from("na"), equalTo(IdentityType.UNKNOWN));
    }

}