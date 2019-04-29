package com.coinninja.coinkeeper.service.client.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.coinninja.coinkeeper.model.PhoneNumber;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(AndroidJUnit4.class)
public class InviteMetadataTest {

    @Test
    public void converts_identity_to_phone_number_identity() {
        InviteMetadata.MetadataContact contact = new InviteMetadata.MetadataContact();

        contact.identity = "13305551111";

        PhoneNumber number = contact.identityAsPhoneNumber();

        assertThat(number.getCountryCode(), equalTo(1));
        assertThat(number.getNationalNumber(), equalTo(3305551111L));
    }

    @Test
    public void returns_phone_number_from_phone_number() {
        InviteMetadata.MetadataContact contact = new InviteMetadata.MetadataContact();

        contact.country_code = 1;
        contact.phone_number = "3305551111";


        PhoneNumber number = contact.identityAsPhoneNumber();

        assertThat(number.getCountryCode(), equalTo(1));
        assertThat(number.getNationalNumber(), equalTo(3305551111L));
    }

}