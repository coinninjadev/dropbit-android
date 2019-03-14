package com.coinninja.coinkeeper.view.adapter.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class BindableTransactionTest {

    private BindableTransaction bindableTransaction;

    @Before
    public void setUp() {
        bindableTransaction = new BindableTransaction();
    }

    @Test
    public void returns_name_as_priority() {
        bindableTransaction.setContactName("--name--");
        bindableTransaction.setContactPhoneNumber("--number--");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("--name--"));
    }

    @Test
    public void returns_number_formatted() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+13305551111");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("(330) 555-1111"));
    }

    @Test
    public void returns_number_when_name_not_available() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+13305551111");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("(330) 555-1111"));
    }

    @Test
    @Config(qualifiers = "es-rAU")
    public void returns_number_when_name_not_available_as_international_format() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("+13305551111");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("+1 330-555-1111"));
    }

    @Test
    public void returns_address_when_no_contact_data() {
        bindableTransaction.setContactName("");
        bindableTransaction.setContactPhoneNumber("");
        bindableTransaction.setTargetAddress("--address--");

        bindableTransaction.getIdentifiableTarget();
        assertThat(bindableTransaction.getIdentifiableTarget(), equalTo("--address--"));
    }
}