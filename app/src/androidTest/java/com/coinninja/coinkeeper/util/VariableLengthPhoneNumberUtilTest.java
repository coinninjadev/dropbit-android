package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.model.PhoneNumber;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class VariableLengthPhoneNumberUtilTest {

    VariableLengthPhoneNumberUtil variableLengthPhoneNumberUtil = new VariableLengthPhoneNumberUtil();
    Hasher hasher = new Hasher();

    PhoneNumber argentinaPrefixed = new PhoneNumber(54, "91123456789");
    PhoneNumber argentinaNonPrefixed = new PhoneNumber(54, "1123456789");

    PhoneNumber mexicanPrefixed = new PhoneNumber(52, "12221234567");
    PhoneNumber mexicanNonPrefixed = new PhoneNumber(52, "2221234567");

    PhoneNumber sanMarinoPrefixed = new PhoneNumber(378, "54966661212");
    PhoneNumber sanMarinoNonPrefixed = new PhoneNumber(378, "66661212");

    @Test
    public void test_optional_splice() {
        assertThat(argentinaPrefixed.getHashReadyPhoneNumber(), equalTo(argentinaNonPrefixed.getHashReadyPhoneNumber()));
        assertThat(mexicanPrefixed.getHashReadyPhoneNumber(), equalTo(mexicanNonPrefixed.getHashReadyPhoneNumber()));
        assertThat(sanMarinoPrefixed.getHashReadyPhoneNumber(), equalTo(sanMarinoNonPrefixed.getHashReadyPhoneNumber()));
    }

    @Test
    public void test_mexico_optional_splice_hash() {
        String expectedHash = "bc082b2e701d4fe3c96b270f88a626bc0cd33453aa1aa6042606fe3dcb33fdf0";
        assertTrue(expectedHash.equals(hasher.hash(mexicanPrefixed)));
    }

    @Test
    public void test_argentina_optional_splice_hash() {
        String expectedHash = "2d4c4bdfa189c8e8240b82788df37d77f7d95d4f87bc1511b1b7ad87139cef70";
        assertTrue(expectedHash.equals(hasher.hash(argentinaPrefixed)));
    }

    @Test
    public void test_san_marino_optional_splice_hash() {
        String expectedHash = "8497fccbb713e01ca298d15bb7fc919baed6fca102bd6089c07351ce220298f7";
        assertTrue(expectedHash.equals(hasher.hash(sanMarinoPrefixed)));
    }

}