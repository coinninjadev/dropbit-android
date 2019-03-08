package com.coinninja.coinkeeper.util;

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
}
