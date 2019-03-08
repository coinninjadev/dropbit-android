package com.coinninja.coinkeeper.util;


import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

public class PhoneNumberUtilTest {
    static final String I18N_PHONE = "+12345678901";
    static final long NATIONAL_NUMBER = 2345678901L;
    static final String NATIONAL_NUMBER_STRING = String.valueOf(NATIONAL_NUMBER);
    public static final int COUNTRY_CODE = 1;

    PhoneNumber phoneNumber;

    PhoneNumberUtil util;

    @Before
    public void setUp(){
        util = new PhoneNumberUtil();
        phoneNumber = new PhoneNumber();
        phoneNumber.setCountryCode(COUNTRY_CODE);
        phoneNumber.setNationalNumber(NATIONAL_NUMBER);
    }

    @Test
    public void i18n_toPhoneNumber() {
        PhoneNumber phoneNumber = util.toPhoneNumber(I18N_PHONE);
        assertThat(phoneNumber.getCountryCode(), equalTo(COUNTRY_CODE));
        assertThat(phoneNumber.getNationalNumber(), equalTo(NATIONAL_NUMBER));
    }

    @Test
    public void toPhoneNumber_null() {
        assertNull(util.toPhoneNumber((String) null));
    }

    @Test
    public void toPhoneNumber_empty() {
        assertNull(util.toPhoneNumber(""));
    }

    @Test
    public void i18Formatted(){
        String formatted = util.i18Formatted(phoneNumber);
        assertThat(formatted, equalTo(I18N_PHONE));
    }

    @Test
    public void i18Formatted_null(){
        assertNull(util.i18Formatted((PhoneNumber) null));
    }
}