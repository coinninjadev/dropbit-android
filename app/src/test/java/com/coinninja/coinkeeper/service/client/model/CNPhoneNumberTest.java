package com.coinninja.coinkeeper.service.client.model;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "en-rUS")
public class CNPhoneNumberTest {

    public static final String NUMBER = "3305555555";
    public static final int COUNTRY_CODE = 1;
    public static final String I18N = "+13305555555";
    private PhoneNumber phoneNumber;
    private CNPhoneNumber number;

    @Before
    public void before() {
        phoneNumber = new PhoneNumber(I18N);

        number = new CNPhoneNumber(phoneNumber);
    }

    @Test
    public void toPhoneNumber(){
        assertThat(number.toPhoneNumber(), equalTo(phoneNumber));
    }

    @Test
    public void inits_from_phone_number() {
        assertThat(number.getPhoneNumber(), equalTo(NUMBER));
    }


    @Test
    public void init_from_i18() {
        CNPhoneNumber CNPhoneNumber = new CNPhoneNumber(I18N);
        assertThat(COUNTRY_CODE, equalTo(CNPhoneNumber.getCountryCode()));
        assertThat(NUMBER, equalTo(CNPhoneNumber.getPhoneNumber()));
    }

    @Test
    public void converts_to_json() {
        String expected = "{\"country_code\":1,\"phone_number\":\"3305555555\"}";

        Gson gson = new Gson();
        String json = gson.toJson(number, CNPhoneNumber.class);

        assertThat(json, equalTo(expected));
    }

}