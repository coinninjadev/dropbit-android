package com.coinninja.coinkeeper.service;

import com.coinninja.coinkeeper.model.PhoneNumber;
import com.google.i18n.phonenumbers.Phonenumber;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ContactLookupServiceTest {


    @Test
    public void compare() {
        ContactLookupService service = new ContactLookupService();
        PhoneNumber i18nPhone = new PhoneNumber("+12345678901");

        assertFalse(service.compare(null, null));
        assertFalse(service.compare(new PhoneNumber(""), i18nPhone));
        assertFalse(service.compare(new PhoneNumber((Phonenumber.PhoneNumber) null), i18nPhone));
        assertFalse(service.compare(new PhoneNumber(1, "6668901"), i18nPhone));
        assertFalse(service.compare(new PhoneNumber(1, "567890"), i18nPhone));

        assertTrue(service.compare(i18nPhone, i18nPhone));
        assertTrue(service.compare(new PhoneNumber(1, "2345678901"), i18nPhone));
        assertTrue(service.compare(new PhoneNumber(1, "5678901"), i18nPhone));
    }


}