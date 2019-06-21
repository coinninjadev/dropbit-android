package com.coinninja.coinkeeper.model.dto;

import com.coinninja.coinkeeper.model.db.Address;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class AddressDTOTest {

    AddressDTO addressDTO;
    String pubKey = "akjsdha3kjrhaecnb3rkjbef";

    @Before
    public void setUp() {
        Address address = new Address();
        address.setIndex(44);
        address.setChangeIndex(0);
        addressDTO = new AddressDTO(address, pubKey);
    }

    @After
    public void tearDown() {
        addressDTO = null;
    }

    @Test
    public void test_display_derivation_path() {
        assertEquals(addressDTO.getDerivationPath(), "M/49/0/0/0/44");
    }
}