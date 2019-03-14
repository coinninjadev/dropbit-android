package com.coinninja.coinkeeper.model;

import android.os.Parcel;

import com.coinninja.coinkeeper.service.client.model.Contact;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ContactTest {

    private static final String PHONE_NUMBER_INTERNATIONAL_FORMATTED_STRING = "+1 330-555-1111";
    private static final String PHONE_NUMBER_STRING = "+13305551111";
    private PhoneNumber PHONE_NUMBER = new PhoneNumber(PHONE_NUMBER_STRING);
    private static final String DISPLAY_NAME = "Joe Smoe";
    private Contact contact;

    @Before
    public void setUp() {
        contact = new Contact(PHONE_NUMBER, DISPLAY_NAME, true);
    }

    @Test
    public void is_parcelable() {
        Parcel parcel = Parcel.obtain();

        contact.writeToParcel(parcel, -1);
        assertThat(contact.getPhoneNumber(), equalTo(PHONE_NUMBER));
        assertThat(contact.getDisplayName(), equalTo(DISPLAY_NAME));
        assertThat(contact.isVerified(), equalTo(true));

        parcel.setDataPosition(0);
        Contact c = Contact.CREATOR.createFromParcel(parcel);

        assertThat(c.getDisplayName(), equalTo(DISPLAY_NAME));
        assertThat(c.getPhoneNumber(), equalTo(PHONE_NUMBER));
        assertThat(contact.isVerified(), equalTo(true));
    }

    @Test
    public void returns_generic_number() {
        assertThat(contact.getNumberWithCountryCode(), equalTo(PHONE_NUMBER_STRING.replace("+", "")));
    }

    @Test
    public void name() {
        String expectedPhoneDisplay = "(330) 555-1111";

        String nationalNumber = contact.getPhoneNumber().toNationalDisplayText();

        assertThat(nationalNumber, equalTo(expectedPhoneDisplay));
    }

    @Test
    public void provides_displayable_international_string() {
        assertThat(contact.toDisplayNameOrInternationalPhoneNumber(), equalTo(DISPLAY_NAME));

        contact = new Contact(PHONE_NUMBER, "", false);
        assertThat(contact.toDisplayNameOrInternationalPhoneNumber(), equalTo(PHONE_NUMBER_INTERNATIONAL_FORMATTED_STRING));
    }
}