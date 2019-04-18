package com.coinninja.coinkeeper.util;


import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class PhoneNumberUtilTest {
    private static final int COUNTRY_CODE = 1;
    private static final String I18N_PHONE = "+12345678901";
    private static final long NATIONAL_NUMBER = 2345678901L;
    private PhoneNumber phoneNumber;
    private PhoneNumberUtil util;

    @Before
    public void setUp() {
        util = new PhoneNumberUtil();
        phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode(COUNTRY_CODE);
        phoneNumber.setNationalNumber(NATIONAL_NUMBER);
    }

    @After
    public void tearDown() {
        util = null;
        phoneNumber = null;
    }

    @Test
    public void i18n_toPhoneNumber() {
        PhoneNumber phoneNumber = util.toPhoneNumber(I18N_PHONE);
        assertThat(phoneNumber.getCountryCode(), equalTo(COUNTRY_CODE));
        assertThat(phoneNumber.getNationalNumber(), equalTo(NATIONAL_NUMBER));
    }

    @Test
    @Config(qualifiers = "es-rAR")
    public void toPhoneNumber_from_international_number() {
        PhoneNumber phoneNumber = util.toPhoneNumber("+5491123456789");
        assertThat(phoneNumber.getCountryCode(), equalTo(54));
        assertThat(phoneNumber.getNationalNumber(), equalTo(91123456789L));

        phoneNumber = util.toPhoneNumber("5491123456789");
        assertThat(phoneNumber.getNationalNumber(), equalTo(91123456789L));
        assertThat(phoneNumber.getCountryCode(), equalTo(54));

        phoneNumber = util.toPhoneNumber("91123456789");
        assertThat(phoneNumber.getNationalNumber(), equalTo(91123456789L));
        assertThat(phoneNumber.getCountryCode(), equalTo(54));

        phoneNumber = util.toPhoneNumber("123456789");
        assertThat(phoneNumber.getNationalNumber(), equalTo(123456789L));
        assertThat(phoneNumber.getCountryCode(), equalTo(54));
    }

    @Test
    public void toPhoneNumber_empty() {
        assertNull(util.toPhoneNumber(""));
    }

    @Test
    public void i18Formatted() {
        String formatted = util.i18Formatted(phoneNumber);
        assertThat(formatted, equalTo(I18N_PHONE));
    }

    @Test
    public void i18Formatted_null() {
        assertNull(util.i18Formatted((PhoneNumber) null));
    }

    @Test
    public void formats_international_number() {
        Phonenumber.PhoneNumber number = new Phonenumber.PhoneNumber();
        number.setNationalNumber(91123456789L);
        number.setCountryCode(54);

        assertThat(util.toInternationalDisplayText(number), equalTo("+54 9 11 2345-6789"));
    }

    @Test
    public void is_valid_number_returns_false_on_null_string() {
        assertFalse(util.isValidNumber(null));

    }

    @Test
    public void is_valid_number_returns_false_on_empty_string() {
        assertFalse(util.isValidNumber(""));
    }

}