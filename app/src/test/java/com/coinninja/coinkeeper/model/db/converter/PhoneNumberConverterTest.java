package com.coinninja.coinkeeper.model.db.converter;

import com.coinninja.coinkeeper.model.PhoneNumber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class PhoneNumberConverterTest {

    static final String I18N_PHONE = "+12345678901";

    private PhoneNumberConverter converter;

    @Before
    public void setUp() {
        converter = new PhoneNumberConverter();
    }

    @Test
    public void convertToEntityProperty() {
        PhoneNumber actualPhoneNumber = converter.convertToEntityProperty(I18N_PHONE);

        assertThat(actualPhoneNumber.toString(), equalTo(I18N_PHONE));
    }

    @Test
    public void convertToEntityProperty_withEmptyString() {
        assertNull(converter.convertToEntityProperty(""));
    }


    @Test
    public void convertToEntityProperty_withNull() {
        assertNull(converter.convertToEntityProperty(null));
    }

    @Test
    public void convertToDatabaseValue_invalid_phone() {
        assertNull(converter.convertToDatabaseValue(new PhoneNumber("")));
    }

    @Test
    public void convertToDatabaseValue() {
        String actual = converter.convertToDatabaseValue(new PhoneNumber(I18N_PHONE));

        assertThat(actual, equalTo(I18N_PHONE));
    }

    @Test
    public void convertToDatabaseValue_withNull() {
        assertNull(converter.convertToDatabaseValue(null));
    }

}