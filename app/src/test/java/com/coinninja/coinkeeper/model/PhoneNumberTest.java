package com.coinninja.coinkeeper.model;

import android.os.Parcel;

import com.google.i18n.phonenumbers.Phonenumber;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class PhoneNumberTest {

    public static final String I18N = "+12345678901";

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
    public void getNationalNumber_defaults_to_0(){
        assertThat(0L, equalTo(new PhoneNumber("").getNationalNumber()));
    }
}