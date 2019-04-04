package com.coinninja.coinkeeper.util;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class HasherTest {

    private Hasher phoneHasher;

    @Before
    public void setUp() {
        phoneHasher = new Hasher();
    }


    @Test
    public void hashes_phone_number() {
        String phoneNumber = "13305551111";

        String hash = "710c3ec37d3bbab4d9b140656ea8ab28d14bad091e12b912dc73d0fbcd78664d";

        assertThat(phoneHasher.hash(phoneNumber), equalTo(hash));
    }

    @Test
    public void hashes_phone_number_obj() {
        Phonenumber.PhoneNumber phoneNumber = new Phonenumber.PhoneNumber();
        phoneNumber.setCountryCode(54);
        phoneNumber.setNationalNumber(3512522169L);

        PhoneNumber pn = new PhoneNumber(phoneNumber);
        assertThat(phoneHasher.hash(pn), equalTo("5cd083b3655f5fda08a57b2a5a6ba67ea1054a0efd4f641337620bde05787b14"));

        phoneNumber.setNationalNumber(93512522169L);
        pn = new PhoneNumber(phoneNumber);
        assertThat(phoneHasher.hash(pn), equalTo("5cd083b3655f5fda08a57b2a5a6ba67ea1054a0efd4f641337620bde05787b14"));
    }
}
