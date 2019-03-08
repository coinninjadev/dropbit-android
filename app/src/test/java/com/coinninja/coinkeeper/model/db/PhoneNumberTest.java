package com.coinninja.coinkeeper.model.db;

import android.os.Parcel;

import com.google.i18n.phonenumbers.Phonenumber;

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
    public void is_parcelable() {
        Parcel parcel = Parcel.obtain();

        String i18n = "+12345678901";
        PhoneNumber phoneNumber = new PhoneNumber(i18n);

        phoneNumber.writeToParcel(parcel, -1);

        parcel.setDataPosition(0);
        PhoneNumber p = PhoneNumber.CREATOR.createFromParcel(parcel);

        assertThat(p.toString(), equalTo(i18n));
    }

    @Test
    public void isValid_no_phone() {
        assertFalse(new PhoneNumber((Phonenumber.PhoneNumber) null).isValid());
    }

    @Test
    public void isValid() {
        assertTrue(new PhoneNumber(I18N).isValid());
    }

}