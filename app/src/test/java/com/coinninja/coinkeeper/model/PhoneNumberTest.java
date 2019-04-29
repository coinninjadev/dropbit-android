package com.coinninja.coinkeeper.model;

import android.os.Parcel;

import com.google.i18n.phonenumbers.Phonenumber;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class PhoneNumberTest {

    public static final String I18N = "+12345678901";
    public static final String I18N_INTERNATIONAL = "+5491123456789";

    @Test
    public void wraps_android_phoneNumber() {
        PhoneNumber phoneNumber = new PhoneNumber(I18N);
        assertThat(phoneNumber.toString(), equalTo(I18N));
    }

    @Test
    public void create_from_empty_string() {
        PhoneNumber phoneNumber = new PhoneNumber(I18N);
        assertThat(phoneNumber.toString(), equalTo(I18N));
    }

    @Test
    public void isValid_no_phone() {
        assertFalse(new PhoneNumber((Phonenumber.PhoneNumber) null).isValid());
    }

    @Test
    public void isValid() {
        assertTrue(new PhoneNumber("+5491123456789").isValid());
        assertFalse(new PhoneNumber("+54123456789").isValid());
    }

    @Test
    public void returns_national_format_when_country_matches() {
        PhoneNumber phoneNumber = new PhoneNumber(I18N);
        assertTrue(phoneNumber.displayTextForLocale().equals(phoneNumber.toNationalDisplayText()));
    }

    @Test
    public void returns_international_format_when_country_matches() {
        PhoneNumber phoneNumber = new PhoneNumber(I18N_INTERNATIONAL);
        assertTrue(phoneNumber.displayTextForLocale().equals(phoneNumber.toInternationalDisplayText()));
    }

    @Test
    public void inits_with_number() {
        PhoneNumber phoneNumber = new PhoneNumber(1, "3305551111");

        assertThat(phoneNumber.toInternationalDisplayText(), equalTo("+1 330-555-1111"));
    }

    @Test
    public void inits_with_international_number() {
        Phonenumber.PhoneNumber number = new Phonenumber.PhoneNumber();
        number.setNationalNumber(91123456789L);
        number.setCountryCode(54);

        PhoneNumber phoneNumber = new PhoneNumber(number);

        assertThat(phoneNumber.toInternationalDisplayText(), equalTo("+54 9 11 2345-6789"));
    }

    @Test
    public void returns_empty_when_no_phone_number() {
        assertThat(new PhoneNumber().toNationalDisplayText(), equalTo(""));
    }

    @Test
    public void is_parcelable() {
        Phonenumber.PhoneNumber number = new Phonenumber.PhoneNumber();
        number.setNationalNumber(91123456789L);
        number.setCountryCode(54);

        Parcel parcel = Parcel.obtain();
        com.coinninja.coinkeeper.model.PhoneNumber phoneNumber = new com.coinninja.coinkeeper.model.PhoneNumber(number);
        phoneNumber.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        phoneNumber = com.coinninja.coinkeeper.model.PhoneNumber.CREATOR.createFromParcel(parcel);

        assertThat(phoneNumber.getCountryCode(), Matchers.equalTo(54));
        assertThat(phoneNumber.getNationalNumber(), Matchers.equalTo(91123456789L));
    }

    @Test
    public void null_phone_number_results_empty() {
        PhoneNumber phoneNumber = new PhoneNumber();
        assertThat(phoneNumber.getCountryCode(), equalTo(0));
        assertThat(phoneNumber.toNationalDisplayText(), equalTo(""));
        assertThat(phoneNumber.toInternationalDisplayText(), equalTo(""));
    }

}